package com.rodrigo.androidapp.futtrack.domain.model

data class SemifinalStatus(
    val secondPlaceTeam: Team,
    val thirdPlaceTeam: Team,
    val differencePercentage: Double,
    val limitPercentage: Double
) {
    val isEligible: Boolean
        get() = differencePercentage <= limitPercentage
}