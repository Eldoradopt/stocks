package com.example.stocks.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        HoldingEntity::class,
        WatchlistEntity::class,
        TransactionEntity::class,
        PriceAlertEntity::class
    ],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun holdingDao(): HoldingDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun transactionDao(): TransactionDao
    abstract fun priceAlertDao(): PriceAlertDao
}
