package com.example.stocks.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BossDropDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(drop: BossDropEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(drops: List<BossDropEntity>)

    @Update
    suspend fun update(drop: BossDropEntity)

    @Query("DELETE FROM boss_drops WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM boss_drops")
    suspend fun deleteAll()

    @Query("SELECT * FROM boss_drops ORDER BY timestamp DESC")
    fun getAllDrops(): Flow<List<BossDropEntity>>

    @Query("SELECT * FROM boss_drops WHERE id = :id")
    suspend fun getById(id: Long): BossDropEntity?

    @Query("SELECT * FROM boss_drops WHERE bossName = :bossName ORDER BY timestamp DESC")
    fun getDropsByBoss(bossName: String): Flow<List<BossDropEntity>>

    @Query("SELECT * FROM boss_drops WHERE sold = 0 ORDER BY timestamp DESC")
    fun getUnsoldDrops(): Flow<List<BossDropEntity>>

    @Query("SELECT * FROM boss_drops WHERE sold = 1 ORDER BY timestamp DESC")
    fun getSoldDrops(): Flow<List<BossDropEntity>>

    @Query("SELECT SUM(dropValue) FROM boss_drops WHERE sold = 0")
    suspend fun getTotalUnsoldValue(): Double

    @Query("SELECT SUM(profit) FROM boss_drops WHERE sold = 1")
    suspend fun getTotalProfit(): Double

    @Query("SELECT SUM(dropValue) FROM boss_drops")
    suspend fun getTotalDropValue(): Double

    @Query("SELECT bossName, COUNT(*) as count, SUM(dropValue) as totalValue FROM boss_drops GROUP BY bossName")
    suspend fun getStatsByBoss(): List<BossStats>
}

data class BossStats(
    val bossName: String,
    val count: Int,
    val totalValue: Double
)
