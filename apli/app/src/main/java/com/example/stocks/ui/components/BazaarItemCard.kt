package com.example.stocks.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stocks.domain.model.BazaarItem
import java.text.NumberFormat
import java.util.Locale

/** Formats a coin amount using SkyBlock conventions (K / M / B suffix). */
fun Double.toSkyCoins(): String = when {
    this >= 1_000_000_000 -> "%.1fB".format(this / 1_000_000_000)
    this >= 1_000_000     -> "%.1fM".format(this / 1_000_000)
    this >= 1_000         -> "%.1fK".format(this / 1_000)
    else                  -> "%.1f".format(this)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BazaarItemCard(
    item: BazaarItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "Vol/wk: ${item.sellMovingWeek.toDouble().toSkyCoins()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = item.sellPrice.toSkyCoins(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Spread: ${"%.1f".format(item.spreadPct)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun ChangeBadge(pct: Double, modifier: Modifier = Modifier) {
    val positive = pct >= 0
    val color    = if (positive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
    ) {
        Text(
            text = "${if (positive) "▲" else "▼"} ${"%.2f".format(pct)}%",
            color = color,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
