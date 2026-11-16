package com.example.stocks.data.local.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<WatchlistEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(item: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE itemTag = :tag")
    suspend fun remove(tag: String)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE itemTag = :tag)")
    suspend fun isWatched(tag: String): Boolean
}
