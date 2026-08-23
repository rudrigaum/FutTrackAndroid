package com.rodrigo.androidapp.futtrack.presentation.video.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.rodrigo.androidapp.futtrack.domain.model.Video
import com.rodrigo.androidapp.futtrack.ui.theme.FutTrackTheme

@Composable
fun VideoPlayerContent(
    video: Video,
    onOpenExternallyClick: (Video) -> Unit,
    modifier: Modifier = Modifier
) {
    VideoPlayerLayout(
        onOpenExternallyClick = {
            onOpenExternallyClick(video)
        },
        modifier = modifier,
        playerContent = {
            YouTubePlayer(
                videoId = video.id,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(VIDEO_ASPECT_RATIO)
            )
        }
    )
}

@Composable
private fun VideoPlayerLayout(
    onOpenExternallyClick: () -> Unit,
    playerContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PLAYER_CONTENT_SPACING)
    ) {
        playerContent()

        TextButton(
            onClick = onOpenExternallyClick,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(text = "Abrir no YouTube")
        }
    }
}

@Preview(
    name = "Video Player Content",
    showBackground = true
)
@Composable
private fun VideoPlayerContentPreview() {
    FutTrackTheme {
        VideoPlayerLayout(
            onOpenExternallyClick = {},
            playerContent = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(VIDEO_ASPECT_RATIO),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Embedded video player",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        )
    }
}

private const val VIDEO_ASPECT_RATIO = 16f / 9f
private val PLAYER_CONTENT_SPACING = 4.dp