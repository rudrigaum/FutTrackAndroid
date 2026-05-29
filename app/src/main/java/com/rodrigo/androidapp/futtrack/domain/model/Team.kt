package com.rodrigo.androidapp.futtrack.domain.model


data class Team(
    val id: String,
    val name: String,
    val isoCode: String,
    val crestUrl: String? = null
)