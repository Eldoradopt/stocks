package com.example.stocks.domain.model

data class BazaarItem(
    val tag: String,           // e.g. "GLOSSY_GEMSTONE"
    val buyPrice: Double,      // instant buy price
    val sellPrice: Double,     // instant sell price
    val buyVolume: Long,
    val sellVolume: Long,
    val buyMovingWeek: Long,
    val sellMovingWeek: Long,
) {
    val spread: Double get() = buyPrice - sellPrice
    val spreadPct: Double get() = if (buyPrice > 0) (spread / buyPrice) * 100.0 else 0.0
    /** Friendly display name — replaces underscores and title-cases. */
    val displayName: String get() = tag
        .replace('_', ' ')
        .split(' ')
        .joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }
}
