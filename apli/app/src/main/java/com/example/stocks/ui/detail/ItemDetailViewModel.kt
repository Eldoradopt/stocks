package com.example.stocks.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stocks.data.local.db.PriceAlertEntity
import com.example.stocks.data.local.db.TransactionType
import com.example.stocks.data.repository.BazaarRepository
import com.example.stocks.data.repository.PortfolioRepository
import com.example.stocks.domain.model.BazaarItem
import com.example.stocks.domain.model.PricePoint
import com.example.stocks.domain.model.SlayerCatalog
import com.example.stocks.ui.components.ChartTimeRange
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ItemDetailUiState(
    val item: BazaarItem? = null,
    val categorySubtitle: String = "Bazaar Commodity",
    val history: List<PricePoint> = emptyList(),
    val selectedTimeRange: ChartTimeRange = ChartTimeRange.ONE_DAY,
    val changePct: Double = 0.0,
    val minPrice: Double = 0.0,
    val maxPrice: Double = 0.0,
    val isWatched: Boolean = false,
    val existingAlert: PriceAlertEntity? = null,
    val flipNetMargin: Double = 0.0,
    val flipRoiPct: Double = 0.0,
    val flipHourlyProfit: Double = 0.0,
    val isLoading: Boolean = true,
    val error: String? = null,
)

class ItemDetailViewModel(
    private val itemTag: String,
    private val bazaarRepo: BazaarRepository,
    private val portfolioRepo: PortfolioRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ItemDetailUiState())
    val uiState: StateFlow<ItemDetailUiState> = _uiState.asStateFlow()

    init {
        load(ChartTimeRange.ONE_DAY)
        observeWatchlistState()
    }

    private fun observeWatchlistState() = viewModelScope.launch {
        portfolioRepo.observeWatchlist().collect { list ->
            val watched = list.any { it.itemTag == itemTag }
            _uiState.update { it.copy(isWatched = watched) }
        }
    }

    fun load(range: ChartTimeRange = _uiState.value.selectedTimeRange) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, selectedTimeRange = range, error = null) }
        runCatching {
            val item = bazaarRepo.getItem(itemTag)
            val history = when (range) {
                ChartTimeRange.ONE_HOUR -> bazaarRepo.getDayHistory(itemTag).takeLast(24)
                ChartTimeRange.FOUR_HOURS -> bazaarRepo.getDayHistory(itemTag).takeLast(96)
                ChartTimeRange.ONE_DAY -> bazaarRepo.getDayHistory(itemTag)
                ChartTimeRange.ONE_WEEK -> bazaarRepo.getWeekHistory(itemTag)
                ChartTimeRange.ONE_MONTH -> bazaarRepo.getMonthHistory(itemTag)
                ChartTimeRange.SIX_MONTHS -> bazaarRepo.get3MonthHistory(itemTag)
                ChartTimeRange.ALL -> bazaarRepo.getYearHistory(itemTag)
            }

            val prices = history.map { it.sellPrice }
            val first = history.firstOrNull()?.sellPrice ?: 0.0
            val last = history.lastOrNull()?.sellPrice ?: (item?.sellPrice ?: 0.0)
            val change = if (first > 0) ((last - first) / first) * 100.0 else 0.0
            val watched = portfolioRepo.isWatched(itemTag)
            val alert = portfolioRepo.getPriceAlert(itemTag)

            // Flipper Profit calculations (1.125% Bazaar tax)
            val buyPrice = item?.buyPrice ?: 0.0
            val netSell = buyPrice * 0.98875
            val margin = (netSell - (item?.sellPrice ?: 0.0)).coerceAtLeast(0.0)
            val roi = if ((item?.sellPrice ?: 0.0) > 0) (margin / item!!.sellPrice) * 100.0 else 0.0
            val hourlyVol = ((item?.sellMovingWeek ?: 0L) / (7 * 24)).coerceAtLeast(0)
            val hourlyCoins = margin * kotlin.math.min(hourlyVol, 71_680L)

            _uiState.update {
                ItemDetailUiState(
                    item = item,
                    categorySubtitle = SlayerCatalog.getCategory(itemTag),
                    history = history,
                    selectedTimeRange = range,
                    changePct = change,
                    minPrice = prices.minOrNull() ?: 0.0,
                    maxPrice = prices.maxOrNull() ?: 0.0,
                    isWatched = watched,
                    existingAlert = alert,
                    flipNetMargin = margin,
                    flipRoiPct = roi,
                    flipHourlyProfit = hourlyCoins,
                    isLoading = false,
                )
            }
        }.onFailure { e ->
            _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
    }

    fun toggleWatchlist() = viewModelScope.launch {
        val newStatus = portfolioRepo.toggleWatchlist(itemTag)
        _uiState.update { it.copy(isWatched = newStatus) }
    }

    fun setPriceAlert(high: Double, low: Double) = viewModelScope.launch {
        portfolioRepo.setPriceAlert(itemTag, high, low)
        val alert = portfolioRepo.getPriceAlert(itemTag)
        _uiState.update { it.copy(existingAlert = alert) }
    }

    fun recordTrade(type: TransactionType, quantity: Long, price: Double, notes: String = "") = viewModelScope.launch {
        if (type == TransactionType.BUY) {
            portfolioRepo.recordBuy(itemTag, quantity, price, notes)
        } else {
            portfolioRepo.recordSell(itemTag, quantity, price, notes)
        }
    }
}
