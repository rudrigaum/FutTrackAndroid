package com.rodrigo.androidapp.futtrack.domain.usecase

import com.rodrigo.androidapp.futtrack.domain.model.Match
import com.rodrigo.androidapp.futtrack.domain.model.MatchLeg
import com.rodrigo.androidapp.futtrack.domain.model.MatchStage
import com.rodrigo.androidapp.futtrack.domain.model.TeamStanding
import javax.inject.Inject

data class KnockoutProgression(
    val semifinalResult: KnockoutTieResult?,
    val finalOpponentTeamId: String?,
    val finalResult: KnockoutTieResult
)

class CalculateKnockoutProgressionUseCase @Inject constructor(
    private val calculateSemifinalStatusUseCase: CalculateSemifinalStatusUseCase,
    private val resolveKnockoutTieUseCase: ResolveKnockoutTieUseCase
) {
    operator fun invoke(
        regularStandings: List<TeamStanding>,
        matches: List<Match>
    ): KnockoutProgression {
        if (regularStandings.size < REQUIRED_TEAMS) {
            return KnockoutProgression(
                semifinalResult = null,
                finalOpponentTeamId = null,
                finalResult = KnockoutTieResult.Pending
            )
        }

        val rankedStandings = regularStandings.sortedWith(standingOrder)
        val firstPlace = rankedStandings[0]
        val secondPlace = rankedStandings[1]
        val thirdPlace = rankedStandings[2]

        val semifinalEligible = calculateSemifinalStatusUseCase(
            secondPlace = secondPlace,
            thirdPlace = thirdPlace
        ).isEligible

        val semifinalResult = if (semifinalEligible) {
            resolveStage(
                stage = MatchStage.SEMIFINAL,
                expectedTeamIds = setOf(secondPlace.team.id, thirdPlace.team.id),
                matches = matches,
                regularStandings = rankedStandings
            )
        } else {
            null
        }

        val finalOpponentTeamId = if (semifinalEligible) {
            (semifinalResult as? KnockoutTieResult.Decided)?.winnerTeamId
        } else {
            secondPlace.team.id
        }

        val finalResult = if (finalOpponentTeamId != null) {
            resolveStage(
                stage = MatchStage.FINAL,
                expectedTeamIds = setOf(firstPlace.team.id, finalOpponentTeamId),
                matches = matches,
                regularStandings = rankedStandings
            )
        } else {
            KnockoutTieResult.Pending
        }

        return KnockoutProgression(
            semifinalResult = semifinalResult,
            finalOpponentTeamId = finalOpponentTeamId,
            finalResult = finalResult
        )
    }

    private fun resolveStage(
        stage: MatchStage,
        expectedTeamIds: Set<String>,
        matches: List<Match>,
        regularStandings: List<TeamStanding>
    ): KnockoutTieResult {
        val stageMatches = matches.filter { it.stage == stage }
        val firstLeg = stageMatches
            .filter { it.leg == MatchLeg.FIRST }
            .singleOrNull() ?: return KnockoutTieResult.Pending
        val secondLeg = stageMatches
            .filter { it.leg == MatchLeg.SECOND }
            .singleOrNull() ?: return KnockoutTieResult.Pending

        if (
            firstLeg.id == secondLeg.id ||
            firstLeg.homeTeamId == firstLeg.awayTeamId ||
            setOf(firstLeg.homeTeamId, firstLeg.awayTeamId) != expectedTeamIds ||
            firstLeg.homeTeamId != secondLeg.awayTeamId ||
            firstLeg.awayTeamId != secondLeg.homeTeamId
        ) {
            return KnockoutTieResult.Pending
        }

        return resolveKnockoutTieUseCase(
            firstLeg = firstLeg,
            secondLeg = secondLeg,
            regularStandings = regularStandings
        )
    }

    private companion object {
        const val REQUIRED_TEAMS = 3

        val standingOrder = compareByDescending<TeamStanding> { it.points }
            .thenByDescending { it.goalDifference }
            .thenByDescending { it.goalsFor }
            .thenBy { it.team.name }
    }
}
