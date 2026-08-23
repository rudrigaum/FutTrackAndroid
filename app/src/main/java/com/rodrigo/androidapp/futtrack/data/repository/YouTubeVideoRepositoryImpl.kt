package com.rodrigo.androidapp.futtrack.data.repository

import android.util.Log
import com.rodrigo.androidapp.futtrack.BuildConfig
import com.rodrigo.androidapp.futtrack.data.remote.YouTubeApiService
import com.rodrigo.androidapp.futtrack.domain.model.Video
import com.rodrigo.androidapp.futtrack.domain.model.VideoPage
import com.rodrigo.androidapp.futtrack.domain.repository.VideoRepository
import javax.inject.Inject

class YouTubeVideoRepositoryImpl @Inject constructor(
    private val apiService: YouTubeApiService
) : VideoRepository {

    private val apiKey = BuildConfig.YOUTUBE_API_KEY

    override suspend fun getVideos(
        cursor: String?
    ): Result<VideoPage> {
        return runCatching {
            val response = apiService.fetchLatestVideos(
                apiKey = apiKey,
                playlistId = uploadsPlaylistId,
                part = SNIPPET_PART,
                maxResults = PAGE_SIZE,
                pageToken = cursor
            )

            VideoPage(
                videos = response.items
                    .map { item ->
                        Video(
                            id = item.snippet.resourceId.videoId,
                            title = item.snippet.title,
                            description = item.snippet.description,
                            thumbnailUrl = item.snippet.thumbnails.high.url,
                            publishedAt = item.snippet.publishedAt
                        )
                    }
                    .sortedByDescending(Video::publishedAt),
                nextCursor = response.nextPageToken
            )
        }.onFailure { throwable ->
            Log.e(
                LOG_TAG,
                "Failed to fetch YouTube uploads",
                throwable
            )
        }
    }

    private companion object {
        const val CHANNEL_ID = "UC0btwBPqDLs8r9zFTcLAQiw"
        const val PAGE_SIZE = 20
        const val SNIPPET_PART = "snippet"
        const val LOG_TAG = "FUTTRACK_NET"

        val uploadsPlaylistId: String
            get() = CHANNEL_ID.replaceFirst(
                oldValue = "UC",
                newValue = "UU"
            )
    }
}