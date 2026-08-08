package com.rodrigo.androidapp.futtrack.domain.model

data class StandingBaseline(
    val teamId: String,
    val points: Int,
    val matchesPlayed: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goalsFor: Int,
    val goalsAgainst: Int
)