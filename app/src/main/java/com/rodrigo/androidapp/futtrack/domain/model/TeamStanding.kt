package com.rodrigo.androidapp.futtrack.domain.model

data class TeamStanding(
    val team: Team,
    val points: Int = 0,
    val matchesPlayed: Int = 0,
    val wins: Int = 0,
    val draws: Int = 0,
    val losses: Int = 0,
    val goalsFor: Int = 0,
    val goalsAgainst: Int = 0
) {
    val goalDifference: Int
        get() = goalsFor - goalsAgainst
}