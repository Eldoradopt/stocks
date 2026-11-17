package com.example.stocks.ui.detail

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stocks.ui.components.*
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
    var showAlertSheet by remember { mutableStateOf(false) }

    val isPositive = state.changePct >= 0

    Scaffold(
        topBar = {
            // Revolut-Style Header Action Bar (Circular buttons)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Back button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .border(1.dp, DarkCardBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Action buttons: Alert Bell + Star
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { showAlertSheet = true },
                        modifier = Modifier
                            .size(40.dp)
                            .border(1.dp, DarkCardBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (state.existingAlert != null) Icons.Default.Notifications else Icons.Outlined.NotificationsNone,
                            contentDescription = "Set Alert",
                            tint = if (state.existingAlert != null) EmeraldProfit else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = viewModel::toggleWatchlist,
                        modifier = Modifier
                            .size(40.dp)
                            .border(1.dp, DarkCardBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (state.isWatched) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Follow",
                            tint = if (state.isWatched) HypixelGold else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Sticky Revolut Pill Action Button at the bottom
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = { showTradeSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        "Instant Flip / Log Trade",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    ) { padding ->
        if (state.isLoading && state.item == null) {
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
                // Revolut Asset Header: TICKER · Name + Category Subtitle
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text(
                        "${item.tag} · ${item.displayName}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        state.categorySubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(14.dp))

                    // Hero Price & Change Delta
                    Text(
                        "${item.sellPrice.toSkyCoins()} coins",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ChangeBadge(pct = state.changePct)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Last ${state.selectedTimeRange.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Revolut Dual-Mode Interactive Chart
                RevolutChart(
                    pricePoints = state.history,
                    isPositiveTrend = isPositive,
                    selectedRange = state.selectedTimeRange,
                    onRangeSelected = viewModel::load,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // Flipper Arbitrage Profit Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
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
                                "⚡ Flip Profit Potential",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "+${state.flipHourlyProfit.toSkyCoins()}/hr",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldProfit
                            )
                        }

                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider(color = DarkCardBorder)
                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem("Net Margin (After 1.125% Tax)", "+${state.flipNetMargin.toSkyCoins()}")
                            StatItem("Flip ROI", "${"%.2f".format(state.flipRoiPct)}%")
                            StatItem("Hourly Volume", "${((item.sellMovingWeek) / (7 * 24)).toDouble().toSkyCoins()}/hr")
                        }
                    }
                }

                // Market Liquidity / Order Book Spread Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .border(1.dp, DarkCardBorder, CardDefaults.shape),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Market Spread & Orders",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem("Instant Buy", item.buyPrice.toSkyCoins())
                            StatItem("Instant Sell", item.sellPrice.toSkyCoins())
                            StatItem("Spread", "${"%.1f".format(item.spreadPct)}%")
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem("Weekly Buy Vol", item.buyMovingWeek.toDouble().toSkyCoins())
                            StatItem("Weekly Sell Vol", item.sellMovingWeek.toDouble().toSkyCoins())
                            StatItem("Total Orders", (item.buyVolume + item.sellVolume).toDouble().toSkyCoins())
                        }
                    }
                }
            }

            state.error?.let {
                Text(
                    "Error loading market data: $it",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
                TextButton(
                    onClick = { viewModel.load() },
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text("Retry")
                }
            }

            Spacer(Modifier.height(80.dp))
        }

        // Trade Sheet
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

        // Price Alert Sheet
        if (showAlertSheet) {
            SetAlertSheet(
                itemTag = itemTag,
                currentPrice = state.item?.sellPrice ?: 0.0,
                onDismiss = { showAlertSheet = false },
                onSaveAlert = { high, low ->
                    viewModel.setPriceAlert(high, low)
                    showAlertSheet = false
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
