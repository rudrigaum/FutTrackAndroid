package com.rodrigo.androidapp.futtrack.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class YouTubePlaylistResponse(
    val items: List<PlaylistItemDto>
)

@Serializable
data class PlaylistItemDto(
    val snippet: PlaylistSnippetDto
)

@Serializable
data class PlaylistSnippetDto(
    val title: String,
    val description: String,
    val thumbnails: PlaylistThumbnailsDto,
    val publishedAt: String,
    val resourceId: PlaylistResourceIdDto
)

@Serializable
data class PlaylistThumbnailsDto(
    val high: PlaylistThumbnailDetailsDto
)

@Serializable
data class PlaylistThumbnailDetailsDto(
    val url: String
)

@Serializable
data class PlaylistResourceIdDto(
    val videoId: String
)