package com.example.stockwatch.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val coinId: String,
    val name: String,
    val symbol: String,
    val imageUrl: String,
    val addedAt: Long = System.currentTimeMillis()
)
