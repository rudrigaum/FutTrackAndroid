package com.rodrigo.androidapp.futtrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rodrigo.androidapp.futtrack.data.local.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players WHERE teamId = :teamId ORDER BY isGoalkeeper DESC, name ASC")
    fun getPlayersByTeam(teamId: String): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players ORDER BY goals DESC, name ASC")
    fun getTopScorers(): Flow<List<PlayerEntity>>
    @Query("UPDATE players SET goals = :goals WHERE id = :playerId")
    suspend fun updatePlayerGoals(playerId: String, goals: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayers(players: List<PlayerEntity>)
}