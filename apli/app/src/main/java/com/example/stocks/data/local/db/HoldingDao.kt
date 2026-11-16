package com.example.stocks.data.local.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HoldingDao {
    @Query("SELECT * FROM holdings")
    fun observeAll(): Flow<List<HoldingEntity>>

    @Query("SELECT * FROM holdings WHERE itemTag = :tag")
    suspend fun getByTag(tag: String): HoldingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(holding: HoldingEntity)

    @Query("DELETE FROM holdings WHERE itemTag = :tag")
    suspend fun deleteByTag(tag: String)
}
