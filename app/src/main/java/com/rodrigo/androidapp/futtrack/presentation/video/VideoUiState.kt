package com.rodrigo.androidapp.futtrack.presentation.video

import com.rodrigo.androidapp.futtrack.domain.model.Video

sealed interface VideoUiState {

    data object Loading : VideoUiState

    data class Success(val videos: List<Video>) : VideoUiState

    data class Error(val message: String) : VideoUiState
}