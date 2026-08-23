package com.rodrigo.androidapp.futtrack.presentation.video.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rodrigo.androidapp.futtrack.domain.model.Video
import com.rodrigo.androidapp.futtrack.ui.theme.FutTrackTheme
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
fun VideoCard(
    video: Video,
    isPlaying: Boolean,
    onPlayClick: (Video) -> Unit,
    modifier: Modifier = Modifier,
    playerContent: @Composable () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isPlaying) {
            VideoPlayingContent(
                video = video,
                playerContent = playerContent
            )
        } else {
            VideoCatalogItem(
                video = video,
                onPlayClick = onPlayClick
            )
        }
    }
}

@Composable
private fun VideoCatalogItem(
    video: Video,
    onPlayClick: (Video) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClickLabel = "Reproduzir ${video.title}",
                role = Role.Button,
                onClick = {
                    onPlayClick(video)
                }
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        VideoThumbnail(
            video = video
        )

        VideoInformation(
            video = video,
            modifier = Modifier.weight(1f)
        )

        VideoPlayIndicator()
    }
}

@Composable
private fun VideoThumbnail(
    video: Video,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = video.thumbnailUrl,
        contentDescription = "Thumbnail do vídeo ${video.title}",
        modifier = modifier
            .width(132.dp)
            .aspectRatio(VIDEO_ASPECT_RATIO)
            .clip(RoundedCornerShape(10.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun VideoInformation(
    video: Video,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = video.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = video.publishedAt.toDisplayDate(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VideoPlayIndicator(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(44.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary
        )
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun VideoPlayingContent(
    video: Video,
    playerContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        playerContent()

        VideoInformation(
            video = video
        )
    }
}

private fun String.toDisplayDate(): String {
    val date = runCatching {
        OffsetDateTime
            .parse(this)
            .toLocalDate()
    }.recoverCatching {
        LocalDate.parse(this)
    }.getOrNull()

    return date
        ?.format(DISPLAY_DATE_FORMATTER)
        ?: this
}

@Preview(
    name = "Video Card - Catalog Item",
    showBackground = true
)
@Composable
private fun VideoCardCatalogPreview() {
    FutTrackTheme {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            VideoCard(
                video = previewVideo,
                isPlaying = false,
                onPlayClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(
    name = "Video Card - Playing",
    showBackground = true
)
@Composable
private fun VideoCardPlayingPreview() {
    FutTrackTheme {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            VideoCard(
                video = previewVideo,
                isPlaying = true,
                onPlayClick = {},
                modifier = Modifier.padding(16.dp),
                playerContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(VIDEO_ASPECT_RATIO),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Embedded video player"
                        )
                    }
                }
            )
        }
    }
}

private const val VIDEO_ASPECT_RATIO = 16f / 9f

private val DISPLAY_DATE_FORMATTER =
    DateTimeFormatter.ofPattern("dd/MM/yyyy")

private val previewVideo = Video(
    id = "123",
    title = "Campeonato BAL 18/04/2026 Parte 05",
    description = "Grandes defesas e gols da rodada.",
    thumbnailUrl = "https://img.youtube.com/vi/dQw4w9WgXcQ/0.jpg",
    publishedAt = "2026-04-18T17:46:29Z"
)