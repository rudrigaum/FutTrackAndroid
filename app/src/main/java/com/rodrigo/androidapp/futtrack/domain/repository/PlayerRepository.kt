package com.rodrigo.androidapp.futtrack.domain.repository

import com.rodrigo.androidapp.futtrack.domain.model.Player
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    fun getPlayersByTeam(teamId: String): Flow<List<Player>>
    fun getTopScorers(): Flow<List<Player>>
    suspend fun updatePlayerGoals(playerId: String, goals: Int)
}