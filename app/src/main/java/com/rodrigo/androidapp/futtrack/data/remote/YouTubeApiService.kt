package com.rodrigo.androidapp.futtrack.data.remote

import com.rodrigo.androidapp.futtrack.data.remote.dto.YouTubeSearchResponse
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface YouTubeApiService {

    @Headers(
        "X-Android-Package: com.rodrigo.androidapp.futtrack",
        "X-Android-Cert: FA:7D:51:20:1B:C1:9B:44:6B:EE:93:93:34:6A:45:DE:E3:BA:A5:B9"
    )
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