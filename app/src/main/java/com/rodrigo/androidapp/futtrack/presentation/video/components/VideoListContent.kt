package com.rodrigo.androidapp.futtrack.presentation.video.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rodrigo.androidapp.futtrack.domain.model.Video
import com.rodrigo.androidapp.futtrack.ui.theme.FutTrackTheme

@Composable
fun VideoListContent(
    videos: List<Video>,
    activeVideoId: String?,
    isLoadingMore: Boolean,
    canLoadMore: Boolean,
    loadMoreErrorMessage: String?,
    onPlayClick: (Video) -> Unit,
    onOpenExternallyClick: (Video) -> Unit,
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    val shouldLoadMore by remember(
        listState,
        videos.size,
        canLoadMore
    ) {
        derivedStateOf {
            shouldRequestNextPage(
                lastVisibleItemIndex = listState.layoutInfo
                    .visibleItemsInfo
                    .lastOrNull()
                    ?.index
                    ?: -1,
                videoCount = videos.size,
                canLoadMore = canLoadMore
            )
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 12.dp,
            end = 16.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item(
            key = VIDEO_HEADER_KEY
        ) {
            VideoCatalogHeader(
                videoCount = videos.size
            )
        }

        itemsIndexed(
            items = videos,
            key = { _, video ->
                video.id
            }
        ) { index, video ->
            Column {
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

                if (index < videos.lastIndex) {
                    VideoDivider()
                }
            }
        }

        if (
            isLoadingMore ||
            loadMoreErrorMessage != null
        ) {
            item(
                key = VIDEO_PAGINATION_KEY
            ) {
                VideoPaginationFooter(
                    isLoading = isLoadingMore,
                    errorMessage = loadMoreErrorMessage,
                    onRetryClick = onRetryLoadMore
                )
            }
        }
    }
}

@Composable
private fun VideoDivider(
    modifier: Modifier = Modifier
) {
    HorizontalDivider(
        modifier = modifier.padding(
            start = VIDEO_DIVIDER_START_PADDING
        ),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

private fun shouldRequestNextPage(
    lastVisibleItemIndex: Int,
    videoCount: Int,
    canLoadMore: Boolean
): Boolean {
    if (!canLoadMore || videoCount == 0) {
        return false
    }

    val lastVideoItemIndex = videoCount

    return lastVisibleItemIndex >=
            lastVideoItemIndex - LOAD_MORE_THRESHOLD
}

@Preview(
    name = "Video List",
    showBackground = true
)
@Composable
private fun VideoListContentPreview() {
    FutTrackTheme {
        VideoListContent(
            videos = previewVideos,
            activeVideoId = null,
            isLoadingMore = false,
            canLoadMore = true,
            loadMoreErrorMessage = null,
            onPlayClick = {},
            onOpenExternallyClick = {},
            onLoadMore = {},
            onRetryLoadMore = {}
        )
    }
}

@Preview(
    name = "Video List - Loading More",
    showBackground = true
)
@Composable
private fun VideoListLoadingMorePreview() {
    FutTrackTheme {
        VideoListContent(
            videos = previewVideos,
            activeVideoId = null,
            isLoadingMore = true,
            canLoadMore = false,
            loadMoreErrorMessage = null,
            onPlayClick = {},
            onOpenExternallyClick = {},
            onLoadMore = {},
            onRetryLoadMore = {}
        )
    }
}

private const val LOAD_MORE_THRESHOLD = 4
private const val VIDEO_HEADER_KEY = "video_header"
private const val VIDEO_PAGINATION_KEY = "video_pagination"

private val VIDEO_DIVIDER_START_PADDING = 144.dp

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
        description = "As melhores defesas dos goleiros.",
        thumbnailUrl = "https://img.youtube.com/vi/dQw4w9WgXcQ/0.jpg",
        publishedAt = "2026-06-29"
    )
)