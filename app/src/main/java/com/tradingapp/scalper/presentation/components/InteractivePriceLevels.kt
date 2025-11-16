package com.tradingapp.scalper.presentation.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradingapp.scalper.presentation.theme.TradingColors
import kotlin.math.abs

@Composable
fun InteractivePriceLevels(
    entryPrice: Double?,
    targetPrice: Double?,
    stopLossPrice: Double?,
    onTargetDrag: (Double) -> Unit,
    onStopLossDrag: (Double) -> Unit
) {
    val density = LocalDensity.current
    var isDraggingTarget by remember { mutableStateOf(false) }
    var isDraggingStopLoss by remember { mutableStateOf(false) }
    var dragStartY by remember { mutableStateOf(0f) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(targetPrice, stopLossPrice) {
                detectDragGestures(
                    onDragStart = { offset ->
                        dragStartY = offset.y

                        // Determine which line is being dragged
                        targetPrice?.let { target ->
                            val targetY = priceToY(target, size.height.toFloat())
                            if (abs(offset.y - targetY) < 30) {
                                isDraggingTarget = true
                                return@detectDragGestures
                            }
                        }

                        stopLossPrice?.let { stopLoss ->
                            val stopLossY = priceToY(stopLoss, size.height.toFloat())
                            if (abs(offset.y - stopLossY) < 30) {
                                isDraggingStopLoss = true
                                return@detectDragGestures
                            }
                        }
                    },
                    onDragEnd = {
                        isDraggingTarget = false
                        isDraggingStopLoss = false
                    }
                ) { change, _ ->
                    val newPrice = yToPrice(change.position.y, size.height.toFloat())

                    when {
                        isDraggingTarget -> {
                            // Ensure target is above entry price
                            entryPrice?.let { entry ->
                                if (newPrice > entry) {
                                    onTargetDrag(newPrice)
                                }
                            } ?: onTargetDrag(newPrice)
                        }
                        isDraggingStopLoss -> {
                            // Ensure stop loss is below entry price
                            entryPrice?.let { entry ->
                                if (newPrice < entry) {
                                    onStopLossDrag(newPrice)
                                }
                            } ?: onStopLossDrag(newPrice)
                        }
                    }
                }
            }
    ) {
        // Draw P&L zones first (behind lines)
        if (entryPrice != null) {
            targetPrice?.let {
                drawPnLZone(
                    fromPrice = entryPrice,
                    toPrice = it,
                    color = TradingColors.BuyGreen.copy(alpha = 0.05f)
                )
            }

            stopLossPrice?.let {
                drawPnLZone(
                    fromPrice = entryPrice,
                    toPrice = it,
                    color = TradingColors.SellRed.copy(alpha = 0.05f)
                )
            }
        }

        // Draw entry price line
        entryPrice?.let { price ->
            drawPriceLine(
                price = price,
                label = "Entry",
                color = Color(0xFF2196F3),
                isDashed = false,
                showHandle = false,
                isDragging = false
            )
        }

        // Draw target price line
        targetPrice?.let { price ->
            val pnl = entryPrice?.let { (price - it) * 100 } ?: 0.0 // Simplified P&L
            drawPriceLine(
                price = price,
                label = "Target (+₹${String.format("%.0f", pnl)})",
                color = TradingColors.BuyGreen,
                isDashed = true,
                showHandle = true,
                isDragging = isDraggingTarget
            )
        }

        // Draw stop loss line
        stopLossPrice?.let { price ->
            val pnl = entryPrice?.let { (price - it) * 100 } ?: 0.0 // Simplified P&L
            drawPriceLine(
                price = price,
                label = "SL (₹${String.format("%.0f", pnl)})",
                color = TradingColors.SellRed,
                isDashed = true,
                showHandle = true,
                isDragging = isDraggingStopLoss
            )
        }

        // Draw risk/reward ratio
        if (entryPrice != null && targetPrice != null && stopLossPrice != null) {
            drawRiskRewardRatio(
                entry = entryPrice,
                target = targetPrice,
                stopLoss = stopLossPrice
            )
        }
    }
}

