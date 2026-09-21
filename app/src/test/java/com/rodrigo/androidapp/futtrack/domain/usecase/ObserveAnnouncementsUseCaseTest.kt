package com.rodrigo.androidapp.futtrack.domain.usecase

import com.rodrigo.androidapp.futtrack.domain.model.Announcement
import com.rodrigo.androidapp.futtrack.domain.repository.AnnouncementRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveAnnouncementsUseCaseTest {

    @Test
    fun test_givenUnsortedAnnouncements_whenObserved_thenReturnsNewestFirst() = runBlocking {
        val oldest = announcement("oldest", "2026-09-19T10:00:00Z")
        val newest = announcement("newest", "2026-09-21T10:00:00Z")
        val middle = announcement("middle", "2026-09-20T10:00:00Z")

        val repository = FakeAnnouncementRepository(
            announcements = flowOf(listOf(oldest, newest, middle))
        )
        val useCase = ObserveAnnouncementsUseCase(repository)

        val emissions = useCase().toList()

        assertEquals(
            listOf("newest", "middle", "oldest"),
            emissions.single().map(Announcement::id)
        )
    }

    @Test
    fun test_givenNoAnnouncements_whenObserved_thenReturnsEmptyList() = runBlocking {
        val repository = FakeAnnouncementRepository(
            announcements = flowOf(emptyList())
        )
        val useCase = ObserveAnnouncementsUseCase(repository)

        val emissions = useCase().toList()

        assertEquals(emptyList<Announcement>(), emissions.single())
    }

    @Test
    fun test_givenNewEmissions_whenObserved_thenSortsEachEmission() = runBlocking {
        val oldest = announcement("oldest", "2026-09-19T10:00:00Z")
        val middle = announcement("middle", "2026-09-20T10:00:00Z")
        val newest = announcement("newest", "2026-09-21T10:00:00Z")

        val repository = FakeAnnouncementRepository(
            announcements = flowOf(
                listOf(oldest, middle),
                listOf(oldest, newest, middle)
            )
        )
        val useCase = ObserveAnnouncementsUseCase(repository)

        val emissions = useCase().toList()

        assertEquals(
            listOf(
                listOf("middle", "oldest"),
                listOf("newest", "middle", "oldest")
            ),
            emissions.map { announcements ->
                announcements.map(Announcement::id)
            }
        )
    }

    private fun announcement(
        id: String,
        createdAt: String
    ): Announcement {
        return Announcement(
            id = id,
            title = "Test announcement",
            message = "Test message",
            createdAt = Instant.parse(createdAt)
        )
    }

    private class FakeAnnouncementRepository(
        private val announcements: Flow<List<Announcement>>
    ) : AnnouncementRepository {

        override fun getAnnouncements(): Flow<List<Announcement>> {
            return announcements
        }
    }
}