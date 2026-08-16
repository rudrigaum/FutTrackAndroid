package com.rodrigo.androidapp.futtrack.presentation.video.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rodrigo.androidapp.futtrack.domain.model.Video
import com.rodrigo.androidapp.futtrack.ui.theme.FutTrackTheme
import java.time.LocalDate
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
        VideoMediaContent(
            video = video,
            isPlaying = isPlaying,
            onPlayClick = onPlayClick,
            playerContent = playerContent
        )

        VideoInformation(video = video)
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
    if (isPlaying) {
        Box(
            modifier = modifier.fillMaxWidth()
        ) {
            playerContent()
        }
    } else {
        VideoThumbnail(
            video = video,
            onPlayClick = onPlayClick,
            modifier = modifier
        )
    }
}

@Composable
private fun VideoThumbnail(
    video: Video,
    onPlayClick: (Video) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(VIDEO_ASPECT_RATIO)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable {
                onPlayClick(video)
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = video.thumbnailUrl,
            contentDescription = "Thumbnail do vídeo ${video.title}",
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop
        )

        VideoPlayButton(
            title = video.title,
            onClick = {
                onPlayClick(video)
            }
        )
    }
}

@Composable
private fun VideoPlayButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledIconButton(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Reproduzir $title",
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun VideoInformation(
    video: Video,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = video.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "Publicado em ${video.publishedAt.toDisplayDate()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (video.description.isNotBlank()) {
            Text(
                text = video.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun String.toDisplayDate(): String {
    return runCatching {
        LocalDate
            .parse(this)
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }.getOrDefault(this)
}

@Preview(
    name = "Video Card - Thumbnail",
    showBackground = true
)
@Composable
private fun VideoCardThumbnailPreview() {
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
                            .aspectRatio(VIDEO_ASPECT_RATIO)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Embedded video player")
                    }
                }
            )
        }
    }
}

private const val VIDEO_ASPECT_RATIO = 16f / 9f

private val previewVideo = Video(
    id = "123",
    title = "Melhores Lances do Baba Amigos do Lelé - Junho 2026",
    description = "Grandes defesas do goleiro Digo e os gols da rodada decisiva.",
    thumbnailUrl = "https://img.youtube.com/vi/dQw4w9WgXcQ/0.jpg",
    publishedAt = "2026-06-30"
)