package com.rodrigo.androidapp.futtrack.presentation.video

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.rodrigo.androidapp.futtrack.presentation.video.components.VideoListContent
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
        onLoadMore = viewModel::loadNextPage,
        onRetryLoadMore = viewModel::retryLoadNextPage,
        modifier = modifier
    )
}

@Composable
fun VideoScreenContent(
    uiState: VideoUiState,
    onVideoClick: (Video) -> Unit,
    onRetryClick: () -> Unit,
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeVideoId by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            FutTrackTopAppBar(
                title = "Vídeos do Baba"
            )
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
            onLoadMore = onLoadMore,
            onRetryLoadMore = onRetryLoadMore,
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
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        VideoUiState.Loading -> {
            VideoLoadingContent(
                modifier = modifier
            )
        }

        is VideoUiState.Success -> {
            VideoSuccessContent(
                state = uiState,
                activeVideoId = activeVideoId,
                onPlayClick = onPlayClick,
                onOpenExternallyClick = onOpenExternallyClick,
                onLoadMore = onLoadMore,
                onRetryLoadMore = onRetryLoadMore,
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
private fun VideoSuccessContent(
    state: VideoUiState.Success,
    activeVideoId: String?,
    onPlayClick: (Video) -> Unit,
    onOpenExternallyClick: (Video) -> Unit,
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.videos.isEmpty()) {
        VideoEmptyContent(
            modifier = modifier
        )
        return
    }

    VideoListContent(
        videos = state.videos,
        activeVideoId = activeVideoId,
        isLoadingMore = state.isLoadingMore,
        canLoadMore = state.canLoadMore,
        loadMoreErrorMessage = state.loadMoreErrorMessage,
        onPlayClick = onPlayClick,
        onOpenExternallyClick = onOpenExternallyClick,
        onLoadMore = onLoadMore,
        onRetryLoadMore = onRetryLoadMore,
        modifier = modifier
    )
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

            Button(
                onClick = onRetryClick
            ) {
                Text(
                    text = "Tentar novamente"
                )
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
            onRetryClick = {},
            onLoadMore = {},
            onRetryLoadMore = {}
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
                videos = previewVideos,
                hasNextPage = true
            ),
            onVideoClick = {},
            onRetryClick = {},
            onLoadMore = {},
            onRetryLoadMore = {}
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
                videos = emptyList(),
                hasNextPage = false
            ),
            onVideoClick = {},
            onRetryClick = {},
            onLoadMore = {},
            onRetryLoadMore = {}
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
            onRetryClick = {},
            onLoadMore = {},
            onRetryLoadMore = {}
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