package com.rodrigo.androidapp.futtrack.domain.model

import java.time.LocalDateTime
import java.util.UUID

enum class MatchStatus {
    SCHEDULED,
    IN_PROGRESS,
    FINISHED
}

data class Match(
    val id: String = UUID.randomUUID().toString(),
    val homeTeamId: String,
    val awayTeamId: String,
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val date: LocalDateTime,
    val status: MatchStatus = MatchStatus.SCHEDULED
)