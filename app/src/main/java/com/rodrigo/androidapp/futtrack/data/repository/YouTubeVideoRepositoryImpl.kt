package com.rodrigo.androidapp.futtrack.data.repository

import android.util.Log
import com.rodrigo.androidapp.futtrack.BuildConfig
import com.rodrigo.androidapp.futtrack.data.remote.YouTubeApiService
import com.rodrigo.androidapp.futtrack.domain.model.Video
import com.rodrigo.androidapp.futtrack.domain.repository.VideoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

class YouTubeVideoRepositoryImpl @Inject constructor(
    private val apiService: YouTubeApiService
) : VideoRepository {

    private val channelIds = listOf("UC0btwBPqDLs8r9zFTcLAQiw")
    private val apiKey = BuildConfig.YOUTUBE_API_KEY

    override suspend fun getVideos(): Result<List<Video>> = withContext(Dispatchers.IO) {
        try {
            supervisorScope {
                val deferredVideos = channelIds.map { channelId ->
                    val uploadsPlaylistId = if (channelId.startsWith("UC")) {
                        channelId.replaceFirst("UC", "UU")
                    } else {
                        channelId
                    }

                    async {
                        apiService.fetchLatestVideos(
                            apiKey = apiKey,
                            playlistId = uploadsPlaylistId,
                            part = "snippet",
                            maxResults = 15
                        )
                    }
                }

                val responses = deferredVideos.awaitAll()

                val allVideos = responses.flatMap { response ->
                    response.items.map { item ->
                        Video(
                            id = item.snippet.resourceId.videoId,
                            title = item.snippet.title,
                            description = item.snippet.description,
                            thumbnailUrl = item.snippet.thumbnails.high.url,
                            publishedAt = item.snippet.publishedAt
                        )
                    }
                }.sortedByDescending { it.publishedAt }

                Result.success(allVideos)
            }
        } catch (e: Exception) {
            Log.e("FUTTRACK_NET", "Falha ao buscar feed de uploads do YouTube", e)
            Result.failure(e)
        }
    }
}