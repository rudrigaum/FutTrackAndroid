package com.rodrigo.androidapp.futtrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rodrigo.androidapp.futtrack.domain.model.Team

@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey val id: String,
    val name: String,
    val isoCode: String,
    val crestUrl: String?
)

fun TeamEntity.toDomain(): Team {
    return Team(
        id = id,
        name = name,
        isoCode = isoCode,
        crestUrl = crestUrl
    )
}

fun Team.toEntity(): TeamEntity {
    return TeamEntity(
        id = id,
        name = name,
        isoCode = isoCode,
        crestUrl = crestUrl
    )
}