package com.rodrigo.androidapp.futtrack.domain.usecase

import com.rodrigo.androidapp.futtrack.domain.model.SemifinalStatus
import com.rodrigo.androidapp.futtrack.domain.model.TeamStanding
import javax.inject.Inject

class CalculateSemifinalStatusUseCase @Inject constructor() {

    operator fun invoke(
        secondPlace: TeamStanding,
        thirdPlace: TeamStanding
    ): SemifinalStatus {
        val differencePercentage = calculateDifferencePercentage(
            secondPlacePoints = secondPlace.points,
            thirdPlacePoints = thirdPlace.points
        )

        return SemifinalStatus(
            secondPlaceTeam = secondPlace.team,
            thirdPlaceTeam = thirdPlace.team,
            differencePercentage = differencePercentage,
            limitPercentage = SEMIFINAL_LIMIT_PERCENTAGE
        )
    }

    private fun calculateDifferencePercentage(
        secondPlacePoints: Int,
        thirdPlacePoints: Int
    ): Double {
        if (secondPlacePoints == 0) {
            return 0.0
        }

        val pointsDifference =
            secondPlacePoints - thirdPlacePoints

        return pointsDifference.toDouble() /
                secondPlacePoints *
                PERCENTAGE_MULTIPLIER
    }

    private companion object {
        const val SEMIFINAL_LIMIT_PERCENTAGE = 30.0
        const val PERCENTAGE_MULTIPLIER = 100.0
    }
}