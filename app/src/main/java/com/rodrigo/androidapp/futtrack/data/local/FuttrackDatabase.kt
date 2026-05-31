package com.rodrigo.androidapp.futtrack.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rodrigo.androidapp.futtrack.data.local.dao.MatchDao
import com.rodrigo.androidapp.futtrack.data.local.dao.PlayerDao // Novo import
import com.rodrigo.androidapp.futtrack.data.local.dao.TeamDao
import com.rodrigo.androidapp.futtrack.data.local.entity.MatchEntity
import com.rodrigo.androidapp.futtrack.data.local.entity.PlayerEntity // Novo import
import com.rodrigo.androidapp.futtrack.data.local.entity.TeamEntity

@Database(
    entities = [TeamEntity::class, MatchEntity::class, PlayerEntity::class],
    version = 2,
    exportSchema = false
)
abstract class FuttrackDatabase : RoomDatabase() {

    abstract fun teamDao(): TeamDao
    abstract fun matchDao(): MatchDao
    abstract fun playerDao(): PlayerDao

    companion object {
        const val DATABASE_NAME = "futtrack_db"
    }
}