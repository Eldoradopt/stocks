package com.example.stocks.ui.portfolio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stocks.ui.components.ChangeBadge
import com.example.stocks.ui.components.toSkyCoins
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(
    onItemClick: (String) -> Unit,
    viewModel: PortfolioViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Portfolio") })
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            // Summary header
            item {
                PortfolioSummaryCard(state)
            }
            if (state.holdings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "No holdings yet.\nSearch for an item to add one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(state.holdings, key = { it.itemTag }) { holding ->
                    HoldingCard(
                        holding = holding,
                        onClick = { onItemClick(holding.itemTag) },
                        onRemove = { viewModel.removeHolding(holding.itemTag) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PortfolioSummaryCard(state: PortfolioUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Total Value", style = MaterialTheme.typography.labelLarge)
            Text(
                state.totalCurrentValue.toSkyCoins() + " coins",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column {
                    Text("Invested", style = MaterialTheme.typography.labelSmall)
                    Text(state.totalInvested.toSkyCoins(), style = MaterialTheme.typography.bodyMedium)
                }
                Column {
                    Text("Unrealised P&L", style = MaterialTheme.typography.labelSmall)
                    Text(state.totalUnrealizedPnL.toSkyCoins(), style = MaterialTheme.typography.bodyMedium)
                }
                ChangeBadge(pct = state.totalReturnPct)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HoldingCard(
    holding: com.example.stocks.domain.model.Holding,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(holding.itemTag.replace('_', ' '), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("${holding.quantity}x @ ${holding.avgBuyPrice.toSkyCoins()}", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(holding.currentValue.toSkyCoins(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                ChangeBadge(holding.returnPct)
            }
        }
    }
}
