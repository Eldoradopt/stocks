package com.example.stocks.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stocks.domain.model.PricePoint
import com.example.stocks.ui.theme.DarkCard
import com.example.stocks.ui.theme.DarkCardBorder
import com.example.stocks.ui.theme.EmeraldProfit
import com.example.stocks.ui.theme.CrimsonLoss
import com.example.stocks.ui.components.toSkyCoins
import kotlin.math.max
import kotlin.math.min

enum class ChartMode {
    LINE,
    CANDLESTICK
}

enum class ChartTimeRange(val label: String, val hours: Int) {
    ONE_HOUR("1H", 1),
    FOUR_HOURS("4H", 4),
    ONE_DAY("1D", 24),
    ONE_WEEK("1W", 168),
    ONE_MONTH("1M", 720),
    SIX_MONTHS("6M", 4320),
    ALL("1Y", Int.MAX_VALUE)
}

data class CandlestickData(
    val open: Double,
    val close: Double,
    val high: Double,
    val low: Double,
    val timestamp: Long
)

@Composable
fun RevolutChart(
    pricePoints: List<PricePoint>,
    isPositiveTrend: Boolean,
    modifier: Modifier = Modifier,
    selectedRange: ChartTimeRange = ChartTimeRange.ONE_WEEK,
    onRangeSelected: (ChartTimeRange) -> Unit = {},
) {
    var chartMode by remember { mutableStateOf(ChartMode.LINE) }
    var selectedPoint by remember { mutableStateOf<PricePoint?>(null) }
    var touchX by remember { mutableStateOf<Float?>(null) }

    val accentColor = if (isPositiveTrend) EmeraldProfit else CrimsonLoss
    val gradientColor = if (isPositiveTrend) Color(0x4000E676) else Color(0x40FF3B30)

    val prices = pricePoints.map { it.sellPrice }
    val minPrice = prices.minOrNull() ?: 0.0
    val maxPrice = prices.maxOrNull() ?: 1.0

    // Synthesize candlesticks from continuous points (grouping every 4 points)
    val candles = remember(pricePoints) {
        if (pricePoints.size < 4) emptyList()
        else {
            pricePoints.chunked(max(1, pricePoints.size / 24)).map { chunk ->
                val p = chunk.map { it.sellPrice }
                CandlestickData(
                    open = chunk.first().sellPrice,
                    close = chunk.last().sellPrice,
                    high = p.maxOrNull() ?: chunk.first().sellPrice,
                    low = p.minOrNull() ?: chunk.first().sellPrice,
                    timestamp = chunk.last().timestamp
                )
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Chart Canvas Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .pointerInput(pricePoints) {
                    detectDragGestures(
                        onDragEnd = {
                            touchX = null
                            selectedPoint = null
                        }
                    ) { change, _ ->
                        touchX = change.position.x
                        val index = ((touchX ?: 0f) / size.width * (pricePoints.size - 1)).toInt().coerceIn(0, pricePoints.size - 1)
                        selectedPoint = pricePoints.getOrNull(index)
                    }
                }
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp)
            ) {
                val w = size.width
                val h = size.height
                val priceRange = (maxPrice - minPrice).coerceAtLeast(0.0001)

                // Draw background horizontal grid lines
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = (h / gridLines) * i
                    drawLine(
                        color = Color(0x1AFFFFFF),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }

                if (chartMode == ChartMode.LINE && pricePoints.size >= 2) {
                    // Line Mode with Glowing Gradient fill
                    val stepX = w / (pricePoints.size - 1)
                    val linePath = Path()
                    val fillPath = Path()

                    pricePoints.forEachIndexed { i, pt ->
                        val x = i * stepX
                        val normY = ((pt.sellPrice - minPrice) / priceRange).toFloat()
                        val y = h - (normY * h)

                        if (i == 0) {
                            linePath.moveTo(x, y)
                            fillPath.moveTo(x, h)
                            fillPath.lineTo(x, y)
                        } else {
                            val prevX = (i - 1) * stepX
                            val prevPt = pricePoints[i - 1]
                            val prevNormY = ((prevPt.sellPrice - minPrice) / priceRange).toFloat()
                            val prevY = h - (prevNormY * h)

                            val cX1 = prevX + (x - prevX) / 2f
                            val cY1 = prevY
                            val cX2 = prevX + (x - prevX) / 2f
                            val cY2 = y

                            linePath.cubicTo(cX1, cY1, cX2, cY2, x, y)
                            fillPath.cubicTo(cX1, cY1, cX2, cY2, x, y)
                        }
                    }

                    fillPath.lineTo(w, h)
                    fillPath.close()

                    // Draw Gradient Fill beneath line
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(gradientColor, Color.Transparent)
                        )
                    )

                    // Draw glowing line
                    drawPath(
                        path = linePath,
                        color = accentColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                } else if (chartMode == ChartMode.CANDLESTICK && candles.isNotEmpty()) {
                    // Candlestick Mode
                    val candleWidth = (w / candles.size) * 0.65f
                    val candleStep = w / candles.size

                    candles.forEachIndexed { i, candle ->
                        val centerX = (i * candleStep) + (candleStep / 2f)

                        val highY = h - (((candle.high - minPrice) / priceRange).toFloat() * h)
                        val lowY = h - (((candle.low - minPrice) / priceRange).toFloat() * h)
                        val openY = h - (((candle.open - minPrice) / priceRange).toFloat() * h)
                        val closeY = h - (((candle.close - minPrice) / priceRange).toFloat() * h)

                        val isGreen = candle.close >= candle.open
                        val candleColor = if (isGreen) EmeraldProfit else CrimsonLoss

                        // Draw wick (high to low)
                        drawLine(
                            color = candleColor,
                            start = Offset(centerX, highY),
                            end = Offset(centerX, lowY),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Draw body (open to close)
                        val bodyTop = min(openY, closeY)
                        val bodyHeight = max(abs(openY - closeY), 3f)

                        drawRect(
                            color = candleColor,
                            topLeft = Offset(centerX - (candleWidth / 2f), bodyTop),
                            size = Size(candleWidth, bodyHeight)
                        )
                    }
                }

                // Touch indicator line
                touchX?.let { x ->
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(x, 0f),
                        end = Offset(x, h),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )
                }
            }

            // High & Low labels pinned to chart edge (Revolut style)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${maxPrice.toSkyCoins()} coins",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${minPrice.toSkyCoins()} coins",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            // Touch indicator line and tooltip
            touchX?.let { x ->
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )
                }
            }

            // Selected point tooltip
            selectedPoint?.let { point ->
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp),
                    color = Color.Black.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            point.sellPrice.toSkyCoins(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                        val time = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                            .format(java.util.Date(point.timestamp))
                        Text(
                            time,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Time Range Selector Row & Candlestick/Line Toggle Button (Exact Revolut layout)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Period Buttons: 1D, 1S, 1M, 6M, 1A
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ChartTimeRange.values().forEach { range ->
                    val isSelected = range == selectedRange
                    Surface(
                        onClick = { onRangeSelected(range) },
                        shape = CircleShape,
                        color = if (isSelected) DarkCardBorder else Color.Transparent,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = range.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // Mode Toggle Button: Line vs Candlestick
            IconButton(
                onClick = {
                    chartMode = if (chartMode == ChartMode.LINE) ChartMode.CANDLESTICK else ChartMode.LINE
                },
                modifier = Modifier
                    .size(36.dp)
                    .border(1.dp, DarkCardBorder, CircleShape)
            ) {
                Icon(
                    imageVector = if (chartMode == ChartMode.LINE) Icons.Default.BarChart else Icons.AutoMirrored.Filled.ShowChart,
                    contentDescription = "Toggle Chart Style",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun abs(value: Float): Float = if (value < 0) -value else value
