package com.rodrigo.androidapp.futtrack.presentation.video

import com.rodrigo.androidapp.futtrack.domain.model.Video
import com.rodrigo.androidapp.futtrack.domain.model.VideoPage
import com.rodrigo.androidapp.futtrack.domain.repository.VideoRepository
import com.rodrigo.androidapp.futtrack.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VideoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial load emits first video page`() = runTest {
        val repository = FakeVideoRepository().apply {
            enqueueSuccess(
                cursor = null,
                page = VideoPage(
                    videos = listOf(video1, video2),
                    nextCursor = "page-2"
                )
            )
        }

        val viewModel = VideoViewModel(repository)

        assertEquals(
            VideoUiState.Loading,
            viewModel.uiState.value
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state is VideoUiState.Success)

        state as VideoUiState.Success

        assertEquals(
            listOf(video1, video2),
            state.videos
        )
        assertTrue(state.hasNextPage)
        assertFalse(state.isLoadingMore)
        assertEquals(null, state.loadMoreErrorMessage)

        assertEquals(
            listOf<String?>(null),
            repository.requestedCursors
        )
    }

    @Test
    fun `load next page appends videos to current list`() = runTest {
        val repository = FakeVideoRepository().apply {
            enqueueSuccess(
                cursor = null,
                page = VideoPage(
                    videos = listOf(video1, video2),
                    nextCursor = "page-2"
                )
            )

            enqueueSuccess(
                cursor = "page-2",
                page = VideoPage(
                    videos = listOf(video3, video4),
                    nextCursor = "page-3"
                )
            )
        }

        val viewModel = VideoViewModel(repository)

        advanceUntilIdle()

        viewModel.loadNextPage()

        val loadingState = viewModel.uiState.value

        assertTrue(loadingState is VideoUiState.Success)

        loadingState as VideoUiState.Success

        assertEquals(
            listOf(video1, video2),
            loadingState.videos
        )
        assertTrue(loadingState.isLoadingMore)

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state is VideoUiState.Success)

        state as VideoUiState.Success

        assertEquals(
            listOf(video1, video2, video3, video4),
            state.videos
        )
        assertTrue(state.hasNextPage)
        assertFalse(state.isLoadingMore)

        assertEquals(
            listOf(null, "page-2"),
            repository.requestedCursors
        )
    }

    @Test
    fun `load next page removes duplicated videos`() = runTest {
        val repository = FakeVideoRepository().apply {
            enqueueSuccess(
                cursor = null,
                page = VideoPage(
                    videos = listOf(video1, video2),
                    nextCursor = "page-2"
                )
            )

            enqueueSuccess(
                cursor = "page-2",
                page = VideoPage(
                    videos = listOf(video2, video3),
                    nextCursor = null
                )
            )
        }

        val viewModel = VideoViewModel(repository)

        advanceUntilIdle()

        viewModel.loadNextPage()

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state is VideoUiState.Success)

        state as VideoUiState.Success

        assertEquals(
            listOf(video1, video2, video3),
            state.videos
        )
        assertFalse(state.hasNextPage)
    }

    @Test
    fun `load next page does nothing when there is no next page`() = runTest {
        val repository = FakeVideoRepository().apply {
            enqueueSuccess(
                cursor = null,
                page = VideoPage(
                    videos = listOf(video1, video2),
                    nextCursor = null
                )
            )
        }

        val viewModel = VideoViewModel(repository)

        advanceUntilIdle()

        viewModel.loadNextPage()

        advanceUntilIdle()

        assertEquals(
            listOf<String?>(null),
            repository.requestedCursors
        )

        val state = viewModel.uiState.value

        assertTrue(state is VideoUiState.Success)

        state as VideoUiState.Success

        assertFalse(state.hasNextPage)
        assertFalse(state.canLoadMore)
    }

    @Test
    fun `initial load failure emits error state`() = runTest {
        val repository = FakeVideoRepository().apply {
            enqueueFailure(
                cursor = null,
                throwable = IllegalStateException(
                    "Network unavailable"
                )
            )
        }

        val viewModel = VideoViewModel(repository)

        advanceUntilIdle()

        assertEquals(
            VideoUiState.Error(
                message = "Network unavailable"
            ),
            viewModel.uiState.value
        )
    }

    @Test
    fun `load next page failure preserves existing videos`() = runTest {
        val repository = FakeVideoRepository().apply {
            enqueueSuccess(
                cursor = null,
                page = VideoPage(
                    videos = listOf(video1, video2),
                    nextCursor = "page-2"
                )
            )

            enqueueFailure(
                cursor = "page-2",
                throwable = IllegalStateException(
                    "Pagination failed"
                )
            )
        }

        val viewModel = VideoViewModel(repository)

        advanceUntilIdle()

        viewModel.loadNextPage()

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state is VideoUiState.Success)

        state as VideoUiState.Success

        assertEquals(
            listOf(video1, video2),
            state.videos
        )
        assertFalse(state.isLoadingMore)
        assertTrue(state.hasNextPage)
        assertEquals(
            "Pagination failed",
            state.loadMoreErrorMessage
        )
        assertFalse(state.canLoadMore)
    }

    @Test
    fun `retry load next page retries cursor and appends videos`() = runTest {
        val repository = FakeVideoRepository().apply {
            enqueueSuccess(
                cursor = null,
                page = VideoPage(
                    videos = listOf(video1, video2),
                    nextCursor = "page-2"
                )
            )

            enqueueFailure(
                cursor = "page-2",
                throwable = IllegalStateException(
                    "Temporary failure"
                )
            )

            enqueueSuccess(
                cursor = "page-2",
                page = VideoPage(
                    videos = listOf(video3, video4),
                    nextCursor = null
                )
            )
        }

        val viewModel = VideoViewModel(repository)

        advanceUntilIdle()

        viewModel.loadNextPage()

        advanceUntilIdle()

        val failedState = viewModel.uiState.value

        assertTrue(failedState is VideoUiState.Success)

        failedState as VideoUiState.Success

        assertEquals(
            "Temporary failure",
            failedState.loadMoreErrorMessage
        )

        viewModel.retryLoadNextPage()

        advanceUntilIdle()

        val recoveredState = viewModel.uiState.value

        assertTrue(recoveredState is VideoUiState.Success)

        recoveredState as VideoUiState.Success

        assertEquals(
            listOf(video1, video2, video3, video4),
            recoveredState.videos
        )
        assertFalse(recoveredState.hasNextPage)
        assertFalse(recoveredState.isLoadingMore)
        assertEquals(
            null,
            recoveredState.loadMoreErrorMessage
        )

        assertEquals(
            listOf(
                null,
                "page-2",
                "page-2"
            ),
            repository.requestedCursors
        )
    }

    private class FakeVideoRepository : VideoRepository {

        private val responses =
            mutableMapOf<String?, ArrayDeque<Result<VideoPage>>>()

        val requestedCursors = mutableListOf<String?>()

        override suspend fun getVideos(
            cursor: String?
        ): Result<VideoPage> {
            requestedCursors += cursor

            val responseQueue = responses[cursor]
                ?: error(
                    "No response configured for cursor: $cursor"
                )

            return responseQueue.removeFirstOrNull()
                ?: error(
                    "No remaining response for cursor: $cursor"
                )
        }

        fun enqueueSuccess(
            cursor: String?,
            page: VideoPage
        ) {
            enqueue(
                cursor = cursor,
                result = Result.success(page)
            )
        }

        fun enqueueFailure(
            cursor: String?,
            throwable: Throwable
        ) {
            enqueue(
                cursor = cursor,
                result = Result.failure(throwable)
            )
        }

        private fun enqueue(
            cursor: String?,
            result: Result<VideoPage>
        ) {
            responses
                .getOrPut(cursor) {
                    ArrayDeque()
                }
                .addLast(result)
        }
    }

    private companion object {

        val video1 = Video(
            id = "video-1",
            title = "Video 1",
            description = "Description 1",
            thumbnailUrl = "thumbnail-1",
            publishedAt = "2026-08-01"
        )

        val video2 = Video(
            id = "video-2",
            title = "Video 2",
            description = "Description 2",
            thumbnailUrl = "thumbnail-2",
            publishedAt = "2026-08-02"
        )

        val video3 = Video(
            id = "video-3",
            title = "Video 3",
            description = "Description 3",
            thumbnailUrl = "thumbnail-3",
            publishedAt = "2026-08-03"
        )

        val video4 = Video(
            id = "video-4",
            title = "Video 4",
            description = "Description 4",
            thumbnailUrl = "thumbnail-4",
            publishedAt = "2026-08-04"
        )
    }
}