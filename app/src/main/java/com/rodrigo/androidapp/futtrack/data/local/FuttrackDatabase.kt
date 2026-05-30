package com.rodrigo.androidapp.futtrack.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rodrigo.androidapp.futtrack.data.local.dao.MatchDao
import com.rodrigo.androidapp.futtrack.data.local.dao.TeamDao
import com.rodrigo.androidapp.futtrack.data.local.entity.MatchEntity
import com.rodrigo.androidapp.futtrack.data.local.entity.TeamEntity

@Database(
    entities = [TeamEntity::class, MatchEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FuttrackDatabase : RoomDatabase() {

    abstract fun teamDao(): TeamDao
    abstract fun matchDao(): MatchDao

    companion object {
        const val DATABASE_NAME = "futtrack_db"
    }
}