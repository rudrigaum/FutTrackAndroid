package com.rodrigo.androidapp.futtrack.data.repository

import com.rodrigo.androidapp.futtrack.domain.model.Team
import com.rodrigo.androidapp.futtrack.domain.repository.TeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class TeamRepositoryImpl : TeamRepository {
    private val mockTeams = listOf(
        Team(id = "1", name = "Brasil", isoCode = "BRA"),
        Team(id = "2", name = "Itália", isoCode = "ITA"),
        Team(id = "3", name = "Alemanha", isoCode = "ALE")
    )

    override fun getTeams(): Flow<List<Team>> {
        return flowOf(mockTeams)
    }
}