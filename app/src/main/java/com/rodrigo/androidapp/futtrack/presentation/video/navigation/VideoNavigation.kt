package com.rodrigo.androidapp.futtrack.presentation.video.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.rodrigo.androidapp.futtrack.domain.model.Video
import com.rodrigo.androidapp.futtrack.presentation.video.VideoScreen
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.serialization.Serializable

@Serializable
data object VideoRoute

fun NavController.navigateToVideo(navOptions: NavOptions? = null) {
    navigate(VideoRoute, navOptions)
}

fun NavGraphBuilder.videoScreen(
    onVideoClick: (Video) -> Unit
) {
    composable<VideoRoute> {
        val viewModel: com.rodrigo.androidapp.futtrack.presentation.video.VideoViewModel = hiltViewModel()

        VideoScreen(
            viewModel = viewModel,
            onVideoClick = onVideoClick
        )
    }
}