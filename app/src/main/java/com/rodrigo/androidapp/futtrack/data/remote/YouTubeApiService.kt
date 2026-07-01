package com.rodrigo.androidapp.futtrack.data.remote

import com.rodrigo.androidapp.futtrack.data.remote.dto.YouTubeSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {

    @GET("search")
    suspend fun fetchLatestVideos(
        @Query("key") apiKey: String,
        @Query("channelId") channelId: String,
        @Query("part") part: String = "snippet",
        @Query("order") order: String = "date",
        @Query("type") type: String = "video",
        @Query("maxResults") maxResults: Int = 15
    ): YouTubeSearchResponse
}