package com.rodrigo.androidapp.futtrack.domain.repository

import com.rodrigo.androidapp.futtrack.domain.model.Announcement
import kotlinx.coroutines.flow.Flow

interface AnnouncementRepository {

    fun getAnnouncements(): Flow<List<Announcement>>
}