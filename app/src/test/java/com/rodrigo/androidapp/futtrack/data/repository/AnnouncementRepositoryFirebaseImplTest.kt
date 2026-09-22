package com.rodrigo.androidapp.futtrack.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.rodrigo.androidapp.futtrack.domain.model.Announcement
import com.rodrigo.androidapp.futtrack.domain.model.AnnouncementType
import java.time.Instant
import java.util.Date
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

@OptIn(ExperimentalCoroutinesApi::class)
class AnnouncementRepositoryFirebaseImplTest {

    private val firestore = Mockito.mock(FirebaseFirestore::class.java)
    private val collection = Mockito.mock(CollectionReference::class.java)
    private val registration = Mockito.mock(ListenerRegistration::class.java)

    private lateinit var eventListener: EventListener<QuerySnapshot>
    private lateinit var repository: AnnouncementRepositoryFirebaseImpl

    @Before
    fun setUp() {
        Mockito.`when`(firestore.collection("announcements"))
            .thenReturn(collection)

        Mockito.doAnswer { invocation ->
            eventListener = invocation.getArgument(0)
            registration
        }.`when`(collection)
            .addSnapshotListener(Mockito.any<EventListener<QuerySnapshot>>())

        repository = AnnouncementRepositoryFirebaseImpl(firestore)
    }

    @Test
    fun test_givenValidDocument_whenObserved_thenMapsAnnouncement() = runTest {
        val createdAt = Instant.parse("2026-09-21T10:00:00Z")
        val document = document(
            id = "announcement-1",
            title = "Next match",
            message = "The match starts at 9 AM.",
            type = "MATCH",
            isImportant = true,
            createdAt = createdAt
        )

        val result = async {
            repository.getAnnouncements().first()
        }

        runCurrent()
        eventListener.onEvent(snapshot(document), null)

        assertEquals(
            listOf(
                Announcement(
                    id = "announcement-1",
                    title = "Next match",
                    message = "The match starts at 9 AM.",
                    type = AnnouncementType.MATCH,
                    isImportant = true,
                    createdAt = createdAt
                )
            ),
            result.await()
        )
    }

    @Test
    fun test_givenInvalidDocuments_whenObserved_thenSkipsInvalidAndDefaultsType() = runTest {
        val validDocument = document(
            id = "valid",
            type = "UNKNOWN"
        )
        val blankTitleDocument = document(
            id = "blank-title",
            title = " "
        )
        val missingDateDocument = document(
            id = "missing-date",
            createdAt = null
        )

        val result = async {
            repository.getAnnouncements().first()
        }

        runCurrent()
        eventListener.onEvent(
            snapshot(validDocument, blankTitleDocument, missingDateDocument),
            null
        )

        val announcements = result.await()

        assertEquals(1, announcements.size)
        assertEquals("valid", announcements.single().id)
        assertEquals(AnnouncementType.GENERAL, announcements.single().type)
    }

    @Test
    fun test_givenSnapshotUpdates_whenObserved_thenEmitsEachList() = runTest {
        val firstDocument = document(id = "first")
        val secondDocument = document(id = "second")

        val result = async {
            repository.getAnnouncements()
                .take(2)
                .toList()
        }

        runCurrent()

        eventListener.onEvent(snapshot(firstDocument), null)
        eventListener.onEvent(snapshot(firstDocument, secondDocument), null)

        val emissions = result.await()

        assertEquals(
            listOf(
                listOf("first"),
                listOf("first", "second")
            ),
            emissions.map { announcements ->
                announcements.map(Announcement::id)
            }
        )

        Mockito.verify(registration).remove()
    }

    @Test
    fun test_givenCancelledCollection_whenCancelled_thenRemovesListener() = runTest {
        val job = backgroundScope.launch {
            repository.getAnnouncements().collect()
        }

        runCurrent()
        job.cancelAndJoin()

        Mockito.verify(registration).remove()
    }

    @Test
    fun test_givenFirestoreError_whenObserved_thenPropagatesError() = runTest {
        val expectedError = Mockito.mock(
            FirebaseFirestoreException::class.java
        )

        val result = async {
            runCatching {
                repository.getAnnouncements().first()
            }.exceptionOrNull()
        }

        runCurrent()
        eventListener.onEvent(null, expectedError)

        assertSame(expectedError, result.await())
        Mockito.verify(registration).remove()
    }

    private fun snapshot(
        vararg documents: DocumentSnapshot
    ): QuerySnapshot {
        return Mockito.mock(QuerySnapshot::class.java).also { snapshot ->
            Mockito.`when`(snapshot.documents)
                .thenReturn(documents.toList())
        }
    }

    private fun document(
        id: String,
        title: String? = "Test title",
        message: String? = "Test message",
        type: String? = "GENERAL",
        isImportant: Boolean? = false,
        createdAt: Instant? = Instant.parse("2026-09-21T10:00:00Z")
    ): DocumentSnapshot {
        return Mockito.mock(DocumentSnapshot::class.java).also { document ->
            Mockito.`when`(document.id).thenReturn(id)
            Mockito.`when`(document.getString("title")).thenReturn(title)
            Mockito.`when`(document.getString("message")).thenReturn(message)
            Mockito.`when`(document.getString("type")).thenReturn(type)
            Mockito.`when`(document.getBoolean("isImportant")).thenReturn(isImportant)
            Mockito.`when`(document.getTimestamp("createdAt"))
                .thenReturn(
                    createdAt?.let { instant ->
                        Timestamp(Date.from(instant))
                    }
                )
        }
    }
}