package com.example.stocks.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bazaar_listings")
data class BazaarListingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemTag: String,
    val itemDisplayName: String,
    val listedQuantity: Long,
    val listedPrice: Double,        // price per unit
    val totalListedCost: Double,    // quantity * listedPrice
    val creationTimestamp: Long,
    val status: ListingStatus,       // ACTIVE, SOLD, CANCELLED, EXPIRED
    val soldQuantity: Long = 0,     // how much was sold
    val soldPrice: Double = 0.0,    // average sell price
    val totalSoldRevenue: Double = 0.0,
    val profit: Double = 0.0,       // totalSoldRevenue - (soldQuantity * listedPrice)
    val completionTimestamp: Long? = null,
)

enum class ListingStatus {
    ACTIVE,
    SOLD,
    CANCELLED,
    EXPIRED
}
