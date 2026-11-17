package com.example.stocks.ui.boss

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stocks.data.local.db.BossDropEntity
import com.example.stocks.data.local.db.BossStats
import com.example.stocks.data.repository.BossDropRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BossTrackerUiState(
    val recentDrops: List<BossDropEntity> = emptyList(),
    val unsoldDrops: List<BossDropEntity> = emptyList(),
    val soldDrops: List<BossDropEntity> = emptyList(),
    val totalDrops: Int = 0,
    val unsoldValue: Double = 0.0,
    val totalProfit: Double = 0.0,
    val bossStats: List<BossStats> = emptyList(),
)

class BossTrackerViewModel(
    private val bossDropRepo: BossDropRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BossTrackerUiState())
    val uiState: StateFlow<BossTrackerUiState> = _uiState.asStateFlow()

    init {
        loadAllData()
    }

    private fun loadAllData() = viewModelScope.launch {
        combine(
            bossDropRepo.getAllDrops(),
            bossDropRepo.getUnsoldDrops(),
            bossDropRepo.getSoldDrops(),
        ) { all, unsold, sold ->
            BossTrackerUiState(
                recentDrops = all.take(50),
                unsoldDrops = unsold,
                soldDrops = sold,
                totalDrops = all.size,
                unsoldValue = unsold.sumOf { it.dropValue },
                totalProfit = sold.sumOf { it.profit },
                bossStats = bossDropRepo.getStatsByBoss()
            )
        }.collect { state ->
            _uiState.value = state
        }
    }

    fun recordDrop(
        bossName: String,
        dropTag: String,
        dropDisplayName: String,
        quantity: Long,
        price: Double,
        tier: Int
    ) = viewModelScope.launch {
        bossDropRepo.recordDrop(bossName, dropTag, dropDisplayName, quantity, price, tier)
    }

    fun markAsSold(dropId: Long, soldPrice: Double) = viewModelScope.launch {
        bossDropRepo.markAsSold(dropId, soldPrice)
    }

    fun deleteDrop(dropId: Long) = viewModelScope.launch {
        bossDropRepo.deleteDrop(dropId)
    }
}
