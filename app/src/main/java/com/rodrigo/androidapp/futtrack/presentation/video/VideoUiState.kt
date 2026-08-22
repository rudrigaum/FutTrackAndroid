package com.rodrigo.androidapp.futtrack.presentation.video

import com.rodrigo.androidapp.futtrack.domain.model.Video

sealed interface VideoUiState {

    data object Loading : VideoUiState

    data class Success(
        val videos: List<Video>,
        val isLoadingMore: Boolean = false,
        val hasNextPage: Boolean = true,
        val loadMoreErrorMessage: String? = null
    ) : VideoUiState {

        val canLoadMore: Boolean
            get() = hasNextPage &&
                    !isLoadingMore &&
                    loadMoreErrorMessage == null
    }

    data class Error(
        val message: String
    ) : VideoUiState
}