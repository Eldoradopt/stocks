package com.example.stocks.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetAlertSheet(
    itemTag: String,
    currentPrice: Double,
    onDismiss: () -> Unit,
    onSaveAlert: (high: Double, low: Double) -> Unit,
) {
    var highPriceText by remember { mutableStateOf(if (currentPrice > 0) "%.1f".format(currentPrice * 1.10) else "") }
    var lowPriceText by remember { mutableStateOf(if (currentPrice > 0) "%.1f".format(currentPrice * 0.90) else "") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
            Text(
                "Set Price Alert",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Get notified in the background when ${itemTag.replace('_', ' ')} reaches your price targets.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            // Current price reference
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Current Sell Price:")
                    Text("${currentPrice.toSkyCoins()} coins", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Target High Price
            OutlinedTextField(
                value = highPriceText,
                onValueChange = { highPriceText = it },
                label = { Text("Notify if price rises above (coins)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Target Low Price
            OutlinedTextField(
                value = lowPriceText,
                onValueChange = { lowPriceText = it },
                label = { Text("Notify if price drops below (coins)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Preset Buttons
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = false,
                    onClick = {
                        highPriceText = "%.1f".format(currentPrice * 1.05)
                        lowPriceText = "%.1f".format(currentPrice * 0.95)
                    },
                    label = { Text("±5%") }
                )
                FilterChip(
                    selected = false,
                    onClick = {
                        highPriceText = "%.1f".format(currentPrice * 1.10)
                        lowPriceText = "%.1f".format(currentPrice * 0.90)
                    },
                    label = { Text("±10%") }
                )
                FilterChip(
                    selected = false,
                    onClick = {
                        highPriceText = "%.1f".format(currentPrice * 1.25)
                        lowPriceText = "%.1f".format(currentPrice * 0.75)
                    },
                    label = { Text("±25%") }
                )
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    val high = highPriceText.toDoubleOrNull() ?: 0.0
                    val low = lowPriceText.toDoubleOrNull() ?: 0.0
                    onSaveAlert(high, low)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Activate Notification Alert")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
