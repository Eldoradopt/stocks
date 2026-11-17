package com.example.stocks.data.repository

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.stocks.data.local.db.*
import com.example.stocks.domain.model.BazaarItem
import com.example.stocks.domain.model.Holding
import com.example.stocks.worker.BazaarAlertWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

class PortfolioRepository(
    private val holdingDao: HoldingDao,
    private val watchlistDao: WatchlistDao,
    private val transactionDao: TransactionDao,
    private val priceAlertDao: PriceAlertDao,
    private val bazaarRepository: BazaarRepository,
    private val context: Context,
) {
    // ---- Holdings ----

    fun observeHoldings(): Flow<List<HoldingEntity>> = holdingDao.observeAll()

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

    // ---- Transactions History ----

    fun observeRecentTransactions(limit: Int = 10): Flow<List<TransactionEntity>> =
        transactionDao.observeRecent(limit)

    // ---- Price Alerts & Notifications ----

    suspend fun setPriceAlert(tag: String, high: Double, low: Double) {
        priceAlertDao.upsertAlert(
            PriceAlertEntity(
                itemTag = tag,
                targetHighPrice = high,
                targetLowPrice = low,
                isEnabled = true
            )
        )
        // Schedule periodic background check
        val workRequest = PeriodicWorkRequestBuilder<BazaarAlertWorker>(15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "bazaar_price_checker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    suspend fun getPriceAlert(tag: String): PriceAlertEntity? =
        priceAlertDao.getAlertByTag(tag)

    suspend fun deletePriceAlert(tag: String) =
        priceAlertDao.deleteAlert(tag)

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
