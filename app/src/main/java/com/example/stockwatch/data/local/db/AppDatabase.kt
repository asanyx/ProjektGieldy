package com.example.stockwatch.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.stockwatch.data.local.dao.WatchlistDao
import com.example.stockwatch.data.local.entity.WatchlistEntity

/**
 * Główna baza danych Room aplikacji StockWatch.
 * Wersja 2 — dodano pole notes w tabeli watchlist.
 */
@Database(entities = [WatchlistEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun watchlistDao(): WatchlistDao

    companion object {
        const val NAME = "stockwatch.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE watchlist ADD COLUMN notes TEXT NOT NULL DEFAULT ''"
                )
            }
        }
    }
}
