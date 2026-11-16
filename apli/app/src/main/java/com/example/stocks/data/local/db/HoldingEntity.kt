package com.example.stocks.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "holdings")
data class HoldingEntity(
    @PrimaryKey val itemTag: String,
    val quantity: Long,
    val avgBuyPrice: Double,
)