private fun DrawScope.drawPriceLine(
    price: Double,
    label: String,
    color: Color,
    isDashed: Boolean,
    showHandle: Boolean,
    isDragging: Boolean
) {
    val y = priceToY(price, size.height)
    val strokeWidth = if (isDragging) 3.dp.toPx() else 2.dp.toPx()
    val pathEffect = if (isDashed) {
        PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 5.dp.toPx()))
    } else null

    // Draw the line
    drawLine(
        color = color.copy(alpha = if (isDragging) 1f else 0.8f),
        start = Offset(0f, y),
        end = Offset(size.width, y),
        strokeWidth = strokeWidth,
        pathEffect = pathEffect,
        cap = StrokeCap.Round
    )

    // Draw label background
    val textPaint = Paint().apply {
        textSize = 12.sp.toPx()
        this.color = android.graphics.Color.WHITE
        isAntiAlias = true
    }

    val text = "$label: ${String.format("%.2f", price)}"
    val textWidth = textPaint.measureText(text)
    val textHeight = textPaint.textSize
    val padding = 8.dp.toPx()

    // Label background
    drawRoundRect(
        color = color.copy(alpha = 0.9f),
        topLeft = Offset(size.width - textWidth - padding * 3, y - textHeight / 2 - padding),
        size = Size(textWidth + padding * 2, textHeight + padding * 2),
        cornerRadius = CornerRadius(4.dp.toPx())
    )

    // Draw label text
    drawIntoCanvas { canvas ->
        canvas.nativeCanvas.drawText(
            text,
            size.width - textWidth - padding * 2,
            y + textHeight / 3,
            textPaint
        )
    }

    // Draw drag handle
    if (showHandle) {
        drawCircle(
            color = color,
            radius = if (isDragging) 10.dp.toPx() else 8.dp.toPx(),
            center = Offset(40.dp.toPx(), y),
            alpha = if (isDragging) 1f else 0.8f
        )

        // Inner circle for better visibility
        drawCircle(
            color = Color.White,
            radius = if (isDragging) 4.dp.toPx() else 3.dp.toPx(),
            center = Offset(40.dp.toPx(), y)
        )
    }
}

private fun DrawScope.drawPnLZone(
    fromPrice: Double,
    toPrice: Double,
    color: Color
) {
    val fromY = priceToY(fromPrice, size.height)
    val toY = priceToY(toPrice, size.height)

    drawRect(
        color = color,
        topLeft = Offset(0f, minOf(fromY, toY)),
        size = Size(size.width, abs(fromY - toY))
    )
}

private fun DrawScope.drawRiskRewardRatio(
    entry: Double,
    target: Double,
    stopLoss: Double
) {
    val profit = abs(target - entry)
    val loss = abs(entry - stopLoss)
    val ratio = profit / loss

    val textPaint = Paint().apply {
        textSize = 14.sp.toPx()
        color = android.graphics.Color.WHITE
        isAntiAlias = true
        isFakeBoldText = true
    }

    val text = "R:R ${String.format("%.2f", ratio)}"
    val padding = 12.dp.toPx()

    // Background
    drawRoundRect(
        color = Color(0xFF1E1E1E).copy(alpha = 0.9f),
        topLeft = Offset(size.width / 2 - 40.dp.toPx(), 20.dp.toPx()),
        size = Size(80.dp.toPx(), 32.dp.toPx()),
        cornerRadius = CornerRadius(16.dp.toPx())
    )

    // Text
    drawIntoCanvas { canvas ->
        canvas.nativeCanvas.drawText(
            text,
            size.width / 2 - 25.dp.toPx(),
            36.dp.toPx(),
            textPaint
        )
    }
}

// Helper functions to convert between price and Y coordinates
private fun priceToY(price: Double, height: Float): Float {
    // This is simplified - in real app, should match chart's price scale
    val maxPrice = 25000.0
    val minPrice = 20000.0
    val priceRange = maxPrice - minPrice
    val normalizedPrice = (price - minPrice) / priceRange
    return height * (1f - normalizedPrice.toFloat())
}

private fun yToPrice(y: Float, height: Float): Double {
    val maxPrice = 25000.0
    val minPrice = 20000.0
    val priceRange = maxPrice - minPrice
    val normalizedY = 1f - (y / height)
    return minPrice + (normalizedY * priceRange)
}