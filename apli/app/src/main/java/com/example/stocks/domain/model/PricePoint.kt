package com.example.stocks.domain.model

data class PricePoint(
    val timestamp: Long,   // epoch ms
    val buyPrice: Double,
    val sellPrice: Double,
)
