package com.rodrigo.androidapp.futtrack.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rodrigo.androidapp.futtrack.domain.model.Player

@Entity(
    tableName = "players",
    foreignKeys = [
        ForeignKey(
            entity = TeamEntity::class,
            parentColumns = ["id"],
            childColumns = ["teamId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("teamId")]
)
data class PlayerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val teamId: String,
    val isGoalkeeper: Boolean,
    val number: String? = null,
    val goals: Int = 0
)

fun PlayerEntity.toDomain() = Player(id, name, teamId, isGoalkeeper, number, goals)
fun Player.toEntity() = PlayerEntity(id, name, teamId, isGoalkeeper, number, goals)