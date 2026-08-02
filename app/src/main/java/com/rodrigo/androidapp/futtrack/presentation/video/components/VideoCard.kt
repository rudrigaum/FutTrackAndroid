package com.rodrigo.androidapp.futtrack.presentation.video.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rodrigo.androidapp.futtrack.domain.model.Video
import com.rodrigo.androidapp.futtrack.ui.theme.FutTrackTheme

@Composable
fun VideoCard(
    video: Video,
    isPlaying: Boolean,
    onPlayClick: (Video) -> Unit,
    modifier: Modifier = Modifier,
    playerContent: @Composable () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            VideoMediaContent(
                video = video,
                isPlaying = isPlaying,
                onPlayClick = onPlayClick,
                playerContent = playerContent
            )

            VideoInformation(video = video)
        }
    }
}

@Composable
private fun VideoMediaContent(
    video: Video,
    isPlaying: Boolean,
    onPlayClick: (Video) -> Unit,
    playerContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isPlaying) {
            playerContent()
        } else {
            VideoThumbnail(
                video = video,
                onPlayClick = onPlayClick
            )
        }
    }
}

@Composable
private fun VideoThumbnail(
    video: Video,
    onPlayClick: (Video) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = video.thumbnailUrl,
            contentDescription = "Thumbnail do vídeo ${video.title}",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentScale = ContentScale.Crop
        )

        FilledIconButton(
            onClick = { onPlayClick(video) }
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Reproduzir ${video.title}"
            )
        }
    }
}

@Composable
private fun VideoInformation(
    video: Video,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp)
    ) {
        Text(
            text = video.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = video.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = DESCRIPTION_ALPHA
            ),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(
    name = "Video Card - Thumbnail",
    showBackground = true
)
@Composable
private fun VideoCardThumbnailPreview() {
    FutTrackTheme {
        VideoCard(
            video = previewVideo,
            isPlaying = false,
            onPlayClick = {}
        )
    }
}

@Preview(
    name = "Video Card - Playing",
    showBackground = true
)
@Composable
private fun VideoCardPlayingPreview() {
    FutTrackTheme {
        VideoCard(
            video = previewVideo,
            isPlaying = true,
            onPlayClick = {},
            playerContent = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Embedded video player")
                }
            }
        )
    }
}

private const val DESCRIPTION_ALPHA = 0.8f

private val previewVideo = Video(
    id = "123",
    title = "Melhores Lances do Baba Amigos do Lelé - Junho 2026",
    description = "Grandes defesas do goleiro Digo e os gols da rodada decisiva.",
    thumbnailUrl = "https://img.youtube.com/vi/dQw4w9WgXcQ/0.jpg",
    publishedAt = "2026-06-30"
)