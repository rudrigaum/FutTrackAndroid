package com.rodrigo.androidapp.futtrack.domain.repository

import com.rodrigo.androidapp.futtrack.domain.model.Video

interface VideoRepository {
    suspend fun getVideos(): Result<List<Video>>
}


