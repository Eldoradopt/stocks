package com.example.stocks.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stocks.data.repository.PortfolioRepository
import com.example.stocks.domain.model.Holding
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PortfolioUiState(
    val holdings: List<Holding> = emptyList(),
    val totalInvested: Double = 0.0,
    val totalCurrentValue: Double = 0.0,
    val totalUnrealizedPnL: Double = 0.0,
    val totalReturnPct: Double = 0.0,
)

class PortfolioViewModel(
    private val repo: PortfolioRepository,
) : ViewModel() {

    val uiState: StateFlow<PortfolioUiState> = repo.observeLiveHoldings()
        .map { holdings ->
            val invested = holdings.sumOf { it.costBasis }
            val value    = holdings.sumOf { it.currentValue }
            PortfolioUiState(
                holdings            = holdings,
                totalInvested       = invested,
                totalCurrentValue   = value,
                totalUnrealizedPnL  = value - invested,
                totalReturnPct      = if (invested > 0) ((value - invested) / invested) * 100.0 else 0.0,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PortfolioUiState())

    fun removeHolding(tag: String) = viewModelScope.launch {
        repo.removeHolding(tag)
    }
}
