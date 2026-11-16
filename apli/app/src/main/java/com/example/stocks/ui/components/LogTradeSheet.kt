package com.example.stocks.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.stocks.data.local.db.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogTradeSheet(
    initialItemTag: String = "",
    marketPrice: Double = 0.0,
    onDismiss: () -> Unit,
    onConfirm: (tag: String, type: TransactionType, quantity: Long, price: Double, notes: String) -> Unit,
) {
    var itemTag by remember { mutableStateOf(initialItemTag) }
    var selectedType by remember { mutableStateOf(TransactionType.BUY) }
    var quantityText by remember { mutableStateOf("1") }
    var priceText by remember { mutableStateOf(if (marketPrice > 0) marketPrice.toString() else "") }
    var notesText by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Log Trade / Add Position",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(16.dp))

            // Buy / Sell Segmented Buttons
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { selectedType = TransactionType.BUY },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedType == TransactionType.BUY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selectedType == TransactionType.BUY) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                ) {
                    Text("BUY")
                }
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = { selectedType = TransactionType.SELL },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedType == TransactionType.SELL) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (selectedType == TransactionType.SELL) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                ) {
                    Text("SELL")
                }
            }

            Spacer(Modifier.height(16.dp))

            // Item Tag input
            OutlinedTextField(
                value = itemTag,
                onValueChange = { itemTag = it.uppercase().replace(' ', '_') },
                label = { Text("Item Tag") },
                placeholder = { Text("e.g. GLOSSY_GEMSTONE") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))

            // Quantity input
            OutlinedTextField(
                value = quantityText,
                onValueChange = { quantityText = it },
                label = { Text("Quantity") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // Fast Stack Chips
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1L, 16L, 64L, 128L, 1024L).forEach { stackSize ->
                    FilterChip(
                        selected = quantityText == stackSize.toString(),
                        onClick = { quantityText = stackSize.toString() },
                        label = { Text(if (stackSize == 64L) "64 (Stack)" else "$stackSize") }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Price input
            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it },
                label = { Text("Price per unit (coins)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                trailingIcon = {
                    if (marketPrice > 0) {
                        TextButton(onClick = { priceText = marketPrice.toString() }) {
                            Text("Market")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))

            // Notes
            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("Notes (optional)") },
                placeholder = { Text("e.g. Bought for Diana event flip") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // Total estimate
            val qty = quantityText.toLongOrNull() ?: 0L
            val prc = priceText.toDoubleOrNull() ?: 0.0
            val total = qty * prc

            Spacer(Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Transaction:")
                    Text(
                        "${total.toSkyCoins()} coins",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (itemTag.isNotBlank() && qty > 0 && prc > 0) {
                        onConfirm(itemTag, selectedType, qty, prc, notesText)
                    }
                },
                enabled = itemTag.isNotBlank() && qty > 0 && prc > 0,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (selectedType == TransactionType.BUY) "Confirm Buy" else "Confirm Sell")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
