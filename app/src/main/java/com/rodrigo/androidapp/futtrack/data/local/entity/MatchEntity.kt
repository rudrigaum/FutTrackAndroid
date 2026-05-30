package com.rodrigo.androidapp.futtrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rodrigo.androidapp.futtrack.domain.model.Match
import com.rodrigo.androidapp.futtrack.domain.model.MatchStatus
import java.time.LocalDateTime

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: String,
    val homeTeamId: String,
    val awayTeamId: String,
    val homeScore: Int?,
    val awayScore: Int?,
    val date: String,
    val status: String
)

fun MatchEntity.toDomain(): Match {
    return Match(
        id = id,
        homeTeamId = homeTeamId,
        awayTeamId = awayTeamId,
        homeScore = homeScore,
        awayScore = awayScore,
        date = LocalDateTime.parse(date),
        status = MatchStatus.valueOf(status)
    )
}

fun Match.toEntity(): MatchEntity {
    return MatchEntity(
        id = id,
        homeTeamId = homeTeamId,
        awayTeamId = awayTeamId,
        homeScore = homeScore,
        awayScore = awayScore,
        date = date.toString(),
        status = status.name
    )
}