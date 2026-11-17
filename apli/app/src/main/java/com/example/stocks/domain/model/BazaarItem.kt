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
    
    /** Icon URL for the item (using Hypixel's wiki or Minecraft wiki) */
    val iconUrl: String? get() = try {
        // Try to get icon from Minecraft wiki or Hypixel wiki
        // For now, use a placeholder - can be enhanced with actual item icon mapping
        when {
            tag.contains("GEMSTONE") -> "https://wiki.hypixel.net/images/9/9e/Gemstone_Gem.png"
            tag.contains("RECOMBOBULATOR") -> "https://wiki.hypixel.net/images/3/38/Recombobulator_3000.png"
            tag.contains("COOKIE") -> "https://wiki.hypixel.net/images/7/70/Booster_Cookie.png"
            tag.contains("DIVAN") -> "https://wiki.hypixel.net/images/9/93/Divan_Fragment.png"
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}
