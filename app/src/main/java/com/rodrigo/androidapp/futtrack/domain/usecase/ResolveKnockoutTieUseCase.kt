
package com.rodrigo.androidapp.futtrack.domain.usecase

import com.rodrigo.androidapp.futtrack.domain.model.Match
import com.rodrigo.androidapp.futtrack.domain.model.MatchLeg
import com.rodrigo.androidapp.futtrack.domain.model.MatchStage
import com.rodrigo.androidapp.futtrack.domain.model.MatchStatus
import com.rodrigo.androidapp.futtrack.domain.model.TeamStanding
import javax.inject.Inject

enum class KnockoutVictoryReason {
    AGGREGATE_SCORE,
    BETTER_REGULAR_SEASON
}

sealed interface KnockoutTieResult {

    data object Pending : KnockoutTieResult

    data object RequiresTiebreaker : KnockoutTieResult

    data class Decided(
        val winnerTeamId: String,
        val loserTeamId: String,
        val firstTeamId: String,
        val secondTeamId: String,
        val firstTeamAggregateGoals: Int,
        val secondTeamAggregateGoals: Int,
        val reason: KnockoutVictoryReason
    ) : KnockoutTieResult
}

class ResolveKnockoutTieUseCase @Inject constructor() {

    operator fun invoke(
        firstLeg: Match?,
        secondLeg: Match?,
        regularStandings: List<TeamStanding>
    ): KnockoutTieResult {
        if (firstLeg == null || secondLeg == null) {
            return KnockoutTieResult.Pending
        }

        validateMatches(firstLeg, secondLeg)

        if (
            firstLeg.status != MatchStatus.FINISHED ||
            secondLeg.status != MatchStatus.FINISHED
        ) {
            return KnockoutTieResult.Pending
        }

        val firstHomeScore = firstLeg.homeScore
            ?: return KnockoutTieResult.Pending

        val firstAwayScore = firstLeg.awayScore
            ?: return KnockoutTieResult.Pending

        val secondHomeScore = secondLeg.homeScore
            ?: return KnockoutTieResult.Pending

        val secondAwayScore = secondLeg.awayScore
            ?: return KnockoutTieResult.Pending

        require(
            listOf(
                firstHomeScore,
                firstAwayScore,
                secondHomeScore,
                secondAwayScore
            ).all { it >= 0 }
        ) {
            "Match scores cannot be negative."
        }

        val firstTeamId = firstLeg.homeTeamId
        val secondTeamId = firstLeg.awayTeamId

        val firstTeamGoals = firstHomeScore + secondAwayScore
        val secondTeamGoals = firstAwayScore + secondHomeScore

        val reason: KnockoutVictoryReason
        val winnerTeamId: String

        when {
            firstTeamGoals > secondTeamGoals -> {
                winnerTeamId = firstTeamId
                reason = KnockoutVictoryReason.AGGREGATE_SCORE
            }

            secondTeamGoals > firstTeamGoals -> {
                winnerTeamId = secondTeamId
                reason = KnockoutVictoryReason.AGGREGATE_SCORE
            }

            else -> {
                val betterTeamId = findBetterRegularSeasonTeam(
                    firstTeamId = firstTeamId,
                    secondTeamId = secondTeamId,
                    standings = regularStandings
                ) ?: return KnockoutTieResult.RequiresTiebreaker

                winnerTeamId = betterTeamId
                reason = KnockoutVictoryReason.BETTER_REGULAR_SEASON
            }
        }

        val loserTeamId = if (winnerTeamId == firstTeamId) {
            secondTeamId
        } else {
            firstTeamId
        }

        return KnockoutTieResult.Decided(
            winnerTeamId = winnerTeamId,
            loserTeamId = loserTeamId,
            firstTeamId = firstTeamId,
            secondTeamId = secondTeamId,
            firstTeamAggregateGoals = firstTeamGoals,
            secondTeamAggregateGoals = secondTeamGoals,
            reason = reason
        )
    }

    private fun validateMatches(
        firstLeg: Match,
        secondLeg: Match
    ) {
        require(firstLeg.id != secondLeg.id) {
            "Knockout legs must be different matches."
        }

        require(
            firstLeg.stage != MatchStage.REGULAR &&
                    firstLeg.stage == secondLeg.stage
        ) {
            "Both matches must belong to the same knockout stage."
        }

        require(
            firstLeg.leg == MatchLeg.FIRST &&
                    secondLeg.leg == MatchLeg.SECOND
        ) {
            "Matches must represent the first and second legs."
        }

        require(firstLeg.homeTeamId != firstLeg.awayTeamId) {
            "A team cannot play against itself."
        }

        require(
            firstLeg.homeTeamId == secondLeg.awayTeamId &&
                    firstLeg.awayTeamId == secondLeg.homeTeamId
        ) {
            "Knockout legs must have reversed home and away teams."
        }
    }

    private fun findBetterRegularSeasonTeam(
        firstTeamId: String,
        secondTeamId: String,
        standings: List<TeamStanding>
    ): String? {
        val firstStanding = requireNotNull(
            standings.find { it.team.id == firstTeamId }
        ) {
            "First team is missing from the regular standings."
        }

        val secondStanding = requireNotNull(
            standings.find { it.team.id == secondTeamId }
        ) {
            "Second team is missing from the regular standings."
        }

        val comparison = regularSeasonComparator.compare(
            firstStanding,
            secondStanding
        )

        return when {
            comparison > 0 -> firstTeamId
            comparison < 0 -> secondTeamId
            else -> null
        }
    }

    private companion object {
        val regularSeasonComparator =
            compareBy<TeamStanding> { it.points }
                .thenBy { it.goalDifference }
                .thenBy { it.goalsFor }
    }
}
