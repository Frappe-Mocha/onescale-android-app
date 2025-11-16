package com.tradingapp.scalper.presentation.chart

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.tradingapp.scalper.presentation.chart.bridge.ChartWebViewBridge

@Composable
fun ChartView(
    viewModel: ChartViewModel,
    onPriceLevelTap: (Double) -> Unit
) {
    val context = LocalContext.current
    val candleData by viewModel.candleData.collectAsState()
    var webView by remember { mutableStateOf<WebView?>(null) }
    var chartBridge by remember { mutableStateOf<ChartWebViewBridge?>(null) }

    // Initialize chart bridge
    DisposableEffect(Unit) {
        onDispose {
            webView?.destroy()
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                configureWebView(this)

                // Create and set bridge
                val bridge = ChartWebViewBridge(
                    webView = this,
                    onPriceSelected = onPriceLevelTap,
                    onChartReady = {
                        viewModel.onChartReady()
                    }
                )

                chartBridge = bridge
                viewModel.setChartBridge(bridge)

                // Set touch listener for Y-axis price selection
                setOnTouchListener { _, event ->
                    handleTouchEvent(event, this, bridge)
                }

                webView = this
            }
        },
        update = { view ->
            // Update chart data when candles change
            if (candleData.isNotEmpty()) {
                chartBridge?.updateData(candleData)
            }

            // Update price lines
            viewModel.uiState.value.let { state ->
                state.targetPrice?.let { price ->
                    chartBridge?.addPriceLine(price, "#4CAF50", "Target", "target")
                }
                state.stopLossPrice?.let { price ->
                    chartBridge?.addPriceLine(price, "#F44336", "SL", "stoploss")
                }
                state.entryPrice?.let { price ->
                    chartBridge?.addPriceLine(price, "#2196F3", "Entry", "entry")
                }
            }
        }
    )
}

@SuppressLint("SetJavaScriptEnabled")
private fun configureWebView(webView: WebView) {
    webView.settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        allowFileAccess = true
        loadWithOverviewMode = true
        useWideViewPort = true
        builtInZoomControls = false
        displayZoomControls = false
        setSupportZoom(false)
    }
}

private fun handleTouchEvent(
    event: MotionEvent,
    webView: WebView,
    bridge: ChartWebViewBridge
): Boolean {
    when (event.action) {
        MotionEvent.ACTION_DOWN -> {
            val x = event.x
            val y = event.y

            // Check if touch is near the Y-axis (right side)
            if (x > webView.width - 120) {
                // Convert Y coordinate to price
                webView.evaluateJavascript(
                    "window.chartManager.getPriceFromY($y);",
                    null
                )
                return true
            }
        }
    }
    return false
}