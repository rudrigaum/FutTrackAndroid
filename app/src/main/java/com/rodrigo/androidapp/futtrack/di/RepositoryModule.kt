package com.rodrigo.androidapp.futtrack.di

import com.rodrigo.androidapp.futtrack.data.repository.YouTubeVideoRepositoryImpl
import com.rodrigo.androidapp.futtrack.domain.repository.VideoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindVideoRepository(
        youtubeVideoRepositoryImpl: YouTubeVideoRepositoryImpl
    ): VideoRepository
}