package com.example.stocks.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val itemTag: String,
    val addedAt: Long = System.currentTimeMillis(),
)
