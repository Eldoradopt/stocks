package com.example.stocks.data.repository

import com.example.stocks.data.local.db.BossDropDao
import com.example.stocks.data.local.db.BossDropEntity
import com.example.stocks.data.local.db.BossStats
import kotlinx.coroutines.flow.Flow

class BossDropRepository(
    private val bossDropDao: BossDropDao,
) {
    fun getAllDrops(): Flow<List<BossDropEntity>> = bossDropDao.getAllDrops()
    fun getDropsByBoss(bossName: String): Flow<List<BossDropEntity>> = bossDropDao.getDropsByBoss(bossName)
    fun getUnsoldDrops(): Flow<List<BossDropEntity>> = bossDropDao.getUnsoldDrops()
    fun getSoldDrops(): Flow<List<BossDropEntity>> = bossDropDao.getSoldDrops()

    suspend fun recordDrop(
        bossName: String,
        dropTag: String,
        dropDisplayName: String,
        quantity: Long,
        unitPrice: Double,
        tier: Int = 1
    ): Long {
        val drop = BossDropEntity(
            bossName = bossName,
            dropTag = dropTag,
            dropDisplayName = dropDisplayName,
            quantity = quantity,
            dropValue = quantity * unitPrice,
            unitPrice = unitPrice,
            timestamp = System.currentTimeMillis(),
            tier = tier
        )
        return bossDropDao.insert(drop)
    }

    suspend fun markAsSold(dropId: Long, soldPrice: Double) {
        val originalDrop = bossDropDao.getById(dropId) ?: return
        val soldRevenue = originalDrop.quantity * soldPrice
        val profit = soldRevenue - originalDrop.dropValue
        
        val updatedDrop = originalDrop.copy(
            sold = true,
            soldPrice = soldPrice,
            profit = profit
        )
        // Need to add update method to DAO
    }

    suspend fun deleteDrop(dropId: Long) {
        bossDropDao.delete(dropId)
    }

    suspend fun getTotalUnsoldValue(): Double = bossDropDao.getTotalUnsoldValue()
    suspend fun getTotalProfit(): Double = bossDropDao.getTotalProfit()
    suspend fun getTotalDropValue(): Double = bossDropDao.getTotalDropValue()
    suspend fun getStatsByBoss(): List<BossStats> = bossDropDao.getStatsByBoss()
}
