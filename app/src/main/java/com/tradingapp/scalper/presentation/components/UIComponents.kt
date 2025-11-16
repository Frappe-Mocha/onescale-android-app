package com.tradingapp.scalper.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradingapp.scalper.domain.model.Position
import com.tradingapp.scalper.presentation.chart.ConnectionStatus
import com.tradingapp.scalper.presentation.theme.TradingColors

@Composable
fun PositionSummaryBar(
    positions: List<Position>,
    totalPnL: Double
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        color = Color(0xFF1A1A1A),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Positions count
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.ShowChart,
                    contentDescription = "Positions",
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "POSITIONS",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = positions.size.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Total P&L
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "TOTAL P&L",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (totalPnL >= 0)
                                Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = "P&L",
                            tint = if (totalPnL >= 0)
                                TradingColors.BuyGreen else TradingColors.SellRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "₹${String.format("%+,.0f", totalPnL)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (totalPnL >= 0)
                                TradingColors.BuyGreen else TradingColors.SellRed
                        )
                    }
                }
            }

            // Orders button
            Button(
                onClick = { /* Navigate to orders */ },
                modifier = Modifier.height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text(
                    text = "ORDERS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ConnectionStatusIndicator(
    status: ConnectionStatus,
    modifier: Modifier = Modifier
) {
    val color by animateColorAsState(
        targetValue = when (status) {
            ConnectionStatus.CONNECTED -> TradingColors.BuyGreen
            ConnectionStatus.CONNECTING -> Color.Yellow
            ConnectionStatus.DISCONNECTED -> Color.Gray
            ConnectionStatus.ERROR -> TradingColors.SellRed
        },
        animationSpec = tween(500)
    )

    val text = when (status) {
        ConnectionStatus.CONNECTED -> "Live"
        ConnectionStatus.CONNECTING -> "Connecting..."
        ConnectionStatus.DISCONNECTED -> "Offline"
        ConnectionStatus.ERROR -> "Error"
    }

    Row(
        modifier = modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Connection dot with animation
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )

        // Show reconnect button if disconnected or error
        if (status == ConnectionStatus.DISCONNECTED || status == ConnectionStatus.ERROR) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Default.Refresh,
                contentDescription = "Reconnect",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
