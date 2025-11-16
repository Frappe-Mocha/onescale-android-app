package com.tradingapp.scalper.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.window.Dialog
import com.tradingapp.scalper.presentation.chart.ConnectionStatus
import com.tradingapp.scalper.presentation.theme.TradingColors

@Composable
fun MarketSelectorTopBar(
    currentSymbol: String,
    currentTimeframe: String,
    onSymbolChange: (String) -> Unit,
    onTimeframeChange: (String) -> Unit,
    connectionStatus: ConnectionStatus
) {
    var showSymbolSelector by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A1A1A),
        shadowElevation = 4.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Symbol selector with market info
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showSymbolSelector = true }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentSymbol,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Change Symbol",
                                tint = Color.Gray,
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(24.dp)
                            )
                        }
                        Text(
                            text = "Perpetual",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Connection status
                ConnectionStatusBadge(status = connectionStatus)

                Spacer(modifier = Modifier.width(8.dp))

                // Timeframe selector
                TimeframeSelector(
                    currentTimeframe = currentTimeframe,
                    onTimeframeChange = onTimeframeChange
                )
            }
        }
    }

    if (showSymbolSelector) {
        SymbolSelectorDialog(
            onSymbolSelected = {
                onSymbolChange(it)
                showSymbolSelector = false
            },
            onDismiss = { showSymbolSelector = false }
        )
    }
}

@Composable
fun TimeframeSelector(
    currentTimeframe: String,
    onTimeframeChange: (String) -> Unit
) {
    val timeframes = listOf("1m", "5m", "15m", "30m", "1h", "4h", "1d")

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        timeframes.forEach { timeframe ->
            TimeframeChip(
                timeframe = timeframe,
                isSelected = currentTimeframe == timeframe,
                onClick = { onTimeframeChange(timeframe) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeframeChip(
    timeframe: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = timeframe.uppercase(),
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        },
        modifier = Modifier.height(28.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFF2196F3),
            selectedLabelColor = Color.White,
            containerColor = Color(0xFF2A2A2A),
            labelColor = Color.Gray
        )
    )
}

@Composable
fun ConnectionStatusBadge(status: ConnectionStatus) {
    val (color, text, icon) = when (status) {
        ConnectionStatus.CONNECTED -> Triple(
            TradingColors.BuyGreen,
            "Live",
            Icons.Default.CheckCircle
        )
        ConnectionStatus.CONNECTING -> Triple(
            Color.Yellow,
            "Connecting",
            Icons.Default.Sync
        )
        ConnectionStatus.DISCONNECTED -> Triple(
            Color.Gray,
            "Offline",
            Icons.Default.WifiOff
        )
        ConnectionStatus.ERROR -> Triple(
            TradingColors.SellRed,
            "Error",
            Icons.Default.Error
        )
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SymbolSelectorDialog(
    onSymbolSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val cryptoSymbols = listOf(
        SymbolInfo("BTCUSD", "Bitcoin", 63449.5, 2.45, true),
        SymbolInfo("ETHUSD", "Ethereum", 3171.4, -1.23, false),
        SymbolInfo("SOLUSD", "Solana", 142.36, 5.67, true),
        SymbolInfo("BNBUSD", "Binance Coin", 589.42, 0.89, true),
        SymbolInfo("XRPUSD", "Ripple", 0.5234, -0.45, false),
        SymbolInfo("ADAUSD", "Cardano", 0.4567, 3.21, true),
        SymbolInfo("AVAXUSD", "Avalanche", 35.67, 1.98, true),
        SymbolInfo("DOTUSD", "Polkadot", 7.23, -2.11, false),
        SymbolInfo("MATICUSD", "Polygon", 0.8912, 4.32, true),
        SymbolInfo("LINKUSD", "Chainlink", 14.56, -0.78, false)
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E1E)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Symbol",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search bar
                var searchQuery by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search symbols...", color = Color.Gray) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.Gray
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2196F3),
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Symbol list
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cryptoSymbols
                        .filter {
                            searchQuery.isEmpty() ||
                                    it.symbol.contains(searchQuery, ignoreCase = true) ||
                                    it.name.contains(searchQuery, ignoreCase = true)
                        }
                        .forEach { symbol ->
                            SymbolItem(
                                symbol = symbol,
                                onClick = { onSymbolSelected(symbol.symbol) }
                            )
                        }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymbolItem(
    symbol: SymbolInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = symbol.symbol,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = symbol.name,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$${String.format("%.2f", symbol.price)}",
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (symbol.isPositive)
                            Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (symbol.isPositive)
                            TradingColors.BuyGreen else TradingColors.SellRed,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${if (symbol.isPositive) "+" else ""}${symbol.change24h}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (symbol.isPositive)
                            TradingColors.BuyGreen else TradingColors.SellRed
                    )
                }
            }
        }
    }
}

data class SymbolInfo(
    val symbol: String,
    val name: String,
    val price: Double,
    val change24h: Double,
    val isPositive: Boolean
)