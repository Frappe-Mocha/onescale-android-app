package com.tradingapp.scalper.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.tradingapp.scalper.presentation.theme.TradingColors

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OptionSelectorPopup(
    price: Double,
    onCallSelected: () -> Unit,
    onPutSelected: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E1E)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Text(
                        text = "Select Option Type",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Strike price display with animation
                    AnimatedContent(
                        targetState = price,
                        transitionSpec = {
                            slideInVertically() + fadeIn() with slideOutVertically() + fadeOut()
                        }
                    ) { targetPrice ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Strike Price",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "₹${String.format("%.0f", targetPrice)}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2196F3)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // CE and PE buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // CE (Call) Option
                        OptionTypeCard(
                            modifier = Modifier.weight(1f),
                            type = "CE",
                            label = "CALL",
                            color = TradingColors.BuyGreen,
                            description = "Bullish",
                            icon = "↑",
                            onClick = onCallSelected
                        )

                        // PE (Put) Option
                        OptionTypeCard(
                            modifier = Modifier.weight(1f),
                            type = "PE",
                            label = "PUT",
                            color = TradingColors.SellRed,
                            description = "Bearish",
                            icon = "↓",
                            onClick = onPutSelected
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Cancel button
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionTypeCard(
    modifier: Modifier = Modifier,
    type: String,
    label: String,
    color: Color,
    description: String,
    icon: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp)),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon
            Text(
                text = icon,
                fontSize = 32.sp,
                color = color
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Type badge
            Surface(
                modifier = Modifier
                    .padding(4.dp),
                shape = RoundedCornerShape(8.dp),
                color = color.copy(alpha = 0.2f)
            ) {
                Text(
                    text = type,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}