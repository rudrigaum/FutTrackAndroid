package com.rodrigo.androidapp.futtrack.di

import com.rodrigo.androidapp.futtrack.data.local.dao.MatchDao
import com.rodrigo.androidapp.futtrack.data.repository.MatchRepositoryImpl
import com.rodrigo.androidapp.futtrack.domain.repository.MatchRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MatchModule {

    @Provides
    @Singleton
    fun provideMatchRepository(dao: MatchDao): MatchRepository {
        return MatchRepositoryImpl(dao)
    }
}