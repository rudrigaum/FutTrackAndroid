package com.rodrigo.androidapp.futtrack.domain.model

import java.time.Instant

enum class AnnouncementType {
    GENERAL,
    MATCH,
    SCHEDULE,
    RESULT
}

data class Announcement(
    val id: String,
    val title: String,
    val message: String,
    val type: AnnouncementType = AnnouncementType.GENERAL,
    val isImportant: Boolean = false,
    val createdAt: Instant
)