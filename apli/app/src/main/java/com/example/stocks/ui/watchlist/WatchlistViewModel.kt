package com.example.stocks.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stocks.data.repository.PortfolioRepository
import com.example.stocks.domain.model.BazaarItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WatchlistViewModel(
    private val repo: PortfolioRepository,
) : ViewModel() {

    val watchlistItems: StateFlow<List<BazaarItem>> = repo.observeWatchlistItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun removeFromWatchlist(tag: String) = viewModelScope.launch {
        repo.removeFromWatchlist(tag)
    }
}
