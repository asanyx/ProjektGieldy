package com.example.stockwatch.data.repository

import com.example.stockwatch.data.local.dao.WatchlistDao
import com.example.stockwatch.data.local.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WatchlistRepository @Inject constructor(
    private val dao: WatchlistDao
) {
    fun getAll(): Flow<List<WatchlistEntity>> = dao.getAllAsFlow()
    fun isWatchlisted(id: String): Flow<Boolean> = dao.isWatchlisted(id)
    suspend fun add(entity: WatchlistEntity) = dao.insert(entity)
    suspend fun remove(entity: WatchlistEntity) = dao.delete(entity)
}
