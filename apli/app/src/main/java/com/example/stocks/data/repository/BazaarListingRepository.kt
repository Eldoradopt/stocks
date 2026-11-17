package com.example.stocks.data.repository

import com.example.stocks.data.local.db.BazaarListingDao
import com.example.stocks.data.local.db.BazaarListingEntity
import com.example.stocks.data.local.db.ListingStatus
import kotlinx.coroutines.flow.Flow

class BazaarListingRepository(
    private val bazaarListingDao: BazaarListingDao,
) {
    fun getAllListings(): Flow<List<BazaarListingEntity>> = bazaarListingDao.getAllListings()
    fun getActiveListings(): Flow<List<BazaarListingEntity>> = bazaarListingDao.getActiveListings()
    fun getSoldListings(): Flow<List<BazaarListingEntity>> = bazaarListingDao.getSoldListings()
    fun getListingsByItem(tag: String): Flow<List<BazaarListingEntity>> = bazaarListingDao.getListingsByItem(tag)

    suspend fun createListing(
        itemTag: String,
        itemDisplayName: String,
        quantity: Long,
        price: Double
    ): Long {
        val listing = BazaarListingEntity(
            itemTag = itemTag,
            itemDisplayName = itemDisplayName,
            listedQuantity = quantity,
            listedPrice = price,
            totalListedCost = quantity * price,
            creationTimestamp = System.currentTimeMillis(),
            status = ListingStatus.ACTIVE
        )
        return bazaarListingDao.insert(listing)
    }

    suspend fun markAsSold(
        listingId: Long,
        soldQuantity: Long,
        avgSellPrice: Double
    ) {
        val originalListing = bazaarListingDao.getById(listingId) ?: return
        
        val soldRevenue = soldQuantity * avgSellPrice
        val costBasis = soldQuantity * originalListing.listedPrice
        val profit = soldRevenue - costBasis
        
        val updatedListing = originalListing.copy(
            status = ListingStatus.SOLD,
            soldQuantity = soldQuantity,
            soldPrice = avgSellPrice,
            totalSoldRevenue = soldRevenue,
            profit = profit,
            completionTimestamp = System.currentTimeMillis()
        )
        bazaarListingDao.update(updatedListing)
    }

    suspend fun cancelListing(listingId: Long) {
        bazaarListingDao.delete(listingId)
    }

    suspend fun getTotalInvested(): Double = bazaarListingDao.getTotalInvested()
    suspend fun getTotalRevenue(): Double = bazaarListingDao.getTotalRevenue()
    suspend fun getTotalProfit(): Double = bazaarListingDao.getTotalProfit()
    suspend fun getActiveCount(): Int = bazaarListingDao.getActiveCount()
    suspend fun getSoldCount(): Int = bazaarListingDao.getSoldCount()
}
