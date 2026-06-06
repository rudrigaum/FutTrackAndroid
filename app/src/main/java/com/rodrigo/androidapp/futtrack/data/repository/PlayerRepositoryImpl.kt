package com.rodrigo.androidapp.futtrack.data.repository

import com.rodrigo.androidapp.futtrack.data.local.dao.PlayerDao
import com.rodrigo.androidapp.futtrack.data.local.entity.toDomain
import com.rodrigo.androidapp.futtrack.domain.model.Player
import com.rodrigo.androidapp.futtrack.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlayerRepositoryImpl @Inject constructor(
    private val dao: PlayerDao
) : PlayerRepository {

    override fun getPlayersByTeam(teamId: String): Flow<List<Player>> {
        return dao.getPlayersByTeam(teamId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTopScorers(): Flow<List<Player>> {
        return dao.getTopScorers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun updatePlayerGoals(playerId: String, goals: Int) {
        dao.updatePlayerGoals(playerId, goals)
    }
}