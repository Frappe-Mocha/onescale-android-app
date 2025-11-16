package com.tradingapp.scalper.presentation.chart.bridge

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.gson.Gson
import com.tradingapp.scalper.domain.model.Candle
import timber.log.Timber

class ChartWebViewBridge(
    private val webView: WebView,
    private val onPriceSelected: (Double) -> Unit,
    private val onChartReady: () -> Unit
) {
    private val gson = Gson()

    init {
        setupWebView()
    }

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
    private fun setupWebView() {
        webView.addJavascriptInterface(ChartJsInterface(), "AndroidBridge")

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                initializeChart()
            }
        }

        webView.loadUrl("file:///android_asset/chart.html")
    }

    private fun initializeChart() {
        webView.evaluateJavascript(
            """
            (function() {
                if (window.chartManager) {
                    AndroidBridge.onChartReady();
                    return true;
                }
                return false;
            })();
            """.trimIndent()
        ) { result ->
            Timber.d("Chart initialization result: $result")
        }
    }

    fun updateData(candles: List<Candle>) {
        val jsonData = gson.toJson(candles)
        webView.post {
            webView.evaluateJavascript(
                "window.chartManager && window.chartManager.updateData($jsonData);",
                null
            )
        }
    }

    fun addPriceLine(price: Double, color: String, label: String, id: String) {
        webView.post {
            webView.evaluateJavascript(
                """
                window.chartManager && window.chartManager.addPriceLine(
                    $price, 
                    '$color', 
                    '$label', 
                    '$id'
                );
                """.trimIndent(),
                null
            )
        }
    }

    fun updatePriceLine(id: String, newPrice: Double) {
        webView.post {
            webView.evaluateJavascript(
                "window.chartManager && window.chartManager.updatePriceLine('$id', $newPrice);",
                null
            )
        }
    }

    fun removePriceLine(id: String) {
        webView.post {
            webView.evaluateJavascript(
                "window.chartManager && window.chartManager.removePriceLine('$id');",
                null
            )
        }
    }

    fun addIndicator(name: String) {
        webView.post {
            webView.evaluateJavascript(
                "window.chartManager && window.chartManager.addIndicator('$name');",
                null
            )
        }
    }

    fun removeIndicator(name: String) {
        webView.post {
            webView.evaluateJavascript(
                "window.chartManager && window.chartManager.removeIndicator('$name');",
                null
            )
        }
    }

    fun setTheme(isDark: Boolean) {
        webView.post {
            webView.evaluateJavascript(
                "window.chartManager && window.chartManager.setTheme('${if (isDark) "dark" else "light"}');",
                null
            )
        }
    }

    inner class ChartJsInterface {
        @JavascriptInterface
        fun onPriceSelected(price: Double) {
            webView.post {
                onPriceSelected(price)
            }
        }

        @JavascriptInterface
        fun onChartReady() {
            webView.post {
                onChartReady()
            }
        }

        @JavascriptInterface
        fun log(message: String) {
            Timber.d("ChartJS: $message")
        }

        @JavascriptInterface
        fun onIndicatorAdded(indicator: String) {
            Timber.d("Indicator added: $indicator")
        }

        @JavascriptInterface
        fun onPriceLineDragged(id: String, newPrice: Double) {
            webView.post {
                // Handle price line drag event
                when (id) {
                    "target" -> {
                        // Update target price in ViewModel
                    }
                    "stoploss" -> {
                        // Update stop loss price in ViewModel
                    }
                }
            }
        }
    }
}