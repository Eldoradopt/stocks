package com.example.stocks.ui.flipper

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stocks.domain.model.BazaarFlip
import com.example.stocks.ui.components.ChangeBadge
import com.example.stocks.ui.components.toSkyCoins
import com.example.stocks.ui.theme.DarkCard
import com.example.stocks.ui.theme.DarkCardBorder
import com.example.stocks.ui.theme.EmeraldProfit
import com.example.stocks.ui.theme.HypixelGold
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlipperScreen(
    onItemClick: (String) -> Unit,
    viewModel: FlipperViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoGraph, contentDescription = null, tint = EmeraldProfit)
                        Spacer(Modifier.width(8.dp))
                        Text("Bazaar Flipper", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::loadFlips) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Market")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Sort Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.sortOrder == FlipSortOrder.HOURLY_PROFIT,
                        onClick = { viewModel.setSortOrder(FlipSortOrder.HOURLY_PROFIT) },
                        label = { Text("⚡ Top Coins/hr") }
                    )
                }
                item {
                    FilterChip(
                        selected = state.sortOrder == FlipSortOrder.HIGHEST_ROI,
                        onClick = { viewModel.setSortOrder(FlipSortOrder.HIGHEST_ROI) },
                        label = { Text("📈 Best ROI %") }
                    )
                }
                item {
                    FilterChip(
                        selected = state.sortOrder == FlipSortOrder.NET_MARGIN,
                        onClick = { viewModel.setSortOrder(FlipSortOrder.NET_MARGIN) },
                        label = { Text("💎 Margin/item") }
                    )
                }
                item {
                    FilterChip(
                        selected = state.sortOrder == FlipSortOrder.HIGH_VOLUME,
                        onClick = { viewModel.setSortOrder(FlipSortOrder.HIGH_VOLUME) },
                        label = { Text("🌊 Fast Volume") }
                    )
                }
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(state.flips, key = { it.tag }) { flip ->
                        FlipOpportunityCard(
                            flip = flip,
                            onClick = { onItemClick(flip.tag) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FlipOpportunityCard(
    flip: BazaarFlip,
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
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        flip.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Vol: ${flip.hourlyVolume.toDouble().toSkyCoins()}/hr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "+${flip.hourlyProfit.toSkyCoins()}/hr",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldProfit
                    )
                    ChangeBadge(pct = flip.roiPct)
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = DarkCardBorder)
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Buy Order (Pay)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(flip.sellPrice.toSkyCoins(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
                Column {
                    Text("Sell Order (List)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(flip.buyPrice.toSkyCoins(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Net Margin (After Tax)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "+${flip.netMargin.toSkyCoins()} coins",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = HypixelGold
                    )
                }
            }
        }
    }
}
