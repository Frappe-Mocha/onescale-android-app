package com.tradingapp.scalper.presentation.components

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
import com.tradingapp.scalper.domain.model.Position
import com.tradingapp.scalper.presentation.theme.TradingColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(
    positions: List<Position>,
    totalPnL: Double
) {
    var selectedTab by remember { mutableStateOf(0) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A1A1A),
        shadowElevation = 8.dp
    ) {
        Column {
            // P&L Summary Bar
            PnLSummaryBar(
                positions = positions,
                totalPnL = totalPnL
            )

            Divider(
                color = Color.Gray.copy(alpha = 0.2f),
                thickness = 1.dp
            )

            // Navigation Tabs
            NavigationBar(
                containerColor = Color(0xFF1A1A1A),
                contentColor = Color.White
            ) {
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.ShowChart,
                            contentDescription = "Charts"
                        )
                    },
                    label = { Text("Charts") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF2196F3),
                        selectedTextColor = Color(0xFF2196F3),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0xFF2196F3).copy(alpha = 0.1f)
                    )
                )

                NavigationBarItem(
                    icon = {
                        BadgedBox(
                            badge = {
                                if (positions.isNotEmpty()) {
                                    Badge(
                                        containerColor = Color(0xFF2196F3)
                                    ) {
                                        Text(positions.size.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.AccountBalance,
                                contentDescription = "Positions"
                            )
                        }
                    },
                    label = { Text("Positions") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF2196F3),
                        selectedTextColor = Color(0xFF2196F3),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0xFF2196F3).copy(alpha = 0.1f)
                    )
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = "Orders"
                        )
                    },
                    label = { Text("Orders") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF2196F3),
                        selectedTextColor = Color(0xFF2196F3),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0xFF2196F3).copy(alpha = 0.1f)
                    )
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Account"
                        )
                    },
                    label = { Text("Account") },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF2196F3),
                        selectedTextColor = Color(0xFF2196F3),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0xFF2196F3).copy(alpha = 0.1f)
                    )
                )
            }
        }
    }
}

@Composable
fun PnLSummaryBar(
    positions: List<Position>,
    totalPnL: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
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

        // Quick Actions
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionButton(
                text = "EXIT ALL",
                color = TradingColors.SellRed,
                enabled = positions.isNotEmpty(),
                onClick = { /* Exit all positions */ }
            )

            QuickActionButton(
                text = "ORDERS",
                color = Color(0xFF2196F3),
                onClick = { /* Navigate to orders */ }
            )
        }
    }
}

@Composable
fun QuickActionButton(
    text: String,
    color: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled) { onClick() },
        color = if (enabled) color.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) color else Color.Gray
        )
    }
}