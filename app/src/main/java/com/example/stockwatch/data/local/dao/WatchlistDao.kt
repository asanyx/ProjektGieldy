package com.example.stockwatch.data.local.dao

import androidx.room.*
import com.example.stockwatch.data.local.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO dla tabeli watchlist. Udostępnia operacje CRUD na lokalnej
 * liście obserwowanych monet.
 */
@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    fun getAllAsFlow(): Flow<List<WatchlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WatchlistEntity)

    @Update
    suspend fun update(item: WatchlistEntity)

    @Delete
    suspend fun delete(item: WatchlistEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE coinId = :id)")
    fun isWatchlisted(id: String): Flow<Boolean>

    @Query("UPDATE watchlist SET notes = :notes WHERE coinId = :coinId")
    suspend fun updateNotes(coinId: String, notes: String)
}
