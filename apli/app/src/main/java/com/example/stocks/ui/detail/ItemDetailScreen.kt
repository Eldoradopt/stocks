package com.example.stocks.ui.detail

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stocks.ui.components.ChangeBadge
import com.example.stocks.ui.components.LogTradeSheet
import com.example.stocks.ui.components.PriceChart
import com.example.stocks.ui.components.toSkyCoins
import com.example.stocks.ui.theme.DarkCard
import com.example.stocks.ui.theme.DarkCardBorder
import com.example.stocks.ui.theme.EmeraldProfit
import com.example.stocks.ui.theme.HypixelGold
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemTag: String,
    onBack: () -> Unit,
    viewModel: ItemDetailViewModel = koinViewModel(parameters = { parametersOf(itemTag) }),
) {
    val state by viewModel.uiState.collectAsState()
    var showTradeSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.item?.displayName ?: itemTag.replace('_', ' '), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::toggleWatchlist) {
                        Icon(
                            imageVector = if (state.isWatched) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Watchlist",
                            tint = if (state.isWatched) HypixelGold else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Log Trade") },
                icon = { Icon(Icons.Default.Add, null) },
                onClick = { showTradeSheet = true },
                containerColor = EmeraldProfit,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            state.item?.let { item ->
                // Price & 7d change row
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .border(1.dp, DarkCardBorder, CardDefaults.shape),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "CURRENT SELL PRICE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            ChangeBadge(pct = state.change7dPct)
                        }

                        Spacer(Modifier.height(6.dp))

                        Text(
                            "${item.sellPrice.toSkyCoins()} coins",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = HypixelGold,
                        )

                        Spacer(Modifier.height(14.dp))
                        Divider(color = DarkCardBorder)
                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem("Instant Buy", item.buyPrice.toSkyCoins())
                            StatItem("Instant Sell", item.sellPrice.toSkyCoins())
                            StatItem("Spread", "${"%.1f".format(item.spreadPct)}%")
                        }
                    }
                }

                // 7-day chart
                if (state.history.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .border(1.dp, DarkCardBorder, CardDefaults.shape),
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "7-Day Price History",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(12.dp))
                            PriceChart(
                                pricePoints = state.history,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatItem("7d Low", state.minPrice.toSkyCoins())
                                StatItem("7d High", state.maxPrice.toSkyCoins())
                            }
                        }
                    }
                }

                // Volume Statistics
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .border(1.dp, DarkCardBorder, CardDefaults.shape),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Market Liquidity",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem("Buy Vol/wk", item.buyMovingWeek.toDouble().toSkyCoins())
                            StatItem("Sell Vol/wk", item.sellMovingWeek.toDouble().toSkyCoins())
                            StatItem("Total Orders", (item.buyVolume + item.sellVolume).toDouble().toSkyCoins())
                        }
                    }
                }
            }

            state.error?.let {
                Text(
                    "Error: $it",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
                TextButton(
                    onClick = viewModel::load,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text("Retry")
                }
            }

            Spacer(Modifier.height(90.dp)) // FAB clearance
        }

        if (showTradeSheet) {
            LogTradeSheet(
                initialItemTag = itemTag,
                marketPrice = state.item?.sellPrice ?: 0.0,
                onDismiss = { showTradeSheet = false },
                onConfirm = { _, type, qty, price, notes ->
                    viewModel.recordTrade(type, qty, price, notes)
                    showTradeSheet = false
                }
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
