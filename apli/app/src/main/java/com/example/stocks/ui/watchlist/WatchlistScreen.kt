package com.example.stocks.ui.watchlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.stocks.ui.components.BazaarItemCard
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    onItemClick: (String) -> Unit,
    viewModel: WatchlistViewModel = koinViewModel(),
) {
    val items by viewModel.watchlistItems.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Watchlist") }) }) { padding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Your watchlist is empty.\nSearch for items to track.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(contentPadding = padding) {
                items(items, key = { it.tag }) { item ->
                    BazaarItemCard(item = item, onClick = { onItemClick(item.tag) })
                }
            }
        }
    }
}
