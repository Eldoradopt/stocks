package com.example.stocks.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    BUY,
    SELL
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemTag: String,
    val type: TransactionType,
    val quantity: Long,
    val pricePerUnit: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
) {
    val totalCoins: Double get() = quantity * pricePerUnit
}
