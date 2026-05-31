package com.rodrigo.androidapp.futtrack.di

import com.rodrigo.androidapp.futtrack.data.local.dao.TeamDao
import com.rodrigo.androidapp.futtrack.data.local.dao.PlayerDao
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
    fun provideTeamRepository(teamDao: TeamDao, playerDao: PlayerDao): TeamRepository {
        return TeamRepositoryImpl(teamDao, playerDao)
    }
}