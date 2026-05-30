package com.rodrigo.androidapp.futtrack.di

import com.rodrigo.androidapp.futtrack.data.local.dao.TeamDao
import com.rodrigo.androidapp.futtrack.data.repository.TeamRepositoryImpl
import com.rodrigo.androidapp.futtrack.domain.repository.TeamRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TeamModule {

    @Provides
    @Singleton
    fun provideTeamRepository(dao: TeamDao): TeamRepository {
        return TeamRepositoryImpl(dao)
    }
}