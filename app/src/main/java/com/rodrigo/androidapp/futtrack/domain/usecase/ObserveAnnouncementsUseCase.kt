package com.rodrigo.androidapp.futtrack.domain.usecase

import com.rodrigo.androidapp.futtrack.domain.model.Announcement
import com.rodrigo.androidapp.futtrack.domain.repository.AnnouncementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveAnnouncementsUseCase @Inject constructor(
    private val repository: AnnouncementRepository
) {

    operator fun invoke(): Flow<List<Announcement>> {
        return repository.getAnnouncements()
            .map { announcements ->
                announcements.sortedByDescending { announcement ->
                    announcement.createdAt
                }
            }
    }
}