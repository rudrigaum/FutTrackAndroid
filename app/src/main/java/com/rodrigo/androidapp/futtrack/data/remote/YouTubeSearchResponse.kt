package com.rodrigo.androidapp.futtrack.data.remote.dto

import com.google.gson.annotations.SerializedName

data class YouTubeSearchResponse(
    @SerializedName("items") val items: List<YouTubeSearchItem>
)

data class YouTubeSearchItem(
    @SerializedName("id") val id: YouTubeVideoId,
    @SerializedName("snippet") val snippet: YouTubeSnippet
)

data class YouTubeVideoId(
    @SerializedName("videoId") val videoId: String?
)

data class YouTubeSnippet(
    @SerializedName("publishedAt") val publishedAt: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("thumbnails") val thumbnails: YouTubeThumbnails
)

data class YouTubeThumbnails(
    @SerializedName("high") val high: YouTubeThumbnail
)

data class YouTubeThumbnail(
    @SerializedName("url") val url: String
)