package com.example.stocks.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "boss_drops")
data class BossDropEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bossName: String,
    val dropTag: String,
    val dropDisplayName: String,
    val quantity: Long,
    val dropValue: Double,         // total value at time of drop
    val unitPrice: Double,          // price per unit at time of drop
    val timestamp: Long,
    val tier: Int = 1,             // 1 for regular, 2+ for rare
    val sold: Boolean = false,
    val soldPrice: Double = 0.0,
    val profit: Double = 0.0,
)
