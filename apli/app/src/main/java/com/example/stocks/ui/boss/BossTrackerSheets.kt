package com.example.stocks.ui.boss

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
import com.example.stocks.data.local.db.BossDropEntity
import com.example.stocks.ui.theme.HypixelGold
import com.example.stocks.ui.components.toSkyCoins
import androidx.compose.material3.ExposedDropdownMenuAnchorType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBossDropSheet(
    onDismiss: () -> Unit,
    onConfirm: (boss: String, tag: String, name: String, quantity: Long, price: Double, tier: Int) -> Unit,
) {
    var bossName by remember { mutableStateOf("") }
    var itemTag by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1") }
    var priceText by remember { mutableStateOf("") }
    var tier by remember { mutableStateOf(1) }

    val bossList = listOf(
        "Broodmother",
        "Blood God",
        "Voidgloom Seraph",
        "Storm",
        "Enderman Slayer",
        "Revenant Horror",
        "Tarantula Broodfather",
        "Sadan",
        "Witherlord",
        "Inferno Demonlord",
        "Kuudra",
        "Phantom Rodholder",
        "The Watcher",
        "Necron",
        "Maxor",
        "Riftstalker Bloodfiend",
        "Sven Packmaster",
        "Magma Boss",
        "Ocean's Monarch",
        "Inferno Lord",
        "Slayer Bosses"
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Record Boss Drop",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(16.dp))

            // Boss Selection
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = bossName,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Boss") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.exposedDropdownSize(true)
                ) {
                    bossList.forEach { boss ->
                        DropdownMenuItem(
                            text = { Text(boss) },
                            onClick = {
                                bossName = boss
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = itemTag,
                onValueChange = { itemTag = it.uppercase().replace(' ', '_') },
                label = { Text("Item Tag") },
                placeholder = { Text("e.g. TARANTULA_SILK") },
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
                label = { Text("Market Price per unit (coins)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))

            // Tier Selection
            Text(
                "Drop Tier",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1, 2, 3, 4, 5).forEach { t ->
                    FilterChip(
                        selected = tier == t,
                        onClick = { tier = t },
                        label = { Text("T$t") },
                        modifier = Modifier.height(32.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val qty = quantityText.toLongOrNull() ?: 0L
                    val price = priceText.toDoubleOrNull() ?: 0.0
                    if (bossName.isNotBlank() && itemTag.isNotBlank() && qty > 0 && price > 0) {
                        onConfirm(bossName, itemTag, itemTag.replace('_', ' '), qty, price, tier)
                    }
                },
                enabled = bossName.isNotBlank() && itemTag.isNotBlank() && quantityText.toLongOrNull() ?: 0L > 0 && priceText.toDoubleOrNull() ?: 0.0 > 0,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Record Drop")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkBossSoldSheet(
    drop: BossDropEntity,
    onDismiss: () -> Unit,
    onConfirm: (soldPrice: Double) -> Unit,
) {
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
                "Boss: ${drop.bossName}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "Item: ${drop.dropDisplayName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "Dropped: ${drop.quantity}x @ ${drop.unitPrice.toSkyCoins()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "Total Value: ${drop.dropValue.toSkyCoins()}",
                style = MaterialTheme.typography.bodySmall,
                color = HypixelGold,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = soldPriceText,
                onValueChange = { soldPriceText = it },
                label = { Text("Sold Price per unit") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val price = soldPriceText.toDoubleOrNull() ?: 0.0
                    if (price > 0) {
                        onConfirm(price)
                    }
                },
                enabled = soldPriceText.toDoubleOrNull() ?: 0.0 > 0,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Confirm Sale")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
