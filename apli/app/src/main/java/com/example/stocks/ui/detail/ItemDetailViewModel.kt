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
    val change7dPct: Double = 0.0,
    val minPrice: Double = 0.0,
    val maxPrice: Double = 0.0,
    val isWatched: Boolean = false,
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
        load()
        observeWatchlistState()
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
            val item    = bazaarRepo.getItem(itemTag)
            val history = bazaarRepo.getWeekHistory(itemTag)
            val prices  = history.map { it.sellPrice }
            val first   = history.firstOrNull()?.sellPrice ?: 0.0
            val last    = history.lastOrNull()?.sellPrice  ?: 0.0
            val change  = if (first > 0) ((last - first) / first) * 100.0 else 0.0
            val watched = portfolioRepo.isWatched(itemTag)
            _uiState.update {
                ItemDetailUiState(
                    item        = item,
                    history     = history,
                    change7dPct = change,
                    minPrice    = prices.minOrNull() ?: 0.0,
                    maxPrice    = prices.maxOrNull() ?: 0.0,
                    isWatched   = watched,
                    isLoading   = false,
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

    fun recordTrade(type: TransactionType, quantity: Long, price: Double, notes: String = "") = viewModelScope.launch {
        if (type == TransactionType.BUY) {
            portfolioRepo.recordBuy(itemTag, quantity, price, notes)
        } else {
            portfolioRepo.recordSell(itemTag, quantity, price, notes)
        }
    }
}
