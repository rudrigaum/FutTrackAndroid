package com.rodrigo.androidapp.futtrack.di

import android.app.Application
import androidx.room.Room
import com.rodrigo.androidapp.futtrack.data.local.FuttrackDatabase
import com.rodrigo.androidapp.futtrack.data.local.dao.MatchDao
import com.rodrigo.androidapp.futtrack.data.local.dao.TeamDao
import com.rodrigo.androidapp.futtrack.data.local.dao.PlayerDao
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
        )
            .fallbackToDestructiveMigration() // Recria o banco limpo ao mudar a versão
            .build()
    }

    @Provides
    @Singleton
    fun provideTeamDao(database: FuttrackDatabase): TeamDao = database.teamDao()

    @Provides
    @Singleton
    fun provideMatchDao(database: FuttrackDatabase): MatchDao = database.matchDao()

    @Provides
    @Singleton
    fun providePlayerDao(database: FuttrackDatabase): PlayerDao = database.playerDao()
}