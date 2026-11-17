package com.example.stocks.domain.model

data class BazaarFlip(
    val tag: String,
    val displayName: String,
    val buyPrice: Double,
    val sellPrice: Double,
    val netMargin: Double,       // Net coins per item after 1.125% Bazaar tax
    val roiPct: Double,          // Return on investment percentage
    val hourlyVolume: Long,      // Estimated fills per hour
    val hourlyProfit: Double,    // Projected coins/hour
    val isNpcArbitrage: Boolean = false,
    val npcSellPrice: Double = 0.0,
)
