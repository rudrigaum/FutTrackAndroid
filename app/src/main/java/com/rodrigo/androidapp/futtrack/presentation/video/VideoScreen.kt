package com.rodrigo.androidapp.futtrack.presentation.video

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rodrigo.androidapp.futtrack.domain.model.Video
import com.rodrigo.androidapp.futtrack.presentation.components.FutTrackTopAppBar
import com.rodrigo.androidapp.futtrack.presentation.video.components.VideoCard
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
            VideoListContent(
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
private fun VideoListContent(
    videos: List<Video>,
    activeVideoId: String?,
    onPlayClick: (Video) -> Unit,
    onOpenExternallyClick: (Video) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = videos,
            key = Video::id
        ) { video ->
            val isPlaying = video.id == activeVideoId

            VideoCard(
                video = video,
                isPlaying = isPlaying,
                onPlayClick = onPlayClick,
                playerContent = {
                    VideoPlayerPlaceholder(
                        video = video,
                        onOpenExternallyClick = onOpenExternallyClick
                    )
                }
            )
        }
    }
}

@Composable
private fun VideoPlayerPlaceholder(
    video: Video,
    onOpenExternallyClick: (Video) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator()

            Text(
                text = "Preparando player...",
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = {
                    onOpenExternallyClick(video)
                }
            ) {
                Text(text = "Abrir no YouTube")
            }
        }
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
        description = "Resumo completo da partida.",
        thumbnailUrl = "https://img.youtube.com/vi/dQw4w9WgXcQ/0.jpg",
        publishedAt = "2026-06-30"
    ),
    Video(
        id = "2",
        title = "Defesas Incríveis do Baba",
        description = "Goleiros fechando o gol.",
        thumbnailUrl = "https://img.youtube.com/vi/dQw4w9WgXcQ/0.jpg",
        publishedAt = "2026-06-29"
    )
)