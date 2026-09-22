package com.rodrigo.androidapp.futtrack.presentation.announcements

import com.rodrigo.androidapp.futtrack.domain.model.Announcement
import com.rodrigo.androidapp.futtrack.domain.repository.AnnouncementRepository
import com.rodrigo.androidapp.futtrack.domain.usecase.ObserveAnnouncementsUseCase
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AnnouncementsViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun test_givenViewModel_whenCreated_thenStartsLoading() = runTest {
        val announcements = MutableStateFlow(emptyList<Announcement>())
        val viewModel = createViewModel(announcements)

        assertEquals(
            AnnouncementsUiState.Loading,
            viewModel.uiState.value
        )
    }

    @Test
    fun test_givenEmptyRepository_whenObserved_thenReturnsEmptySuccess() = runTest {
        val announcements = MutableStateFlow(emptyList<Announcement>())
        val viewModel = createViewModel(announcements)

        backgroundScope.launch {
            viewModel.uiState.collect()
        }

        runCurrent()

        assertEquals(
            AnnouncementsUiState.Success(emptyList()),
            viewModel.uiState.value
        )
    }

    @Test
    fun test_givenAnnouncements_whenObserved_thenReturnsNewestFirst() = runTest {
        val oldest = announcement("oldest", "2026-09-19T10:00:00Z")
        val newest = announcement("newest", "2026-09-21T10:00:00Z")

        val announcements = MutableStateFlow(
            listOf(oldest, newest)
        )
        val viewModel = createViewModel(announcements)

        backgroundScope.launch {
            viewModel.uiState.collect()
        }

        runCurrent()

        assertEquals(
            AnnouncementsUiState.Success(listOf(newest, oldest)),
            viewModel.uiState.value
        )
    }

    @Test
    fun test_givenRepositoryUpdates_whenObserved_thenUpdatesUiState() = runTest {
        val first = announcement("first", "2026-09-19T10:00:00Z")
        val second = announcement("second", "2026-09-21T10:00:00Z")

        val announcements = MutableStateFlow(listOf(first))
        val viewModel = createViewModel(announcements)

        backgroundScope.launch {
            viewModel.uiState.collect()
        }

        runCurrent()

        assertEquals(
            AnnouncementsUiState.Success(listOf(first)),
            viewModel.uiState.value
        )

        announcements.value = listOf(first, second)
        runCurrent()

        assertEquals(
            AnnouncementsUiState.Success(listOf(second, first)),
            viewModel.uiState.value
        )
    }

    @Test
    fun test_givenRepositoryFailure_whenObserved_thenReturnsError() = runTest {
        val failingFlow = flow<List<Announcement>> {
            throw IllegalStateException("Firestore unavailable.")
        }
        val viewModel = createViewModel(failingFlow)

        backgroundScope.launch {
            viewModel.uiState.collect()
        }

        runCurrent()

        assertEquals(
            AnnouncementsUiState.Error,
            viewModel.uiState.value
        )
    }

    private fun createViewModel(
        announcements: Flow<List<Announcement>>
    ): AnnouncementsViewModel {
        val repository = FakeAnnouncementRepository(announcements)
        val useCase = ObserveAnnouncementsUseCase(repository)

        return AnnouncementsViewModel(useCase)
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