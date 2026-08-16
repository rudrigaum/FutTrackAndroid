package com.rodrigo.androidapp.futtrack.presentation.video

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rodrigo.androidapp.futtrack.domain.model.Video
import com.rodrigo.androidapp.futtrack.presentation.components.FutTrackTopAppBar
import com.rodrigo.androidapp.futtrack.presentation.video.components.VideoCard
import com.rodrigo.androidapp.futtrack.presentation.video.components.YouTubePlayer
import com.rodrigo.androidapp.futtrack.ui.theme.FutTrackTheme

@Composable
fun VideoScreen(
    viewModel: VideoViewModel,
    onVideoClick: (Video) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    VideoScreenContent(
        uiState = uiState,
        onVideoClick = onVideoClick,
        onRetryClick = viewModel::fetchVideos,
        modifier = modifier
    )
}

@Composable
fun VideoScreenContent(
    uiState: VideoUiState,
    onVideoClick: (Video) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeVideoId by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            FutTrackTopAppBar(title = "Vídeos do Baba")
        }
    ) { paddingValues ->
        VideoScreenState(
            uiState = uiState,
            activeVideoId = activeVideoId,
            onPlayClick = { video ->
                activeVideoId = video.id
            },
            onOpenExternallyClick = onVideoClick,
            onRetryClick = onRetryClick,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

@Composable
private fun VideoScreenState(
    uiState: VideoUiState,
    activeVideoId: String?,
    onPlayClick: (Video) -> Unit,
    onOpenExternallyClick: (Video) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        VideoUiState.Loading -> {
            VideoLoadingContent(modifier = modifier)
        }

        is VideoUiState.Success -> {
            VideoSuccessContent(
                videos = uiState.videos,
                activeVideoId = activeVideoId,
                onPlayClick = onPlayClick,
                onOpenExternallyClick = onOpenExternallyClick,
                modifier = modifier
            )
        }

        is VideoUiState.Error -> {
            VideoErrorContent(
                message = uiState.message,
                onRetryClick = onRetryClick,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun VideoLoadingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun VideoSuccessContent(
    videos: List<Video>,
    activeVideoId: String?,
    onPlayClick: (Video) -> Unit,
    onOpenExternallyClick: (Video) -> Unit,
    modifier: Modifier = Modifier
) {
    if (videos.isEmpty()) {
        VideoEmptyContent(modifier = modifier)
        return
    }

    VideoListContent(
        videos = videos,
        activeVideoId = activeVideoId,
        onPlayClick = onPlayClick,
        onOpenExternallyClick = onOpenExternallyClick,
        modifier = modifier
    )
}

@Composable
private fun VideoListContent(
    videos: List<Video>,
    activeVideoId: String?,
    onPlayClick: (Video) -> Unit,
    onOpenExternallyClick: (Video) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 20.dp,
            end = 16.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            VideoCatalogHeader(videoCount = videos.size)
        }

        items(
            items = videos,
            key = Video::id
        ) { video ->
            VideoCard(
                video = video,
                isPlaying = video.id == activeVideoId,
                onPlayClick = onPlayClick,
                playerContent = {
                    VideoPlayerContent(
                        video = video,
                        onOpenExternallyClick = onOpenExternallyClick
                    )
                }
            )
        }
    }
}

@Composable
private fun VideoCatalogHeader(
    videoCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Últimos vídeos",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Melhores momentos, gols e lances do Baba Amigos do Lelé",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = videoCount.toVideoCountLabel(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun VideoPlayerContent(
    video: Video,
    onOpenExternallyClick: (Video) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        YouTubePlayer(
            videoId = video.id,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        )

        TextButton(
            onClick = {
                onOpenExternallyClick(video)
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(text = "Abrir no YouTube")
        }
    }
}

@Composable
private fun VideoEmptyContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Nenhum vídeo disponível no momento.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VideoErrorContent(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge
            )

            Button(onClick = onRetryClick) {
                Text(text = "Tentar novamente")
            }
        }
    }
}

private fun Int.toVideoCountLabel(): String {
    return when (this) {
        1 -> "1 vídeo"
        else -> "$this vídeos"
    }
}

@Preview(
    name = "Videos - Loading",
    showBackground = true
)
@Composable
private fun VideoScreenLoadingPreview() {
    FutTrackTheme {
        VideoScreenContent(
            uiState = VideoUiState.Loading,
            onVideoClick = {},
            onRetryClick = {}
        )
    }
}

@Preview(
    name = "Videos - Success",
    showBackground = true
)
@Composable
private fun VideoScreenSuccessPreview() {
    FutTrackTheme {
        VideoScreenContent(
            uiState = VideoUiState.Success(
                videos = previewVideos
            ),
            onVideoClick = {},
            onRetryClick = {}
        )
    }
}

@Preview(
    name = "Videos - Empty",
    showBackground = true
)
@Composable
private fun VideoScreenEmptyPreview() {
    FutTrackTheme {
        VideoScreenContent(
            uiState = VideoUiState.Success(
                videos = emptyList()
            ),
            onVideoClick = {},
            onRetryClick = {}
        )
    }
}

@Preview(
    name = "Videos - Error",
    showBackground = true
)
@Composable
private fun VideoScreenErrorPreview() {
    FutTrackTheme {
        VideoScreenContent(
            uiState = VideoUiState.Error(
                message = "Não foi possível carregar os vídeos."
            ),
            onVideoClick = {},
            onRetryClick = {}
        )
    }
}

private val previewVideos = listOf(
    Video(
        id = "1",
        title = "Brasil 3 x 1 Itália - Melhores Momentos",
        description = "Resumo completo da partida e os principais lances da rodada.",
        thumbnailUrl = "https://img.youtube.com/vi/dQw4w9WgXcQ/0.jpg",
        publishedAt = "2026-06-30"
    ),
    Video(
        id = "2",
        title = "Defesas Incríveis do Baba",
        description = "As melhores defesas dos goleiros durante a rodada.",
        thumbnailUrl = "https://img.youtube.com/vi/dQw4w9WgXcQ/0.jpg",
        publishedAt = "2026-06-29"
    )
)