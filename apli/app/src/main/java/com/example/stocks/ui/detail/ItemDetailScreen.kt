package com.example.stocks.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.stocks.ui.components.ChangeBadge
import com.example.stocks.ui.components.PriceChart
import com.example.stocks.ui.components.toSkyCoins
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
    var watched by remember { mutableStateOf(false) }
    var showAddSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.item?.displayName ?: itemTag) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        watched = !watched
                        if (watched) viewModel.addToWatchlist() else viewModel.removeFromWatchlist()
                    }) {
                        Icon(
                            if (watched) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Watch",
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Add to Portfolio") },
                icon = { Icon(Icons.Default.Bookmark, null) },
                onClick = { showAddSheet = true },
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
                // Price row
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        item.sellPrice.toSkyCoins(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    ChangeBadge(pct = state.change7dPct)
                }

                // Buy / Sell spread info
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    StatItem("Buy", item.buyPrice.toSkyCoins())
                    StatItem("Sell", item.sellPrice.toSkyCoins())
                    StatItem("Spread", "${"%.1f".format(item.spreadPct)}%")
                }

                // 7-day chart
                if (state.history.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text("7-Day Price", modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.labelLarge)
                    PriceChart(
                        pricePoints = state.history,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(horizontal = 8.dp),
                    )
                }

                // Min / Max
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    StatItem("7d Low", state.minPrice.toSkyCoins())
                    StatItem("7d High", state.maxPrice.toSkyCoins())
                }

                // Volume
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    StatItem("Buy Vol/wk", item.buyMovingWeek.toDouble().toSkyCoins())
                    StatItem("Sell Vol/wk", item.sellMovingWeek.toDouble().toSkyCoins())
                }
            }

            state.error?.let {
                Text("Error: $it", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                TextButton(onClick = viewModel::load, modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("Retry")
                }
            }

            Spacer(Modifier.height(80.dp)) // FAB clearance
        }

        if (showAddSheet) {
            AddHoldingSheet(
                onDismiss = { showAddSheet = false },
                onConfirm = { qty, price ->
                    viewModel.upsertHolding(qty, price)
                    showAddSheet = false
                }
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddHoldingSheet(
    onDismiss: () -> Unit,
    onConfirm: (Long, Double) -> Unit,
) {
    var qty   by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            Text("Add Holding", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = qty,
                onValueChange = { qty = it },
                label = { Text("Quantity") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Avg Buy Price (coins)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val q = qty.toLongOrNull()   ?: return@Button
                    val p = price.toDoubleOrNull() ?: return@Button
                    onConfirm(q, p)
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save") }
            Spacer(Modifier.height(24.dp))
        }
    }
}
