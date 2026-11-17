package com.example.stocks.ui.boss

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stocks.data.local.db.BossDropEntity
import com.example.stocks.data.local.db.BossStats
import com.example.stocks.ui.theme.DarkCard
import com.example.stocks.ui.theme.DarkCardBorder
import com.example.stocks.ui.theme.EmeraldProfit
import com.example.stocks.ui.theme.CrimsonLoss
import com.example.stocks.ui.theme.HypixelGold
import com.example.stocks.ui.theme.HypixelBlue
import com.example.stocks.ui.components.toSkyCoins
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BossTrackerScreen(
    viewModel: BossTrackerViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var showMarkSoldSheet by remember { mutableStateOf<BossDropEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Boss Tracker", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = HypixelGold
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Drop")
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize()
        ) {
            // Summary Cards
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryCard("Total Drops", state.totalDrops.toString(), HypixelBlue, Modifier.weight(1f))
                    SummaryCard("Unsold", state.unsoldValue.toSkyCoins(), HypixelGold, Modifier.weight(1f))
                    SummaryCard("Profit", state.totalProfit.toSkyCoins(), 
                        if (state.totalProfit >= 0) EmeraldProfit else CrimsonLoss, Modifier.weight(1f))
                }
            }

            // Boss Stats
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Boss Statistics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            items(state.bossStats, key = { it.bossName }) { stat ->
                BossStatCard(stat)
            }

            // Recent Drops
            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Recent Drops",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            if (state.recentDrops.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No drops recorded yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(state.recentDrops, key = { it.id }) { drop ->
                    BossDropCard(
                        drop = drop,
                        onMarkSold = { showMarkSoldSheet = drop },
                        onDelete = { viewModel.deleteDrop(drop.id) }
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }

        // Add Drop Sheet
        if (showAddSheet) {
            AddBossDropSheet(
                onDismiss = { showAddSheet = false },
                onConfirm = { boss, tag, name, qty, price, tier ->
                    viewModel.recordDrop(boss, tag, name, qty, price, tier)
                    showAddSheet = false
                }
            )
        }

        // Mark as Sold Sheet
        showMarkSoldSheet?.let { drop ->
            MarkBossSoldSheet(
                drop = drop,
                onDismiss = { showMarkSoldSheet = null },
                onConfirm = { soldPrice ->
                    viewModel.markAsSold(drop.id, soldPrice)
                    showMarkSoldSheet = null
                }
            )
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .border(1.dp, DarkCardBorder, CardDefaults.shape),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun BossStatCard(stat: BossStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .border(1.dp, DarkCardBorder, CardDefaults.shape),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                stat.bossName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "${stat.count} drops",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                stat.totalValue.toSkyCoins(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = HypixelGold
            )
        }
    }
}

@Composable
private fun BossDropCard(
    drop: BossDropEntity,
    onMarkSold: () -> Unit,
    onDelete: () -> Unit,
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .border(1.dp, DarkCardBorder, CardDefaults.shape),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        drop.bossName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        drop.dropDisplayName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "${drop.quantity}x @ ${drop.unitPrice.toSkyCoins()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = HypixelBlue
                    )
                }
                
                if (drop.tier > 1) {
                    Surface(
                        color = HypixelGold.copy(alpha = 0.15f),
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            "T${drop.tier}",
                            color = HypixelGold,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Value: ${drop.dropValue.toSkyCoins()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (drop.sold) {
                    Text(
                        "Profit: ${drop.profit.toSkyCoins()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (drop.profit >= 0) EmeraldProfit else CrimsonLoss,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (!drop.sold) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onMarkSold,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = EmeraldProfit
                        )
                    ) {
                        Icon(Icons.Default.Sell, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Mark Sold")
                    }
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = CrimsonLoss
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
            }
        }
    }
}
