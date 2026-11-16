package com.example.stocks.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stocks.ui.components.BazaarItemCard
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onItemClick: (String) -> Unit,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val query   by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Search Bazaar") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("GLOSSY_GEMSTONE…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
            )
            LazyColumn {
                items(results, key = { it.tag }) { item ->
                    BazaarItemCard(item = item, onClick = { onItemClick(item.tag) })
                }
            }
        }
    }
}
