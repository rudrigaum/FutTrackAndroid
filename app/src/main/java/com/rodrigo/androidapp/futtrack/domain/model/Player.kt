package com.rodrigo.androidapp.futtrack.domain.model

data class Player(
    val id: String,
    val name: String,
    val teamId: String,
    val isGoalkeeper: Boolean
)