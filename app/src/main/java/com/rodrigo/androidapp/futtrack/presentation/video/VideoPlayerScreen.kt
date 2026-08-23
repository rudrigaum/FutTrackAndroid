package com.rodrigo.androidapp.futtrack.presentation.video.player

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rodrigo.androidapp.futtrack.presentation.video.components.YouTubePlayer
import com.rodrigo.androidapp.futtrack.ui.theme.FutTrackTheme

@Composable
fun VideoPlayerScreen(
    videoId: String,
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LockPlayerOrientation()

    VideoPlayerScreenContent(
        title = title,
        onBackClick = onBackClick,
        modifier = modifier,
        playerContent = {
            YouTubePlayer(
                videoId = videoId,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(VIDEO_ASPECT_RATIO)
            )
        }
    )
}

@Composable
private fun LockPlayerOrientation() {
    val activity = LocalContext.current.findActivity()

    DisposableEffect(activity) {
        val previousOrientation = activity?.requestedOrientation

        activity?.requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        onDispose {
            if (previousOrientation != null) {
                activity.requestedOrientation = previousOrientation
            }
        }
    }
}

@Composable
private fun VideoPlayerScreenContent(
    title: String,
    onBackClick: () -> Unit,
    playerContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        playerContent()

        VideoPlayerBackButton(
            title = title,
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )
    }
}

@Composable
private fun VideoPlayerBackButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = Color.Black.copy(
            alpha = BACKGROUND_ALPHA
        )
    ) {
        IconButton(
            onClick = onClick
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Voltar de $title",
                tint = Color.White
            )
        }
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this

        is ContextWrapper -> {
            baseContext.findActivity()
        }

        else -> null
    }
}

@Preview(
    name = "Video Player Screen",
    showBackground = true
)
@Composable
private fun VideoPlayerScreenPreview() {
    FutTrackTheme {
        VideoPlayerScreenContent(
            title = "Campeonato BAL - Brasil x Itália",
            onBackClick = {},
            playerContent = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(VIDEO_ASPECT_RATIO)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        )
    }
}

private const val VIDEO_ASPECT_RATIO = 16f / 9f
private const val BACKGROUND_ALPHA = 0.55f