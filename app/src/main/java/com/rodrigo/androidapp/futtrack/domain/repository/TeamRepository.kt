package com.rodrigo.androidapp.futtrack.domain.repository

import com.rodrigo.androidapp.futtrack.domain.model.Team
import kotlinx.coroutines.flow.Flow

interface TeamRepository {
    fun getTeams(): Flow<List<Team>>
}