
package com.rodrigo.androidapp.futtrack.domain.usecase

import com.rodrigo.androidapp.futtrack.domain.model.Match
import com.rodrigo.androidapp.futtrack.domain.model.MatchLeg
import com.rodrigo.androidapp.futtrack.domain.model.MatchStage
import javax.inject.Inject

enum class KnockoutSchedulingError {
    INVALID_STAGE,
    MISSING_LEG,
    SAME_TEAM,
    MISSING_MATCH_NUMBER,
    OCCUPIED_SLOT,
    DUPLICATE_LEG,
    MISSING_FIRST_LEG,
    INVALID_OPPONENTS,
    INVALID_HOME_AWAY_ORDER,
    INVALID_LEG_DATE
}

sealed interface KnockoutSchedulingResult {
    data object Valid : KnockoutSchedulingResult

    data class Invalid(
        val error: KnockoutSchedulingError
    ) : KnockoutSchedulingResult
}

class ValidateKnockoutSchedulingUseCase @Inject constructor() {

    operator fun invoke(
        candidate: Match,
        competitionMatches: List<Match>
    ): KnockoutSchedulingResult {
        if (candidate.stage == MatchStage.REGULAR) {
            return invalid(KnockoutSchedulingError.INVALID_STAGE)
        }

        val leg = candidate.leg
            ?: return invalid(KnockoutSchedulingError.MISSING_LEG)

        if (candidate.homeTeamId == candidate.awayTeamId) {
            return invalid(KnockoutSchedulingError.SAME_TEAM)
        }

        val matchNumber = candidate.matchNumber
            ?: return invalid(KnockoutSchedulingError.MISSING_MATCH_NUMBER)

        val occupiedSlot = competitionMatches.any { existing ->
            existing.date.toLocalDate() == candidate.date.toLocalDate() &&
                    existing.matchNumber == matchNumber
        }

        if (occupiedSlot) {
            return invalid(KnockoutSchedulingError.OCCUPIED_SLOT)
        }

        val stageMatches = competitionMatches.filter { existing ->
            existing.stage == candidate.stage
        }

        if (stageMatches.any { existing -> existing.leg == leg }) {
            return invalid(KnockoutSchedulingError.DUPLICATE_LEG)
        }

        if (leg == MatchLeg.SECOND) {
            return validateSecondLeg(candidate, stageMatches)
        }

        return KnockoutSchedulingResult.Valid
    }

    private fun validateSecondLeg(
        candidate: Match,
        stageMatches: List<Match>
    ): KnockoutSchedulingResult {
        val firstLeg = stageMatches
            .singleOrNull { match ->
                match.leg == MatchLeg.FIRST
            }
            ?: return invalid(KnockoutSchedulingError.MISSING_FIRST_LEG)

        val sameOpponents = setOf(
            candidate.homeTeamId,
            candidate.awayTeamId
        ) == setOf(
            firstLeg.homeTeamId,
            firstLeg.awayTeamId
        )

        if (!sameOpponents) {
            return invalid(KnockoutSchedulingError.INVALID_OPPONENTS)
        }

        val reversedHomeAway =
            candidate.homeTeamId == firstLeg.awayTeamId &&
                    candidate.awayTeamId == firstLeg.homeTeamId

        if (!reversedHomeAway) {
            return invalid(
                KnockoutSchedulingError.INVALID_HOME_AWAY_ORDER
            )
        }

        if (!candidate.date.isAfter(firstLeg.date)) {
            return invalid(KnockoutSchedulingError.INVALID_LEG_DATE)
        }

        return KnockoutSchedulingResult.Valid
    }

    private fun invalid(
        error: KnockoutSchedulingError
    ): KnockoutSchedulingResult {
        return KnockoutSchedulingResult.Invalid(error)
    }
}
