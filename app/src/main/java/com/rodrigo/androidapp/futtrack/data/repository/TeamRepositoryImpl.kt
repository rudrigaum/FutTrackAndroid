package com.rodrigo.androidapp.futtrack.data.repository

import com.rodrigo.androidapp.futtrack.data.local.dao.TeamDao
import com.rodrigo.androidapp.futtrack.data.local.entity.TeamEntity
import com.rodrigo.androidapp.futtrack.data.local.entity.toDomain
import com.rodrigo.androidapp.futtrack.data.local.entity.toEntity
import com.rodrigo.androidapp.futtrack.domain.model.Team
import com.rodrigo.androidapp.futtrack.domain.repository.TeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class TeamRepositoryImpl @Inject constructor(
    private val dao: TeamDao
) : TeamRepository {

    override fun getTeams(): Flow<List<Team>> {
        return dao.getAllTeams()
            .onEach { entities ->
                if (entities.isEmpty()) {
                    val initialTeams = listOf(
                        TeamEntity(id = "1", name = "Alemanha", isoCode = "ALE", crestUrl = null),
                        TeamEntity(id = "2", name = "Brasil", isoCode = "BRA", crestUrl = null),
                        TeamEntity(id = "3", name = "Italia", isoCode = "ITA", crestUrl = null)
                    )
                    initialTeams.forEach { dao.insertTeam(it) }
                }
            }
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override suspend fun addTeam(team: Team) {
        dao.insertTeam(team.toEntity())
    }

    override suspend fun deleteTeam(teamId: String) {
        dao.deleteTeam(teamId)
    }
}