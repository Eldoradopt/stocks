package com.example.stocks.ui.portfolio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stocks.data.local.db.TransactionEntity
import com.example.stocks.data.local.db.TransactionType
import com.example.stocks.domain.model.CoopProfile
import com.example.stocks.domain.model.Holding
import com.example.stocks.domain.model.SacksBreakdown
import com.example.stocks.ui.components.ChangeBadge
import com.example.stocks.ui.components.LinkAccountSheet
import com.example.stocks.ui.components.LogTradeSheet
import com.example.stocks.ui.components.toSkyCoins
import com.example.stocks.ui.theme.DarkCard
import com.example.stocks.ui.theme.DarkCardBorder
import com.example.stocks.ui.theme.EmeraldProfit
import com.example.stocks.ui.theme.HypixelGold
import com.example.stocks.ui.theme.CrimsonLoss
import com.example.stocks.ui.theme.HypixelBlue
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(
    onItemClick: (String) -> Unit,
    viewModel: PortfolioViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var showTradeSheet by remember { mutableStateOf(false) }
    var showAccountSheet by remember { mutableStateOf(false) }

    // Close sheet when linking succeeds
    LaunchedEffect(state.userSettings.linkedIgn) {
        if (state.userSettings.linkedIgn.isNotBlank() && showAccountSheet) {
            showAccountSheet = false
        }
    }

    val isHidden = state.userSettings.isBalanceHidden

    fun formatBalance(amount: Double): String {
        return if (isHidden) "••••••••" else "${amount.toSkyCoins()} coins"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("SkyBank", fontWeight = FontWeight.Bold)
                        if (state.userSettings.linkedIgn.isNotBlank()) {
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = EmeraldProfit.copy(alpha = 0.15f),
                                shape = MaterialTheme.shapes.extraSmall
                            ) {
                                Text(
                                    text = state.userSettings.linkedIgn,
                                    color = EmeraldProfit,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    Button(
                        onClick = { showAccountSheet = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HypixelBlue,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(if (state.userSettings.linkedIgn.isNotBlank()) "Manage Account" else "Link Account", style = MaterialTheme.typography.labelSmall)
                    }
                    if (state.userSettings.linkedIgn.isNotBlank()) {
                        IconButton(onClick = viewModel::toggleHideBalance) {
                            Icon(
                                imageVector = if (isHidden) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = "Toggle Balance Privacy",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        IconButton(onClick = viewModel::refreshBalances) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize()
        ) {
            // Co-op / Profile Switcher Chips
            if (state.coopProfiles.size > 1) {
                item {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.coopProfiles) { profile ->
                            val isSelected = profile.profileId == state.userSettings.selectedProfileId
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.switchProfile(profile.profileId) },
                                label = { Text("🌿 ${profile.cuteName} (Co-op)") }
                            )
                        }
                    }
                }
            }

            // 1. Hero Net Worth Card
            item {
                BankHeroCard(
                    totalNetWorth = formatBalance(state.totalNetWorth),
                    unrealizedPnL = if (isHidden) "••••" else state.bazaarUnrealizedPnL.toSkyCoins(),
                    returnPct = state.bazaarReturnPct,
                    isHidden = isHidden,
                    linkedAccount = state.userSettings.linkedIgn.isNotBlank(),
                    profileName = state.coopProfiles.firstOrNull { it.profileId == state.userSettings.selectedProfileId }?.cuteName,
                    onLinkAccount = { showAccountSheet = true }
                )
            }

            // 2. Quick Action Pills
            item {
                QuickActionRow(
                    onLogTrade = { showTradeSheet = true },
                    onRefresh = viewModel::refreshBalances,
                )
            }

            // 3. Asset Breakdown Card (Purse + Bank + Sacks + Bazaar)
            item {
                AssetBreakdownCard(
                    purseCoins = formatBalance(state.purseCoins),
                    bankCoins = formatBalance(state.bankCoins),
                    sacksCoins = formatBalance(state.sacksCoins),
                    investments = formatBalance(state.bazaarCurrentValue),
                )
            }

            // 4. Sacks & Slayer Items Valuation Stash
            state.sacksBreakdown?.let { sacks ->
                item {
                    SacksValuationCard(
                        sacks = sacks,
                        isHidden = isHidden,
                        onItemClick = onItemClick
                    )
                }
            }

            // 5. Holdings Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Bazaar Holdings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${state.holdings.size} positions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (state.holdings.isEmpty()) {
                item {
                    EmptySectionPlaceholder(
                        title = "No manual investments logged",
                        subtitle = "Tap '+ Log Trade' to add items you are holding or flipping.",
                        onAction = { showTradeSheet = true }
                    )
                }
            } else {
                items(state.holdings, key = { it.itemTag }) { holding ->
                    HoldingRowItem(
                        holding = holding,
                        isHidden = isHidden,
                        onClick = { onItemClick(holding.itemTag) },
                    )
                }
            }

            // 6. Recent Transaction History
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Trade Ledger / Statements",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (state.recentTransactions.isEmpty()) {
                item {
                    Text(
                        "No trades logged yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            } else {
                items(state.recentTransactions, key = { it.id }) { tx ->
                    TransactionRowItem(transaction = tx, isHidden = isHidden)
                }
            }

            item {
                Spacer(Modifier.height(40.dp))
            }
        }

        // Bottom Sheets
        if (showTradeSheet) {
            LogTradeSheet(
                onDismiss = { showTradeSheet = false },
                onConfirm = { tag, type, qty, price, notes ->
                    if (type == TransactionType.BUY) {
                        viewModel.recordBuy(tag, qty, price, notes)
                    } else {
                        viewModel.recordSell(tag, qty, price, notes)
                    }
                    showTradeSheet = false
                },
                allItems = viewModel.allItems.collectAsState().value.values.toList()
            )
        }

        if (showAccountSheet) {
            LinkAccountSheet(
                currentIgn = state.userSettings.linkedIgn,
                isLoading = state.isLinkingAccount,
                errorMessage = state.linkAccountError,
                onDismiss = { 
                    if (!state.isLinkingAccount) showAccountSheet = false 
                },
                onLink = { ign, key, shiiyuUrl ->
                    viewModel.linkAccount(ign, key, shiiyuUrl)
                    // Don't dismiss immediately - let the user see loading/error state
                    // Sheet will close on success or after manual dismiss
                },
                onUnlink = {
                    viewModel.unlinkAccount()
                    showAccountSheet = false
                }
            )
        }
    }
}

@Composable
private fun BankHeroCard(
    totalNetWorth: String,
    unrealizedPnL: String,
    returnPct: Double,
    isHidden: Boolean,
    linkedAccount: Boolean,
    profileName: String?,
    onLinkAccount: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(1.dp, DarkCardBorder, CardDefaults.shape),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "TOTAL NET WORTH",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.2.sp
                    )
                    if (linkedAccount && profileName != null) {
                        Text(
                            "Profile: $profileName",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldProfit,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                if (!linkedAccount) {
                    Button(
                        onClick = onLinkAccount,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HypixelBlue,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Link Account", style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    Button(
                        onClick = onLinkAccount,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = HypixelBlue
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Manage", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = totalNetWorth,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = HypixelGold,
            )

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                ChangeBadge(pct = returnPct)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "$unrealizedPnL unrealized P&L",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun QuickActionRow(
    onLogTrade: () -> Unit,
    onRefresh: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickActionButton(
            label = "Log Trade",
            icon = Icons.Default.AddCircle,
            onClick = onLogTrade,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            label = "Sync",
            icon = Icons.Default.Sync,
            onClick = onRefresh,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = DarkCard,
        modifier = modifier.border(1.dp, DarkCardBorder, MaterialTheme.shapes.medium)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = HypixelBlue,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AssetBreakdownCard(
    purseCoins: String,
    bankCoins: String,
    sacksCoins: String,
    investments: String,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.dp, DarkCardBorder, CardDefaults.shape),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            AssetColumn("👛 Purse", purseCoins)
            AssetColumn("🏦 Bank", bankCoins)
            AssetColumn("🎒 Sacks", sacksCoins)
            AssetColumn("📈 Portfolio", investments)
        }
    }
}

@Composable
private fun SacksValuationCard(
    sacks: SacksBreakdown,
    isHidden: Boolean,
    onItemClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, DarkCardBorder, CardDefaults.shape),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "🎒 Sacks & Slayer Stash",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isHidden) "••••" else "${sacks.totalSacksValue.toSkyCoins()} coins",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = HypixelGold
                )
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = DarkCardBorder)
            Spacer(Modifier.height(10.dp))

            // Sub-category totals
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("⚔️ Slayer Stash", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if (isHidden) "••••" else sacks.slayerDropsValue.toSkyCoins(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
                Column {
                    Text("💎 Gemstones", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if (isHidden) "••••" else sacks.gemstonesValue.toSkyCoins(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("🌾 Farming", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if (isHidden) "••••" else sacks.farmingValue.toSkyCoins(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }

            if (sacks.topValuedItems.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("Most Valuable Items:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))

                sacks.topValuedItems.take(4).forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable { onItemClick(item.tag) },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${item.quantity}x ${item.displayName}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            if (isHidden) "••••" else "${item.totalValue.toSkyCoins()} coins",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = HypixelGold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AssetColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = HypixelGold)
    }
}

@Composable
private fun HoldingRowItem(
    holding: Holding,
    isHidden: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .border(1.dp, DarkCardBorder, CardDefaults.shape),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    holding.itemTag.replace('_', ' '),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${holding.quantity}x @ ${if (isHidden) "•••" else holding.avgBuyPrice.toSkyCoins()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = HypixelBlue,
                    fontWeight = FontWeight.Medium
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (isHidden) "••••" else "${holding.currentValue.toSkyCoins()} coins",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = HypixelGold
                )
                ChangeBadge(holding.returnPct)
            }
        }
    }
}

@Composable
private fun TransactionRowItem(
    transaction: TransactionEntity,
    isHidden: Boolean,
) {
    val isBuy = transaction.type == TransactionType.BUY
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .border(1.dp, DarkCardBorder, CardDefaults.shape),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (isBuy) EmeraldProfit.copy(alpha = 0.2f) else CrimsonLoss.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.extraSmall,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isBuy) EmeraldProfit.copy(alpha = 0.5f) else CrimsonLoss.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = if (isBuy) "BUY" else "SELL",
                    color = if (isBuy) EmeraldProfit else CrimsonLoss,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    transaction.itemTag.replace('_', ' '),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    dateFormat.format(Date(transaction.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isHidden) "••••" else "${if (isBuy) "-" else "+"}${transaction.totalCoins.toSkyCoins()} coins",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isBuy) MaterialTheme.colorScheme.onSurface else EmeraldProfit
                )
                Text(
                    text = "${transaction.quantity}x @ ${transaction.pricePerUnit.toSkyCoins()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = HypixelBlue,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun EmptySectionPlaceholder(
    title: String,
    subtitle: String,
    onAction: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, DarkCardBorder, CardDefaults.shape),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onAction) {
                Text("Log First Trade")
            }
        }
    }
}
