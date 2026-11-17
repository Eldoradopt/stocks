package com.example.stocks.ui.bazaar

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stocks.data.local.db.BazaarListingEntity
import com.example.stocks.data.local.db.ListingStatus
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
fun BazaarWatcherScreen(
    viewModel: BazaarWatcherViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var showMarkSoldSheet by remember { mutableStateOf<BazaarListingEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bazaar Watcher", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = EmeraldProfit
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Listing")
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
                    SummaryCard("Active", state.activeCount.toString(), HypixelBlue)
                    SummaryCard("Sold", state.soldCount.toString(), EmeraldProfit)
                    SummaryCard("Profit", state.totalProfit.toSkyCoins(), 
                        if (state.totalProfit >= 0) EmeraldProfit else CrimsonLoss)
                }
            }

            // Tabs
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.selectedTab == Tab.ACTIVE,
                        onClick = { viewModel.setTab(Tab.ACTIVE) },
                        label = { Text("Active") }
                    )
                    FilterChip(
                        selected = state.selectedTab == Tab.SOLD,
                        onClick = { viewModel.setTab(Tab.SOLD) },
                        label = { Text("Sold") }
                    )
                    FilterChip(
                        selected = state.selectedTab == Tab.ALL,
                        onClick = { viewModel.setTab(Tab.ALL) },
                        label = { Text("All") }
                    )
                }
            }

            // Listings
            val listings = when (state.selectedTab) {
                Tab.ACTIVE -> state.activeListings
                Tab.SOLD -> state.soldListings
                Tab.ALL -> state.allListings
            }

            if (listings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No listings yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(listings, key = { it.id }) { listing ->
                    ListingCard(
                        listing = listing,
                        onMarkSold = { showMarkSoldSheet = listing },
                        onCancel = { viewModel.cancelListing(listing.id) }
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }

        // Add Listing Sheet
        if (showAddSheet) {
            AddListingSheet(
                onDismiss = { showAddSheet = false },
                onConfirm = { tag, name, qty, price ->
                    viewModel.createListing(tag, name, qty, price)
                    showAddSheet = false
                }
            )
        }

        // Mark as Sold Sheet
        showMarkSoldSheet?.let { listing ->
            MarkSoldSheet(
                listing = listing,
                onDismiss = { showMarkSoldSheet = null },
                onConfirm = { soldQty, soldPrice ->
                    viewModel.markAsSold(listing.id, soldQty, soldPrice)
                    showMarkSoldSheet = null
                }
            )
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = Modifier
            .border(1.dp, DarkCardBorder, CardDefaults.shape),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
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
private fun ListingCard(
    listing: BazaarListingEntity,
    onMarkSold: () -> Unit,
    onCancel: () -> Unit,
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
                        listing.itemDisplayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "${listing.listedQuantity}x @ ${listing.listedPrice.toSkyCoins()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = HypixelBlue
                    )
                }
                
                when (listing.status) {
                    ListingStatus.ACTIVE -> {
                        Surface(
                            color = HypixelBlue.copy(alpha = 0.15f),
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                "ACTIVE",
                                color = HypixelBlue,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    ListingStatus.SOLD -> {
                        Surface(
                            color = EmeraldProfit.copy(alpha = 0.15f),
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                "SOLD",
                                color = EmeraldProfit,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    else -> {}
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Listed: ${listing.totalListedCost.toSkyCoins()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (listing.status == ListingStatus.SOLD) {
                    Text(
                        "Profit: ${listing.profit.toSkyCoins()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (listing.profit >= 0) EmeraldProfit else CrimsonLoss,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (listing.status == ListingStatus.ACTIVE) {
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
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = CrimsonLoss
                        )
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

enum class Tab {
    ACTIVE, SOLD, ALL
}
