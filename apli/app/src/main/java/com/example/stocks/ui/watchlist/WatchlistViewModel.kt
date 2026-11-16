package com.example.stocks.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stocks.data.repository.BazaarRepository
import com.example.stocks.data.repository.PortfolioRepository
import com.example.stocks.domain.model.BazaarItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class WatchlistUiState(
    val selectedCategory: String = "Followed",
    val items: List<BazaarItem> = emptyList(),
    val followedTags: Set<String> = emptySet(),
    val isLoading: Boolean = false,
)

class WatchlistViewModel(
    private val portfolioRepo: PortfolioRepository,
    private val bazaarRepo: BazaarRepository,
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("Followed")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _categoryItems = MutableStateFlow<List<BazaarItem>>(emptyList())
    private val _isLoading = MutableStateFlow(false)

    val followedTags: StateFlow<Set<String>> = portfolioRepo.observeWatchlist()
        .map { list -> list.map { it.itemTag }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val followedItems: StateFlow<List<BazaarItem>> = portfolioRepo.observeWatchlistItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<WatchlistUiState> = combine(
        _selectedCategory,
        _categoryItems,
        followedItems,
        followedTags,
        _isLoading,
    ) { category, catItems, favItems, favTags, loading ->
        val displayed = if (category == "Followed") favItems else catItems
        WatchlistUiState(
            selectedCategory = category,
            items = displayed,
            followedTags = favTags,
            isLoading = loading,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WatchlistUiState())

    init {
        // Load default category
        selectCategory("Followed")
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        if (category != "Followed") {
            viewModelScope.launch {
                _isLoading.value = true
                runCatching {
                    _categoryItems.value = bazaarRepo.getCuratedCategory(category)
                }
                _isLoading.value = false
            }
        }
    }

    fun toggleFollow(tag: String) = viewModelScope.launch {
        portfolioRepo.toggleWatchlist(tag)
    }
}
