
package com.rodrigo.androidapp.futtrack.domain.usecase

import com.rodrigo.androidapp.futtrack.domain.model.Match
import com.rodrigo.androidapp.futtrack.domain.repository.MatchRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class ScheduleKnockoutMatchUseCase @Inject constructor(
    private val matchRepository: MatchRepository,
    private val validateKnockoutSchedulingUseCase:
    ValidateKnockoutSchedulingUseCase
) {

    suspend operator fun invoke(
        candidate: Match
    ): KnockoutSchedulingResult {
        val existingMatches = matchRepository
            .getMatches()
            .first()

        val validation = validateKnockoutSchedulingUseCase(
            candidate = candidate,
            competitionMatches = existingMatches
        )

        return when (validation) {
            KnockoutSchedulingResult.Valid -> {
                matchRepository.scheduleMatch(candidate)
                KnockoutSchedulingResult.Valid
            }

            is KnockoutSchedulingResult.Invalid -> validation
        }
    }
}
