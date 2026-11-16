package com.example.stocks.domain.model

data class Holding(
    val itemTag: String,
    val quantity: Long,
    val avgBuyPrice: Double,     // coins per unit, user-entered
    val liveSellPrice: Double,   // populated from Bazaar API
) {
    val costBasis: Double      get() = quantity * avgBuyPrice
    val currentValue: Double   get() = quantity * liveSellPrice
    val unrealizedPnL: Double  get() = currentValue - costBasis
    val returnPct: Double      get() = if (costBasis > 0) (unrealizedPnL / costBasis) * 100.0 else 0.0
}
