package com.rodrigo.androidapp.futtrack.presentation.video.navigation

object YouTubeUrlBuilder {

    private const val WATCH_URL = "https://www.youtube.com/watch?v="

    fun watchUrl(videoId: String): String {
        return "$WATCH_URL$videoId"
    }
}