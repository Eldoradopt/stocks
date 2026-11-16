package com.example.stocks.ui.watchlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stocks.ui.components.BazaarItemCard
import org.koin.androidx.compose.koinViewModel

val WATCHLIST_CATEGORIES = listOf(
    "Followed",
    "Trending",
    "Blue Chips",
    "Gemstones",
    "Farming",
    "Slayer"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    onItemClick: (String) -> Unit,
    viewModel: WatchlistViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Market Watchlist", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Category Chips Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(WATCHLIST_CATEGORIES) { category ->
                    FilterChip(
                        selected = state.selectedCategory == category,
                        onClick = { viewModel.selectCategory(category) },
                        label = {
                            Text(
                                when (category) {
                                    "Followed" -> "⭐ Followed"
                                    "Trending" -> "🔥 Trending"
                                    "Blue Chips" -> "📦 Blue Chips"
                                    "Gemstones" -> "💎 Gemstones"
                                    "Farming" -> "🌾 Farming"
                                    "Slayer" -> "⚔️ Slayer"
                                    else -> category
                                }
                            )
                        }
                    )
                }
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (state.selectedCategory == "Followed")
                                "Your watchlist is empty."
                            else
                                "No items in this category.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (state.selectedCategory == "Followed")
                                "Browse Trending, Blue Chips, or Gemstones tabs above and tap the ⭐ to follow items!"
                            else
                                "Check your internet connection or try again later.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(state.items, key = { it.tag }) { item ->
                        val isFollowed = state.followedTags.contains(item.tag)
                        BazaarItemCard(
                            item = item,
                            onClick = { onItemClick(item.tag) },
                            isFollowed = isFollowed,
                            onToggleFollow = { viewModel.toggleFollow(item.tag) },
                        )
                    }
                }
            }
        }
    }
}
