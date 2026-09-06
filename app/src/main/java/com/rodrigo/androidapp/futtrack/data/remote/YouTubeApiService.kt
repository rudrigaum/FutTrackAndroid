package com.rodrigo.androidapp.futtrack.data.remote

import com.rodrigo.androidapp.futtrack.data.remote.dto.YouTubePlaylistResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {

    @GET("playlistItems")
    suspend fun fetchLatestVideos(
        @Query("key") apiKey: String,
        @Query("playlistId") playlistId: String,
        @Query("part") part: String,
        @Query("maxResults") maxResults: Int = 20,
        @Query("pageToken") pageToken: String? = null
    ): YouTubePlaylistResponse
}