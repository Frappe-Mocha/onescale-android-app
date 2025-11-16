package com.tradingapp.scalper.presentation.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tradingapp.scalper.presentation.order.OrderState
import com.tradingapp.scalper.presentation.theme.TradingColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderConfirmationBottomSheet(
    orderState: OrderState,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onCancel,
        containerColor = Color(0xFF1E1E1E),
        contentColor = Color.White,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Order",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Order Confirmation",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                IconButton(onClick = onCancel) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Gray
                    )
                }
            }

            Divider(color = Color.Gray.copy(alpha = 0.3f))

            Spacer(modifier = Modifier.height(16.dp))

            // Order Details
            orderState.selectedOption?.let { option ->
                OrderDetailSection(
                    symbol = option.symbol,
                    strikePrice = option.strikePrice,
                    optionType = option.type,
                    lots = orderState.lots,
                    lotSize = orderState.lotSize,
                    price = option.lastPrice
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Margin Calculation Card
            MarginCalculationCard(
                marginRequired = orderState.marginRequired,
                availableFunds = orderState.availableFunds
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Order Type Selection
            OrderTypeSelector(
                currentType = orderState.orderType,
                onTypeChange = { /* Update order type */ }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Product Type Selection
            ProductTypeSelector(
                currentType = orderState.productType,
                onTypeChange = { /* Update product type */ }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Add to Basket Button
                OutlinedButton(
                    onClick = { /* Add to basket */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = SolidColor(Color.Gray)
                    )
                ) {
                    Icon(
                        Icons.Default.AddShoppingCart,
                        contentDescription = "Basket",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Basket")
                }

                // Confirm Button
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(2f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TradingColors.BuyGreen
                    )
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Confirm",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PLACE ORDER",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun OrderDetailSection(
    symbol: String,
    strikePrice: Double,
    optionType: String,
    lots: Int,
    lotSize: Int,
    price: Double
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2A2A2A))
            .padding(16.dp)
    ) {
        Text(
            text = symbol,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        OrderDetailRow("Strike", "₹${String.format("%.0f", strikePrice)}")
        OrderDetailRow("Type", optionType)
        OrderDetailRow("Lots", lots.toString())
        OrderDetailRow("Quantity", (lots * lotSize).toString())
        OrderDetailRow("Price", "₹${String.format("%.2f", price)}")
        OrderDetailRow("Total Value", "₹${String.format("%,.0f", price * lots * lotSize)}")
    }
}

@Composable
fun OrderDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun MarginCalculationCard(
    marginRequired: Double,
    availableFunds: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Margin Required",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "₹${String.format("%,.0f", marginRequired)}",
                    fontSize = 18.sp,
                    color = Color(0xFF2196F3),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar showing margin usage
            val marginPercentage = (marginRequired / availableFunds).coerceIn(0.0, 1.0)
            LinearProgressIndicator(
                progress = marginPercentage.toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = when {
                    marginPercentage > 0.8 -> TradingColors.SellRed
                    marginPercentage > 0.6 -> Color.Yellow
                    else -> TradingColors.BuyGreen
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Available",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "₹${String.format("%,.0f", availableFunds)}",
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun OrderTypeSelector(
    currentType: String,
    onTypeChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OrderTypeChip(
            label = "MARKET",
            selected = currentType == "MARKET",
            onClick = { onTypeChange("MARKET") }
        )
        OrderTypeChip(
            label = "LIMIT",
            selected = currentType == "LIMIT",
            onClick = { onTypeChange("LIMIT") }
        )
        OrderTypeChip(
            label = "SL",
            selected = currentType == "SL",
            onClick = { onTypeChange("SL") }
        )
        OrderTypeChip(
            label = "SL-M",
            selected = currentType == "SL-M",
            onClick = { onTypeChange("SL-M") }
        )
    }
}

@Composable
fun ProductTypeSelector(
    currentType: String,
    onTypeChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ProductTypeChip(
            label = "INTRA",
            selected = currentType == "INTRA",
            onClick = { onTypeChange("INTRA") },
            modifier = Modifier.weight(1f)
        )
        ProductTypeChip(
            label = "NRML",
            selected = currentType == "NRML",
            onClick = { onTypeChange("NRML") },
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTypeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 12.sp
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFF2196F3),
            selectedLabelColor = Color.White,
            containerColor = Color(0xFF3A3A3A),
            labelColor = Color.Gray
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductTypeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFF2196F3),
            selectedLabelColor = Color.White,
            containerColor = Color(0xFF3A3A3A),
            labelColor = Color.Gray
        )
    )
}