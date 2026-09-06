package com.rodrigo.androidapp.futtrack.domain.model

data class VideoPage(
    val videos: List<Video>,
    val nextCursor: String? = null
) {
    val hasNextPage: Boolean
        get() = nextCursor != null
}