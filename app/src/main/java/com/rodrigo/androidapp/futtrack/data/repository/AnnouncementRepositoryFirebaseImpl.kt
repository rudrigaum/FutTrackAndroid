package com.rodrigo.androidapp.futtrack.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.rodrigo.androidapp.futtrack.domain.model.Announcement
import com.rodrigo.androidapp.futtrack.domain.model.AnnouncementType
import com.rodrigo.androidapp.futtrack.domain.repository.AnnouncementRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class AnnouncementRepositoryFirebaseImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AnnouncementRepository {

    private val announcementsCollection =
        firestore.collection(ANNOUNCEMENTS_COLLECTION)

    override fun getAnnouncements(): Flow<List<Announcement>> = callbackFlow {
        val listener = announcementsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val announcements = snapshot
                ?.documents
                ?.mapNotNull { document ->
                    document.toAnnouncement()
                }
                .orEmpty()

            trySend(announcements)
        }

        awaitClose {
            listener.remove()
        }
    }

    private fun DocumentSnapshot.toAnnouncement(): Announcement? {
        val title = getString(FIELD_TITLE)
            ?.takeIf(String::isNotBlank)
            ?: return null

        val message = getString(FIELD_MESSAGE)
            ?.takeIf(String::isNotBlank)
            ?: return null

        val createdAt = getTimestamp(FIELD_CREATED_AT)
            ?.toDate()
            ?.toInstant()
            ?: return null

        val type = getString(FIELD_TYPE)
            ?.let { value ->
                AnnouncementType.entries.find { it.name == value }
            }
            ?: AnnouncementType.GENERAL

        return Announcement(
            id = id,
            title = title,
            message = message,
            type = type,
            isImportant = getBoolean(FIELD_IS_IMPORTANT) ?: false,
            createdAt = createdAt
        )
    }

    private companion object {
        const val ANNOUNCEMENTS_COLLECTION = "announcements"

        const val FIELD_TITLE = "title"
        const val FIELD_MESSAGE = "message"
        const val FIELD_TYPE = "type"
        const val FIELD_IS_IMPORTANT = "isImportant"
        const val FIELD_CREATED_AT = "createdAt"
    }
}