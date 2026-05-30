package com.rodrigo.androidapp.futtrack.di

import android.app.Application
import androidx.room.Room
import com.rodrigo.androidapp.futtrack.data.local.FuttrackDatabase
import com.rodrigo.androidapp.futtrack.data.local.dao.MatchDao
import com.rodrigo.androidapp.futtrack.data.local.dao.TeamDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFuttrackDatabase(app: Application): FuttrackDatabase {
        return Room.databaseBuilder(
            app,
            FuttrackDatabase::class.java,
            FuttrackDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideTeamDao(database: FuttrackDatabase): TeamDao {
        return database.teamDao()
    }

    @Provides
    @Singleton
    fun provideMatchDao(database: FuttrackDatabase): MatchDao {
        return database.matchDao()
    }
}