
package com.rodrigo.androidapp.futtrack.domain.model

import java.time.LocalDateTime
import java.util.UUID

enum class MatchStatus {
    SCHEDULED,
    IN_PROGRESS,
    FINISHED
}

enum class MatchStage {
    REGULAR,
    SEMIFINAL,
    FINAL
}

enum class MatchLeg {
    FIRST,
    SECOND
}

data class Match(
    val id: String = UUID.randomUUID().toString(),
    val matchNumber: Int? = null,
    val homeTeamId: String,
    val awayTeamId: String,
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val date: LocalDateTime,
    val status: MatchStatus = MatchStatus.SCHEDULED,
    val stage: MatchStage = MatchStage.REGULAR,
    val leg: MatchLeg? = null
)
