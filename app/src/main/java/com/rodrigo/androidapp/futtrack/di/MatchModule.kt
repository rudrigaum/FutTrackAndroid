package com.rodrigo.androidapp.futtrack.di

import com.google.firebase.firestore.FirebaseFirestore
import com.rodrigo.androidapp.futtrack.data.repository.MatchRepositoryFirebaseImpl
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
    fun provideMatchRepository(firestore: FirebaseFirestore): MatchRepository {
        return MatchRepositoryFirebaseImpl(firestore)
    }
}