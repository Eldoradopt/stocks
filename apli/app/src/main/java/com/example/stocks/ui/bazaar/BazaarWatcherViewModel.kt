package com.example.stocks.ui.bazaar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stocks.data.local.db.BazaarListingEntity
import com.example.stocks.data.repository.BazaarListingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BazaarWatcherUiState(
    val allListings: List<BazaarListingEntity> = emptyList(),
    val activeListings: List<BazaarListingEntity> = emptyList(),
    val soldListings: List<BazaarListingEntity> = emptyList(),
    val activeCount: Int = 0,
    val soldCount: Int = 0,
    val totalProfit: Double = 0.0,
    val selectedTab: Tab = Tab.ACTIVE,
)

class BazaarWatcherViewModel(
    private val bazaarListingRepo: BazaarListingRepository,
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(Tab.ACTIVE)
    val selectedTab: StateFlow<Tab> = _selectedTab.asStateFlow()

    val uiState: StateFlow<BazaarWatcherUiState> = combine(
        bazaarListingRepo.getAllListings(),
        bazaarListingRepo.getActiveListings(),
        bazaarListingRepo.getSoldListings(),
        _selectedTab,
    ) { all, active, sold, tab ->
        BazaarWatcherUiState(
            allListings = all,
            activeListings = active,
            soldListings = sold,
            activeCount = active.size,
            soldCount = sold.size,
            totalProfit = sold.sumOf { it.profit },
            selectedTab = tab,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BazaarWatcherUiState())

    fun setTab(tab: Tab) {
        _selectedTab.value = tab
    }

    fun createListing(itemTag: String, itemDisplayName: String, quantity: Long, price: Double) = viewModelScope.launch {
        bazaarListingRepo.createListing(itemTag, itemDisplayName, quantity, price)
    }

    fun markAsSold(listingId: Long, soldQuantity: Long, avgSellPrice: Double) = viewModelScope.launch {
        bazaarListingRepo.markAsSold(listingId, soldQuantity, avgSellPrice)
    }

    fun cancelListing(listingId: Long) = viewModelScope.launch {
        bazaarListingRepo.cancelListing(listingId)
    }
}
