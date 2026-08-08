package com.rodrigo.androidapp.futtrack.di

import com.rodrigo.androidapp.futtrack.data.repository.StandingBaselineRepositoryFirebaseImpl
import com.rodrigo.androidapp.futtrack.domain.repository.StandingBaselineRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StandingBaselineModule {

    @Binds
    @Singleton
    abstract fun bindStandingBaselineRepository(
        implementation: StandingBaselineRepositoryFirebaseImpl
    ): StandingBaselineRepository
}