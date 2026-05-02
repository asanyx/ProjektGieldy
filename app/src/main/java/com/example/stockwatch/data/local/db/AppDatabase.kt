package com.example.stockwatch.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.stockwatch.data.local.dao.WatchlistDao
import com.example.stockwatch.data.local.entity.WatchlistEntity

@Database(entities = [WatchlistEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun watchlistDao(): WatchlistDao

    companion object {
        const val NAME = "stockwatch.db"
    }
}
