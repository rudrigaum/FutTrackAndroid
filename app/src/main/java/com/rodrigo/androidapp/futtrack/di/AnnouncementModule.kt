package com.rodrigo.androidapp.futtrack.di

import com.google.firebase.firestore.FirebaseFirestore
import com.rodrigo.androidapp.futtrack.data.repository.AnnouncementRepositoryFirebaseImpl
import com.rodrigo.androidapp.futtrack.domain.repository.AnnouncementRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnnouncementModule {

    @Provides
    @Singleton
    fun provideAnnouncementRepository(
        firestore: FirebaseFirestore
    ): AnnouncementRepository {
        return AnnouncementRepositoryFirebaseImpl(firestore)
    }
}