package com.example.stocks.ui.bazaar

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
import com.example.stocks.data.local.db.BazaarListingEntity
import com.example.stocks.ui.components.toSkyCoins

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddListingSheet(
    onDismiss: () -> Unit,
    onConfirm: (tag: String, name: String, quantity: Long, price: Double) -> Unit,
) {
    var itemTag by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1") }
    var priceText by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Add Bazaar Listing",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = itemTag,
                onValueChange = { itemTag = it.uppercase().replace(' ', '_') },
                label = { Text("Item Tag") },
                placeholder = { Text("e.g. GLOSSY_GEMSTONE") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = quantityText,
                onValueChange = { quantityText = it },
                label = { Text("Quantity") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it },
                label = { Text("List Price per unit (coins)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val qty = quantityText.toLongOrNull() ?: 0L
                    val price = priceText.toDoubleOrNull() ?: 0.0
                    if (itemTag.isNotBlank() && qty > 0 && price > 0) {
                        onConfirm(itemTag, itemTag.replace('_', ' '), qty, price)
                    }
                },
                enabled = itemTag.isNotBlank() && quantityText.toLongOrNull() ?: 0L > 0 && priceText.toDoubleOrNull() ?: 0.0 > 0,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Create Listing")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkSoldSheet(
    listing: BazaarListingEntity,
    onDismiss: () -> Unit,
    onConfirm: (soldQuantity: Long, soldPrice: Double) -> Unit,
) {
    var soldQuantityText by remember { mutableStateOf(listing.listedQuantity.toString()) }
    var soldPriceText by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Mark as Sold",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Item: ${listing.itemDisplayName}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "Listed: ${listing.listedQuantity}x @ ${listing.listedPrice.toSkyCoins()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = soldQuantityText,
                onValueChange = { soldQuantityText = it },
                label = { Text("Sold Quantity") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = soldPriceText,
                onValueChange = { soldPriceText = it },
                label = { Text("Average Sell Price per unit") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val qty = soldQuantityText.toLongOrNull() ?: 0L
                    val price = soldPriceText.toDoubleOrNull() ?: 0.0
                    if (qty > 0 && price > 0) {
                        onConfirm(qty, price)
                    }
                },
                enabled = soldQuantityText.toLongOrNull() ?: 0L > 0 && soldPriceText.toDoubleOrNull() ?: 0.0 > 0,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Confirm Sale")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
