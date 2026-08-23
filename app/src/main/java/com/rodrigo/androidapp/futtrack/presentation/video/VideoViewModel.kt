package com.rodrigo.androidapp.futtrack.presentation.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigo.androidapp.futtrack.domain.model.Video
import com.rodrigo.androidapp.futtrack.domain.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor(
    private val repository: VideoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<VideoUiState>(
        VideoUiState.Loading
    )
    val uiState: StateFlow<VideoUiState> = _uiState.asStateFlow()

    private var nextCursor: String? = null

    init {
        fetchVideos()
    }

    fun fetchVideos() {
        viewModelScope.launch {
            nextCursor = null
            _uiState.value = VideoUiState.Loading

            repository.getVideos()
                .onSuccess { page ->
                    nextCursor = page.nextCursor

                    _uiState.value = VideoUiState.Success(
                        videos = page.videos,
                        hasNextPage = page.hasNextPage
                    )
                }
                .onFailure { exception ->
                    _uiState.value = VideoUiState.Error(
                        message = exception.localizedMessage
                            ?: DEFAULT_ERROR_MESSAGE
                    )
                }
        }
    }

    fun loadNextPage() {
        val currentState = _uiState.value as? VideoUiState.Success
            ?: return

        if (!currentState.canLoadMore) {
            return
        }

        val cursor = nextCursor ?: return

        _uiState.value = currentState.copy(
            isLoadingMore = true,
            loadMoreErrorMessage = null
        )

        viewModelScope.launch {
            repository.getVideos(cursor)
                .onSuccess { page ->
                    nextCursor = page.nextCursor

                    val latestState =
                        _uiState.value as? VideoUiState.Success
                            ?: return@onSuccess

                    _uiState.value = latestState.copy(
                        videos = mergeVideos(
                            currentVideos = latestState.videos,
                            newVideos = page.videos
                        ),
                        isLoadingMore = false,
                        hasNextPage = page.hasNextPage,
                        loadMoreErrorMessage = null
                    )
                }
                .onFailure { exception ->
                    val latestState =
                        _uiState.value as? VideoUiState.Success
                            ?: return@onFailure

                    _uiState.value = latestState.copy(
                        isLoadingMore = false,
                        loadMoreErrorMessage = exception.localizedMessage
                            ?: DEFAULT_LOAD_MORE_ERROR_MESSAGE
                    )
                }
        }
    }

    fun retryLoadNextPage() {
        val currentState = _uiState.value as? VideoUiState.Success
            ?: return

        if (currentState.isLoadingMore || !currentState.hasNextPage) {
            return
        }

        _uiState.value = currentState.copy(
            loadMoreErrorMessage = null
        )

        loadNextPage()
    }

    private fun mergeVideos(
        currentVideos: List<Video>,
        newVideos: List<Video>
    ): List<Video> {
        return (currentVideos + newVideos)
            .distinctBy(Video::id)
    }

    private companion object {
        const val DEFAULT_ERROR_MESSAGE =
            "Não foi possível carregar os vídeos."

        const val DEFAULT_LOAD_MORE_ERROR_MESSAGE =
            "Não foi possível carregar mais vídeos."
    }
}