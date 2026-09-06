package com.rodrigo.androidapp.futtrack.presentation.video.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.rodrigo.androidapp.futtrack.domain.model.Video
import com.rodrigo.androidapp.futtrack.presentation.video.VideoScreen
import com.rodrigo.androidapp.futtrack.presentation.video.VideoViewModel
import com.rodrigo.androidapp.futtrack.presentation.video.player.VideoPlayerScreen
import kotlinx.serialization.Serializable

@Serializable
data object VideoRoute

@Serializable
data class VideoPlayerRoute(
    val videoId: String,
    val title: String
)

fun NavController.navigateToVideo(
    navOptions: NavOptions? = null
) {
    navigate(
        route = VideoRoute,
        navOptions = navOptions
    )
}

fun NavController.navigateToVideoPlayer(
    video: Video
) {
    navigate(
        VideoPlayerRoute(
            videoId = video.id,
            title = video.title
        )
    )
}

fun NavGraphBuilder.videoScreen(
    onVideoClick: (Video) -> Unit
) {
    composable<VideoRoute> {
        val viewModel: VideoViewModel = hiltViewModel()

        VideoScreen(
            viewModel = viewModel,
            onVideoClick = onVideoClick
        )
    }
}

fun NavGraphBuilder.videoPlayerScreen(
    onBackClick: () -> Unit
) {
    composable<VideoPlayerRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<VideoPlayerRoute>()

        VideoPlayerScreen(
            videoId = route.videoId,
            title = route.title,
            onBackClick = onBackClick
        )
    }
}