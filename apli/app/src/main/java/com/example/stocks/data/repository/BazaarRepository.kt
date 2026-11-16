package com.example.stocks.data.repository

import com.example.stocks.data.remote.hypixel.HypixelApiService
import com.example.stocks.data.remote.coflnet.CoflnetApiService
import com.example.stocks.data.remote.coflnet.model.PriceHistoryEntry
import com.example.stocks.domain.model.BazaarItem
import com.example.stocks.domain.model.PricePoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Fetches and caches live Bazaar prices (full market in one API call)
 * and historical data from Coflnet.
 *
 * Cache TTL: 60 s for live prices, 5 min for history (Coflnet rate limit).
 */
class BazaarRepository(
    private val hypixel: HypixelApiService,
    private val coflnet: CoflnetApiService,
) {
    // ---- in-memory cache ----
    private var cachedItems: Map<String, BazaarItem> = emptyMap()
    private var cacheTimestamp: Long = 0L
    private val liveCacheTtlMs = 60_000L   // 60 s

    private val historyCache = mutableMapOf<String, Pair<Long, List<PricePoint>>>()
    private val historyCacheTtlMs = 300_000L  // 5 min

    // Returns the full market, refreshing if stale.
    suspend fun getAllItems(): Map<String, BazaarItem> {
        val now = System.currentTimeMillis()
        if (now - cacheTimestamp > liveCacheTtlMs) {
            val response = hypixel.getBazaar()
            cachedItems = response.products.mapValues { (_, product) ->
                val qs = product.quickStatus
                BazaarItem(
                    tag              = qs.productId,
                    buyPrice         = qs.buyPrice,
                    sellPrice        = qs.sellPrice,
                    buyVolume        = qs.buyVolume,
                    sellVolume       = qs.sellVolume,
                    buyMovingWeek    = qs.buyMovingWeek,
                    sellMovingWeek   = qs.sellMovingWeek,
                )
            }
            cacheTimestamp = now
        }
        return cachedItems
    }

    suspend fun getItem(tag: String): BazaarItem? = getAllItems()[tag]

    fun observeItems(): Flow<Map<String, BazaarItem>> = flow {
        while (true) {
            emit(getAllItems())
            kotlinx.coroutines.delay(liveCacheTtlMs)
        }
    }

    // ---- Coflnet history ----
    suspend fun getWeekHistory(tag: String): List<PricePoint> {
        val now = System.currentTimeMillis()
        val cached = historyCache[tag]
        if (cached != null && now - cached.first < historyCacheTtlMs) return cached.second

        val entries = coflnet.getWeekHistory(tag)
        val points  = entries.map { it.toDomain() }
        historyCache[tag] = now to points
        return points
    }

    suspend fun getDayHistory(tag: String): List<PricePoint> {
        val entries = coflnet.getDayHistory(tag)
        return entries.map { it.toDomain() }
    }

    private fun PriceHistoryEntry.toDomain() = PricePoint(
        // Coflnet returns ISO-8601; parse to epoch ms.
        timestamp = try {
            java.time.Instant.parse(timestamp + "Z").toEpochMilli()
        } catch (_: Exception) {
            System.currentTimeMillis()
        },
        buyPrice  = buy,
        sellPrice = sell,
    )
}
