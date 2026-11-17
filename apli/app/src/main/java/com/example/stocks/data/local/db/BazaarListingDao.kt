package com.example.stocks.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BazaarListingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(listing: BazaarListingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(listings: List<BazaarListingEntity>)

    @Update
    suspend fun update(listing: BazaarListingEntity)

    @Query("DELETE FROM bazaar_listings WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM bazaar_listings")
    suspend fun deleteAll()

    @Query("SELECT * FROM bazaar_listings ORDER BY creationTimestamp DESC")
    fun getAllListings(): Flow<List<BazaarListingEntity>>

    @Query("SELECT * FROM bazaar_listings WHERE id = :id")
    suspend fun getById(id: Long): BazaarListingEntity?

    @Query("SELECT * FROM bazaar_listings WHERE status = 'ACTIVE' ORDER BY creationTimestamp DESC")
    fun getActiveListings(): Flow<List<BazaarListingEntity>>

    @Query("SELECT * FROM bazaar_listings WHERE status = 'SOLD' ORDER BY completionTimestamp DESC")
    fun getSoldListings(): Flow<List<BazaarListingEntity>>

    @Query("SELECT * FROM bazaar_listings WHERE itemTag = :tag ORDER BY creationTimestamp DESC")
    fun getListingsByItem(tag: String): Flow<List<BazaarListingEntity>>

    @Query("SELECT SUM(totalListedCost) FROM bazaar_listings WHERE status = 'ACTIVE'")
    suspend fun getTotalInvested(): Double

    @Query("SELECT SUM(totalSoldRevenue) FROM bazaar_listings WHERE status = 'SOLD'")
    suspend fun getTotalRevenue(): Double

    @Query("SELECT SUM(profit) FROM bazaar_listings WHERE status = 'SOLD'")
    suspend fun getTotalProfit(): Double

    @Query("SELECT COUNT(*) FROM bazaar_listings WHERE status = 'ACTIVE'")
    suspend fun getActiveCount(): Int

    @Query("SELECT COUNT(*) FROM bazaar_listings WHERE status = 'SOLD'")
    suspend fun getSoldCount(): Int
}
