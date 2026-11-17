package com.example.stocks.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stocks.data.local.db.TransactionType
import com.example.stocks.data.repository.BazaarRepository
import com.example.stocks.data.repository.PortfolioRepository
import com.example.stocks.domain.model.BazaarItem
import com.example.stocks.domain.model.PricePoint
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ItemDetailUiState(
    val item: BazaarItem? = null,
    val history: List<PricePoint> = emptyList(),
    val changePct: Double = 0.0,
    val minPrice: Double = 0.0,
    val maxPrice: Double = 0.0,
    val isWatched: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedTimeRange: TimeRange = TimeRange.WEEK,
)

enum class TimeRange(val displayName: String) {
    DAY("1D"),
    WEEK("1W"),
    MONTH("1M")
}

class ItemDetailViewModel(
    private val itemTag: String,
    private val bazaarRepo: BazaarRepository,
    private val portfolioRepo: PortfolioRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ItemDetailUiState())
    val uiState: StateFlow<ItemDetailUiState> = _uiState.asStateFlow()
    private val _allItems = MutableStateFlow<Map<String, BazaarItem>>(emptyMap())
    val allItems: StateFlow<Map<String, BazaarItem>> = _allItems.asStateFlow()

    init {
        load()
        observeWatchlistState()
        loadAllItems()
    }

    private fun loadAllItems() = viewModelScope.launch {
        runCatching { _allItems.value = bazaarRepo.getAllItems() }
    }

    private fun observeWatchlistState() = viewModelScope.launch {
        portfolioRepo.observeWatchlist().collect { list ->
            val watched = list.any { it.itemTag == itemTag }
            _uiState.update { it.copy(isWatched = watched) }
        }
    }

    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        runCatching {
            val item = bazaarRepo.getItem(itemTag)
            val watched = portfolioRepo.isWatched(itemTag)
            _uiState.update { it.copy(item = item, isWatched = watched) }
            loadHistory()
        }.onFailure { e ->
            _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
    }

    fun loadHistory() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        runCatching {
            val history = when (_uiState.value.selectedTimeRange) {
                TimeRange.DAY -> bazaarRepo.getDayHistory(itemTag)
                TimeRange.WEEK -> bazaarRepo.getWeekHistory(itemTag)
                TimeRange.MONTH -> bazaarRepo.getMonthHistory(itemTag)
            }
            val prices = history.map { it.sellPrice }
            val first = history.firstOrNull()?.sellPrice ?: 0.0
            val last = history.lastOrNull()?.sellPrice ?: 0.0
            val change = if (first > 0) ((last - first) / first) * 100.0 else 0.0
            _uiState.update {
                it.copy(
                    history = history,
                    changePct = change,
                    minPrice = prices.minOrNull() ?: 0.0,
                    maxPrice = prices.maxOrNull() ?: 0.0,
                    isLoading = false,
                )
            }
        }.onFailure { e ->
            _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
    }

    fun setTimeRange(range: TimeRange) {
        _uiState.update { it.copy(selectedTimeRange = range) }
        loadHistory()
    }

    fun toggleWatchlist() = viewModelScope.launch {
        val newStatus = portfolioRepo.toggleWatchlist(itemTag)
        _uiState.update { it.copy(isWatched = newStatus) }
    }

    fun recordTrade(type: TransactionType, quantity: Long, price: Double, notes: String = "") = viewModelScope.launch {
        if (type == TransactionType.BUY) {
            portfolioRepo.recordBuy(itemTag, quantity, price, notes)
        } else {
            portfolioRepo.recordSell(itemTag, quantity, price, notes)
        }
    }
}
