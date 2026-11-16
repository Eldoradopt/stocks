package com.example.stocks.data.repository

import com.example.stocks.data.local.db.HoldingDao
import com.example.stocks.data.local.db.HoldingEntity
import com.example.stocks.data.local.db.WatchlistDao
import com.example.stocks.data.local.db.WatchlistEntity
import com.example.stocks.domain.model.BazaarItem
import com.example.stocks.domain.model.Holding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class PortfolioRepository(
    private val holdingDao: HoldingDao,
    private val watchlistDao: WatchlistDao,
    private val bazaarRepository: BazaarRepository,
) {
    // ---- Holdings ----

    fun observeHoldings(): Flow<List<HoldingEntity>> = holdingDao.observeAll()

    /**
     * Combines stored holdings with live Bazaar prices to produce
     * domain Holding objects with current P&L.
     */
    fun observeLiveHoldings(): Flow<List<Holding>> =
        holdingDao.observeAll().map { entities ->
            val market = runCatching { bazaarRepository.getAllItems() }.getOrDefault(emptyMap())
            entities.map { entity ->
                val livePrice = market[entity.itemTag]?.sellPrice ?: 0.0
                Holding(
                    itemTag      = entity.itemTag,
                    quantity     = entity.quantity,
                    avgBuyPrice  = entity.avgBuyPrice,
                    liveSellPrice = livePrice,
                )
            }
        }

    suspend fun upsertHolding(tag: String, quantity: Long, avgBuyPrice: Double) {
        holdingDao.upsert(HoldingEntity(tag, quantity, avgBuyPrice))
    }

    suspend fun removeHolding(tag: String) = holdingDao.deleteByTag(tag)

    // ---- Watchlist ----

    fun observeWatchlist(): Flow<List<WatchlistEntity>> = watchlistDao.observeAll()

    /**
     * Watchlist items enriched with live prices.
     */
    fun observeWatchlistItems(): Flow<List<BazaarItem>> =
        watchlistDao.observeAll().map { entities ->
            val market = runCatching { bazaarRepository.getAllItems() }.getOrDefault(emptyMap())
            entities.mapNotNull { market[it.itemTag] }
        }

    suspend fun addToWatchlist(tag: String) = watchlistDao.add(WatchlistEntity(tag))
    suspend fun removeFromWatchlist(tag: String) = watchlistDao.remove(tag)
    suspend fun isWatched(tag: String) = watchlistDao.isWatched(tag)
}
