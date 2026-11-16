package com.example.stocks.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun MiniSparkline(
    prices: List<Double>,
    color: Color,
    modifier: Modifier = Modifier,
) {
    if (prices.size < 2) return

    Canvas(modifier = modifier) {
        val min = prices.minOrNull() ?: 0.0
        val max = prices.maxOrNull() ?: 1.0
        val range = (max - min).coerceAtLeast(0.0001)

        val width = size.width
        val height = size.height

        val stepX = width / (prices.size - 1)
        val path = Path()

        prices.forEachIndexed { index, price ->
            val x = index * stepX
            val normalizedY = ((price - min) / range).toFloat()
            // Invert Y because canvas coordinates have (0,0) at top-left
            val y = height - (normalizedY * height)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
