package com.rodrigo.androidapp.futtrack.presentation.video

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rodrigo.androidapp.futtrack.domain.model.Video
import com.rodrigo.androidapp.futtrack.presentation.video.components.VideoCard

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
        onRetryClick = { viewModel.fetchVideos() },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreenContent(
    uiState: VideoUiState,
    onVideoClick: (Video) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Vídeos do Baba") })
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is VideoUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is VideoUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = uiState.videos,
                            key = { video -> video.id }
                        ) { video ->
                            VideoCard(
                                video = video,
                                onVideoClick = onVideoClick
                            )
                        }
                    }
                }
                is VideoUiState.Error -> {
                    Box(contentAlignment = Alignment.Center) {
                        Button(onClick = onRetryClick) {
                            Text(text = "Erro: ${uiState.message}. Tentar Novamente")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VideoScreenSuccessPreview() {
    val mockVideos = listOf(
        Video("1", "Brasil 3 x 1 Itália - Melhores Momentos", "Resumo completo da partida.", "https://img.youtube.com/vi/dQw4w9WgXcQ/0.jpg", "2026-06-30"),
        Video("2", "Defesas Incríveis do Baba", "Goleiros fechando o gol.", "https://img.youtube.com/vi/dQw4w9WgXcQ/0.jpg", "2026-06-29")
    )
    VideoScreenContent(
        uiState = VideoUiState.Success(mockVideos),
        onVideoClick = {},
        onRetryClick = {}
    )
}