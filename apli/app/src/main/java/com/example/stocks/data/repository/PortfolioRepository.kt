package com.example.stocks.data.repository

import com.example.stocks.data.local.db.*
import com.example.stocks.domain.model.BazaarItem
import com.example.stocks.domain.model.Holding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class PortfolioRepository(
    private val holdingDao: HoldingDao,
    private val watchlistDao: WatchlistDao,
    private val transactionDao: TransactionDao,
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
                    itemTag       = entity.itemTag,
                    quantity      = entity.quantity,
                    avgBuyPrice   = entity.avgBuyPrice,
                    liveSellPrice = livePrice,
                )
            }
        }

    /**
     * Record a manual BUY trade:
     * - Adds/updates Room holding (weighted average buy price)
     * - Inserts transaction into transactions table
     */
    suspend fun recordBuy(tag: String, quantity: Long, pricePerUnit: Double, notes: String = "") {
        val existing = holdingDao.getByTag(tag)
        val newQuantity: Long
        val newAvgPrice: Double

        if (existing != null) {
            val totalCost = (existing.quantity * existing.avgBuyPrice) + (quantity * pricePerUnit)
            newQuantity = existing.quantity + quantity
            newAvgPrice = if (newQuantity > 0) totalCost / newQuantity else pricePerUnit
        } else {
            newQuantity = quantity
            newAvgPrice = pricePerUnit
        }

        holdingDao.upsert(HoldingEntity(tag, newQuantity, newAvgPrice))
        transactionDao.insert(
            TransactionEntity(
                itemTag = tag,
                type = TransactionType.BUY,
                quantity = quantity,
                pricePerUnit = pricePerUnit,
                notes = notes,
            )
        )
    }

    /**
     * Record a manual SELL trade:
     * - Decreases Room holding (or deletes if quantity reaches 0)
     * - Inserts transaction into transactions table
     */
    suspend fun recordSell(tag: String, quantity: Long, pricePerUnit: Double, notes: String = "") {
        val existing = holdingDao.getByTag(tag) ?: return
        val remaining = (existing.quantity - quantity).coerceAtLeast(0)

        if (remaining == 0L) {
            holdingDao.deleteByTag(tag)
        } else {
            holdingDao.upsert(existing.copy(quantity = remaining))
        }

        transactionDao.insert(
            TransactionEntity(
                itemTag = tag,
                type = TransactionType.SELL,
                quantity = quantity,
                pricePerUnit = pricePerUnit,
                notes = notes,
            )
        )
    }

    suspend fun upsertHolding(tag: String, quantity: Long, avgBuyPrice: Double) {
        holdingDao.upsert(HoldingEntity(tag, quantity, avgBuyPrice))
    }

    suspend fun removeHolding(tag: String) = holdingDao.deleteByTag(tag)

    // ---- Transactions History / Statement ----

    fun observeRecentTransactions(limit: Int = 10): Flow<List<TransactionEntity>> =
        transactionDao.observeRecent(limit)

    fun observeAllTransactions(): Flow<List<TransactionEntity>> =
        transactionDao.observeAll()

    // ---- Watchlist ----

    fun observeWatchlist(): Flow<List<WatchlistEntity>> = watchlistDao.observeAll()

    fun observeWatchlistItems(): Flow<List<BazaarItem>> =
        watchlistDao.observeAll().map { entities ->
            val market = runCatching { bazaarRepository.getAllItems() }.getOrDefault(emptyMap())
            entities.mapNotNull { market[it.itemTag] }
        }

    suspend fun toggleWatchlist(tag: String): Boolean {
        return if (watchlistDao.isWatched(tag)) {
            watchlistDao.remove(tag)
            false
        } else {
            watchlistDao.add(WatchlistEntity(tag))
            true
        }
    }

    suspend fun addToWatchlist(tag: String) = watchlistDao.add(WatchlistEntity(tag))
    suspend fun removeFromWatchlist(tag: String) = watchlistDao.remove(tag)
    suspend fun isWatched(tag: String) = watchlistDao.isWatched(tag)
}
