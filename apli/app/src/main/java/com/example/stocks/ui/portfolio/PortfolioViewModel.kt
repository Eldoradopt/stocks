package com.example.stocks.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stocks.data.local.datastore.UserSettings
import com.example.stocks.data.local.db.TransactionEntity
import com.example.stocks.data.local.db.TransactionType
import com.example.stocks.data.repository.AccountRepository
import com.example.stocks.data.repository.BazaarRepository
import com.example.stocks.data.repository.PortfolioRepository
import com.example.stocks.domain.model.Holding
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BankDashboardUiState(
    val holdings: List<Holding> = emptyList(),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val userSettings: UserSettings = UserSettings(),
    val bazaarInvestedCost: Double = 0.0,
    val bazaarCurrentValue: Double = 0.0,
    val bazaarUnrealizedPnL: Double = 0.0,
    val bazaarReturnPct: Double = 0.0,
    // Bank & Consolidated stats:
    val purseCoins: Double = 0.0,
    val bankCoins: Double = 0.0,
    val totalNetWorth: Double = 0.0,
    val isLinkingAccount: Boolean = false,
    val linkAccountError: String? = null,
)

class PortfolioViewModel(
    private val portfolioRepo: PortfolioRepository,
    private val accountRepo: AccountRepository,
    private val bazaarRepo: BazaarRepository,
) : ViewModel() {

    private val _isLinking = MutableStateFlow(false)
    private val _linkError = MutableStateFlow<String?>(null)
    private val _allItems = MutableStateFlow<Map<String, com.example.stocks.domain.model.BazaarItem>>(emptyMap())

    val allItems: StateFlow<Map<String, com.example.stocks.domain.model.BazaarItem>> = _allItems.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { _allItems.value = bazaarRepo.getAllItems() }
        }
    }

    val uiState: StateFlow<BankDashboardUiState> = combine(
        portfolioRepo.observeLiveHoldings(),
        portfolioRepo.observeRecentTransactions(10),
        accountRepo.userSettings,
        _isLinking,
        _linkError,
    ) { holdings, transactions, settings, linking, error ->
        val invested = holdings.sumOf { it.costBasis }
        val bazaarValue = holdings.sumOf { it.currentValue }
        val pnl = bazaarValue - invested
        val returnPct = if (invested > 0) (pnl / invested) * 100.0 else 0.0

        val purse = settings.cachedPurseCoins
        val bank = settings.cachedBankCoins
        val netWorth = purse + bank + bazaarValue

        BankDashboardUiState(
            holdings = holdings,
            recentTransactions = transactions,
            userSettings = settings,
            bazaarInvestedCost = invested,
            bazaarCurrentValue = bazaarValue,
            bazaarUnrealizedPnL = pnl,
            bazaarReturnPct = returnPct,
            purseCoins = purse,
            bankCoins = bank,
            totalNetWorth = netWorth,
            isLinkingAccount = linking,
            linkAccountError = error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BankDashboardUiState())

    fun toggleHideBalance() = viewModelScope.launch {
        val current = uiState.value.userSettings.isBalanceHidden
        accountRepo.toggleHideBalance(!current)
    }

    fun recordBuy(tag: String, quantity: Long, price: Double, notes: String = "") = viewModelScope.launch {
        portfolioRepo.recordBuy(tag, quantity, price, notes)
    }

    fun recordSell(tag: String, quantity: Long, price: Double, notes: String = "") = viewModelScope.launch {
        portfolioRepo.recordSell(tag, quantity, price, notes)
    }

    fun removeHolding(tag: String) = viewModelScope.launch {
        portfolioRepo.removeHolding(tag)
    }

    fun linkAccount(ign: String, apiKey: String) = viewModelScope.launch {
        _isLinking.value = true
        _linkError.value = null
        val result = accountRepo.linkAccount(ign, apiKey)
        result.onFailure { exception ->
            _linkError.value = exception.message ?: "Could not link account. Please try again."
        }
        _isLinking.value = false
    }

    fun unlinkAccount() = viewModelScope.launch {
        accountRepo.unlinkAccount()
    }

    fun refreshBalances() = viewModelScope.launch {
        accountRepo.refreshLinkedAccount()
    }
}
