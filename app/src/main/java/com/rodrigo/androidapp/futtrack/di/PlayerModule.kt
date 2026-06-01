package com.rodrigo.androidapp.futtrack.di

import com.google.firebase.firestore.FirebaseFirestore
import com.rodrigo.androidapp.futtrack.data.repository.PlayerRepositoryFirebaseImpl
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
    fun providePlayerRepository(firestore: FirebaseFirestore): PlayerRepository {
        return PlayerRepositoryFirebaseImpl(firestore)
    }
}