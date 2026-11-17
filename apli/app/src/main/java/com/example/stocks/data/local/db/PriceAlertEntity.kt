package com.example.stocks.data.local.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "price_alerts")
data class PriceAlertEntity(
    @PrimaryKey val itemTag: String,
    val targetHighPrice: Double = 0.0,
    val targetLowPrice: Double = 0.0,
    val notifyOnSpikePercent: Double = 5.0, // default 5%
    val isEnabled: Boolean = true,
    val lastKnownPrice: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface PriceAlertDao {
    @Query("SELECT * FROM price_alerts WHERE isEnabled = 1")
    fun observeActiveAlerts(): Flow<List<PriceAlertEntity>>

    @Query("SELECT * FROM price_alerts WHERE isEnabled = 1")
    suspend fun getActiveAlerts(): List<PriceAlertEntity>

    @Query("SELECT * FROM price_alerts WHERE itemTag = :tag")
    suspend fun getAlertByTag(tag: String): PriceAlertEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAlert(alert: PriceAlertEntity)

    @Query("DELETE FROM price_alerts WHERE itemTag = :tag")
    suspend fun deleteAlert(tag: String)
}
