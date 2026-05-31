package com.rodrigo.androidapp.futtrack.di

import com.rodrigo.androidapp.futtrack.data.local.dao.PlayerDao
import com.rodrigo.androidapp.futtrack.data.repository.PlayerRepositoryImpl
import com.rodrigo.androidapp.futtrack.domain.repository.PlayerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {

    @Provides
    @Singleton
    fun providePlayerRepository(dao: PlayerDao): PlayerRepository {
        return PlayerRepositoryImpl(dao)
    }
}