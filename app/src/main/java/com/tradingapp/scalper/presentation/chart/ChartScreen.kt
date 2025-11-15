package com.tradingapp.scalper.presentation.chart

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.tradingapp.scalper.presentation.components.BottomNavigationBar
import com.tradingapp.scalper.presentation.components.MarketSelectorTopBar
import com.tradingapp.scalper.presentation.components.OptionSelectorPopup
import com.tradingapp.scalper.presentation.components.OrderConfirmationBottomSheet
import com.tradingapp.scalper.presentation.order.OrderViewModel

@Composable
fun ChartScreen(
    chartViewModel: ChartViewModel = hiltViewModel(),
    orderViewModel: OrderViewModel = hiltViewModel()
) {
    val uiState by chartViewModel.uiState.collectAsState()
    val orderState by orderViewModel.orderState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showOptionSelector by remember { mutableStateOf(false) }
    var selectedPrice by remember { mutableStateOf(0.0) }

    // Show error snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            chartViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            MarketSelectorTopBar(
                currentSymbol = uiState.currentSymbol,
                currentTimeframe = uiState.currentTimeframe,
                onSymbolChange = { chartViewModel.onSymbolChange(it) },
                onTimeframeChange = { chartViewModel.onTimeframeChange(it) },
                connectionStatus = uiState.connectionStatus
            )
        },
        bottomBar = {
            BottomNavigationBar(
                positions = uiState.positions,
                totalPnL = uiState.totalPnL
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Chart View
            ChartView(
                viewModel = chartViewModel,
                onPriceLevelTap = { price ->
                    selectedPrice = price
                    showOptionSelector = true
                }
            )
        }
    }

    // Option Selector Popup
    if (showOptionSelector) {
        OptionSelectorPopup(
            price = selectedPrice,
            onCallSelected = {
                chartViewModel.onCallOptionSelected(selectedPrice)
                orderViewModel.selectCallOption(selectedPrice)
                showOptionSelector = false
            },
            onPutSelected = {
                chartViewModel.onPutOptionSelected(selectedPrice)
                orderViewModel.selectPutOption(selectedPrice)
                showOptionSelector = false
            },
            onDismiss = { showOptionSelector = false }
        )
    }

    // Order Confirmation Bottom Sheet
    if (orderState.showOrderSheet) {
        OrderConfirmationBottomSheet(
            orderState = orderState,
            onConfirm = {
                chartViewModel.placeBuyOrder()
                orderViewModel.dismissOrderSheet()
            },
            onCancel = {
                orderViewModel.dismissOrderSheet()
            }
        )
    }
}
