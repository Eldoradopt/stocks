package com.example.stocks.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stocks.data.repository.BazaarRepository
import com.example.stocks.domain.model.BazaarItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.FlowPreview::class)
class SearchViewModel(
    private val bazaarRepo: BazaarRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _allItems = MutableStateFlow<Map<String, BazaarItem>>(emptyMap())

    val results: StateFlow<List<BazaarItem>> = _query
        .debounce(200)
        .combine(_allItems) { q, items ->
            if (q.isBlank()) items.values.take(50).toList()
            else items.values.filter { it.tag.contains(q, ignoreCase = true) || it.displayName.contains(q, ignoreCase = true) }
                .take(50)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            runCatching { _allItems.value = bazaarRepo.getAllItems() }
        }
    }

    fun onQueryChange(q: String) { _query.value = q }
}
