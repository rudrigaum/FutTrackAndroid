package com.rodrigo.androidapp.futtrack.domain.repository

import com.rodrigo.androidapp.futtrack.domain.model.VideoPage

interface VideoRepository {

    suspend fun getVideos(
        cursor: String? = null
    ): Result<VideoPage>
}