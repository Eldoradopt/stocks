package com.example.stocks.ui.flipper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stocks.data.repository.BazaarRepository
import com.example.stocks.domain.model.BazaarFlip
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.min

enum class FlipSortOrder {
    HOURLY_PROFIT,
    HIGHEST_ROI,
    NET_MARGIN,
    HIGH_VOLUME,
    NPC_ARBITRAGE
}

data class FlipperUiState(
    val flips: List<BazaarFlip> = emptyList(),
    val sortOrder: FlipSortOrder = FlipSortOrder.HOURLY_PROFIT,
    val minVolumeFilter: Long = 5_000L,
    val isLoading: Boolean = false,
)

class FlipperViewModel(
    private val bazaarRepo: BazaarRepository,
) : ViewModel() {

    private val _sortOrder = MutableStateFlow(FlipSortOrder.HOURLY_PROFIT)
    val sortOrder = _sortOrder.asStateFlow()

    private val _rawFlips = MutableStateFlow<List<BazaarFlip>>(emptyList())
    private val _isLoading = MutableStateFlow(false)

    val uiState: StateFlow<FlipperUiState> = combine(
        _rawFlips,
        _sortOrder,
        _isLoading
    ) { allFlips, sort, loading ->
        val sorted = when (sort) {
            FlipSortOrder.HOURLY_PROFIT -> allFlips.sortedByDescending { it.hourlyProfit }
            FlipSortOrder.HIGHEST_ROI   -> allFlips.filter { it.roiPct < 500 }.sortedByDescending { it.roiPct }
            FlipSortOrder.NET_MARGIN    -> allFlips.sortedByDescending { it.netMargin }
            FlipSortOrder.HIGH_VOLUME   -> allFlips.sortedByDescending { it.hourlyVolume }
            FlipSortOrder.NPC_ARBITRAGE -> allFlips.filter { it.isNpcArbitrage }.sortedByDescending { it.netMargin }
        }
        FlipperUiState(
            flips = sorted.take(50),
            sortOrder = sort,
            isLoading = loading
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FlipperUiState())

    init {
        loadFlips()
    }

    fun loadFlips() = viewModelScope.launch {
        _isLoading.value = true
        runCatching {
            val market = bazaarRepo.getAllItems()
            val list = market.values.mapNotNull { item ->
                if (item.buyPrice <= 0 || item.sellPrice <= 0) return@mapNotNull null

                // Hypixel Bazaar tax: 1.125%
                val netSell = item.buyPrice * 0.98875
                val margin = netSell - item.sellPrice
                if (margin <= 0) return@mapNotNull null

                val roi = (margin / item.sellPrice) * 100.0
                val hourlyVol = (item.sellMovingWeek / (7 * 24)).coerceAtLeast(0)
                if (hourlyVol < 10) return@mapNotNull null // Filter dead items

                // Order cap estimation (max 71,680 items per order)
                val hourlyFills = min(hourlyVol, 71_680L)
                val hourlyCoins = margin * hourlyFills

                BazaarFlip(
                    tag = item.tag,
                    displayName = item.displayName,
                    buyPrice = item.buyPrice,
                    sellPrice = item.sellPrice,
                    netMargin = margin,
                    roiPct = roi,
                    hourlyVolume = hourlyVol,
                    hourlyProfit = hourlyCoins,
                    isNpcArbitrage = false
                )
            }
            _rawFlips.value = list
        }
        _isLoading.value = false
    }

    fun setSortOrder(order: FlipSortOrder) {
        _sortOrder.value = order
    }
}
