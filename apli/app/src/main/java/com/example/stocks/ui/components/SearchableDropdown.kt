package com.example.stocks.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.stocks.domain.model.BazaarItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableDropdown(
    label: String,
    placeholder: String,
    selectedTag: String,
    allItems: List<BazaarItem>,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    
    val selectedItem = allItems.find { it.tag == selectedTag }
    val displayText = selectedItem?.displayName ?: selectedTag
    val filteredItems = remember(searchText, allItems) {
        if (searchText.isBlank()) {
            // When search is empty, show items sorted by price/volume or just first 50
            allItems.take(50)
        } else {
            allItems.filter { 
                it.tag.contains(searchText, ignoreCase = true) || 
                it.displayName.contains(searchText, ignoreCase = true) 
            }.sortedBy { 
                // Prioritize exact matches in tag, then prefix matches, then contains
                when {
                    it.tag.equals(searchText, ignoreCase = true) -> 0
                    it.tag.startsWith(searchText, ignoreCase = true) -> 1
                    it.displayName.startsWith(searchText, ignoreCase = true) -> 2
                    it.tag.contains(searchText, ignoreCase = true) -> 3
                    it.displayName.contains(searchText, ignoreCase = true) -> 4
                    else -> 5
                }
            }.take(50)
        }
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = if (expanded) searchText else displayText,
            onValueChange = { 
                if (expanded) {
                    searchText = it
                } else {
                    expanded = true
                    searchText = it
                }
            },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            trailingIcon = {
                Row {
                    if (selectedTag.isNotBlank() && !expanded) {
                        IconButton(onClick = { onItemSelected("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                    }
                }
            },
            leadingIcon = if (expanded) {
                { Icon(Icons.Default.Search, contentDescription = null) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        if (expanded) {
            Popup(
                alignment = Alignment.TopStart,
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    LazyColumn {
                        if (filteredItems.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "No items found",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(filteredItems) { item ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                item.displayName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (item.tag.equals(searchText, ignoreCase = true)) FontWeight.Bold else FontWeight.Normal
                                            )
                                            Text(
                                                item.tag,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        onItemSelected(item.tag)
                                        expanded = false
                                        searchText = ""
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
