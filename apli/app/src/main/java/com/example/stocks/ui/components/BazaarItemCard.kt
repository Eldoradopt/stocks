package com.example.stocks.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stocks.domain.model.BazaarItem
import com.example.stocks.ui.theme.EmeraldProfit
import com.example.stocks.ui.theme.HypixelGold
import com.example.stocks.ui.theme.CrimsonLoss
import com.example.stocks.ui.theme.HypixelBlue

/** Formats a coin amount using SkyBlock conventions (K / M / B suffix). */
fun Double.toSkyCoins(): String = when {
    this >= 1_000_000_000 -> "%.2fB".format(this / 1_000_000_000)
    this >= 1_000_000     -> "%.2fM".format(this / 1_000_000)
    this >= 1_000         -> "%.1fK".format(this / 1_000)
    else                  -> "%.1f".format(this)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BazaarItemCard(
    item: BazaarItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFollowed: Boolean = false,
    onToggleFollow: (() -> Unit)? = null,
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = CardDefaults.shape
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Item Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Buy: ${(item.buyPrice ?: 0.0).toSkyCoins()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = HypixelBlue,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Spread: ${"%.1f".format(item.spreadPct)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (item.spreadPct > 5) CrimsonLoss else EmeraldProfit,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            // Price & Follow Button
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${item.sellPrice.toSkyCoins()} coins",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = HypixelGold,
                )
                Text(
                    text = "Vol: ${item.sellMovingWeek.toDouble().toSkyCoins()}/wk",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (onToggleFollow != null) {
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = onToggleFollow,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isFollowed) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Follow",
                        tint = if (isFollowed) HypixelGold else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun ChangeBadge(pct: Double, modifier: Modifier = Modifier) {
    val positive = pct >= 0
    val color    = if (positive) EmeraldProfit else CrimsonLoss
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            color.copy(alpha = 0.5f)
        )
    ) {
        Text(
            text = "${if (positive) "▲" else "▼"} ${"%.2f".format(pct)}%",
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
