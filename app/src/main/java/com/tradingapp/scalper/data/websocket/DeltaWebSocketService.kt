package com.tradingapp.scalper.data.websocket

import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeltaWebSocketService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {
    private val _candleFlow = MutableStateFlow<CandleData?>(null)
    val candleFlow: StateFlow<CandleData?> = _candleFlow

    private val _tickerFlow = MutableStateFlow<TickerDataWS?>(null)
    val tickerFlow: StateFlow<TickerDataWS?> = _tickerFlow

    private val _connectionStatus = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionState> = _connectionStatus

    private val _orderBookFlow = MutableStateFlow<OrderBookWS?>(null)
    val orderBookFlow: StateFlow<OrderBookWS?> = _orderBookFlow

    private var webSocket: WebSocket? = null
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var reconnectJob: Job? = null
    private var reconnectAttempts = 0

    private val subscriptions = mutableSetOf<String>()

    companion object {
        private const val WS_URL = "wss://socket.delta.exchange"
        private const val PING_INTERVAL = 30L
        private const val MAX_RECONNECT_ATTEMPTS = 5
        private const val RECONNECT_DELAY_MS = 2000L
    }

    fun connect() {
        if (_connectionStatus.value == ConnectionState.CONNECTED ||
            _connectionStatus.value == ConnectionState.CONNECTING) {
            Timber.d("Already connected or connecting")
            return
        }

        _connectionStatus.value = ConnectionState.CONNECTING

        val request = Request.Builder()
            .url(WS_URL)
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Timber.d("WebSocket connected")
                _connectionStatus.value = ConnectionState.CONNECTED
                reconnectAttempts = 0

                // Resubscribe to previous channels
                resubscribeAll()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleMessage(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                handleMessage(bytes.utf8())
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Timber.d("WebSocket closing: $code - $reason")
                _connectionStatus.value = ConnectionState.DISCONNECTED
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Timber.d("WebSocket closed: $code - $reason")
                _connectionStatus.value = ConnectionState.DISCONNECTED
                attemptReconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Timber.e(t, "WebSocket failure")
                _connectionStatus.value = ConnectionState.ERROR
                attemptReconnect()
            }
        })
    }

    fun disconnect() {
        reconnectJob?.cancel()
        webSocket?.close(1000, "User disconnect")
        webSocket = null
        subscriptions.clear()
        _connectionStatus.value = ConnectionState.DISCONNECTED
    }

    fun subscribeToCandles(symbol: String, resolution: String = "1") {
        val channel = "candlestick_${symbol}_${resolution}m"
        subscribe(channel)
    }

    fun subscribeToTicker(symbol: String) {
        val channel = "v2/ticker/$symbol"
        subscribe(channel)
    }

    fun subscribeToOrderBook(symbol: String) {
        val channel = "l2_orderbook/$symbol"
        subscribe(channel)
    }

    fun unsubscribe(channel: String) {
        val message = mapOf(
            "type" to "unsubscribe",
            "payload" to mapOf("channels" to listOf(channel))
        )
        sendMessage(message)
        subscriptions.remove(channel)
    }

    private fun subscribe(channel: String) {
        if (subscriptions.contains(channel)) {
            Timber.d("Already subscribed to $channel")
            return
        }

        val message = mapOf(
            "type" to "subscribe",
            "payload" to mapOf("channels" to listOf(channel))
        )

        sendMessage(message)
        subscriptions.add(channel)
    }

    private fun resubscribeAll() {
        subscriptions.forEach { channel ->
            val message = mapOf(
                "type" to "subscribe",
                "payload" to mapOf("channels" to listOf(channel))
            )
            sendMessage(message)
        }
    }

    private fun sendMessage(message: Map<String, Any>) {
        val json = gson.toJson(message)
        val sent = webSocket?.send(json) ?: false
        if (!sent) {
            Timber.w("Failed to send message: $json")
        } else {
            Timber.d("Sent: $json")
        }
    }

    private fun handleMessage(text: String) {
        try {
            Timber.d("Received: $text")

            val jsonElement = JsonParser.parseString(text)
            if (!jsonElement.isJsonObject) return

            val jsonObject = jsonElement.asJsonObject

            // Check if it's a subscription confirmation
            if (jsonObject.has("type") && jsonObject.get("type").asString == "subscriptions") {
                Timber.d("Subscription confirmed")
                return
            }

            // Handle different message types
            when {
                text.contains("candlestick") -> {
                    val candleData = gson.fromJson(text, CandleData::class.java)
                    _candleFlow.value = candleData
                }
                text.contains("ticker") -> {
                    val tickerData = gson.fromJson(text, TickerDataWS::class.java)
                    _tickerFlow.value = tickerData
                }
                text.contains("l2_orderbook") -> {
                    val orderBook = gson.fromJson(text, OrderBookWS::class.java)
                    _orderBookFlow.value = orderBook
                }
                else -> {
                    Timber.d("Unknown message type: $text")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error parsing message: $text")
        }
    }

    private fun attemptReconnect() {
        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            Timber.e("Max reconnect attempts reached")
            _connectionStatus.value = ConnectionState.ERROR
            return
        }

        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            reconnectAttempts++
            val delayMs = RECONNECT_DELAY_MS * reconnectAttempts
            Timber.d("Reconnecting in ${delayMs}ms (attempt $reconnectAttempts)")
            delay(delayMs)
            connect()
        }
    }
}

enum class ConnectionState {
    CONNECTED,
    CONNECTING,
    DISCONNECTED,
    ERROR
}

// Order book WebSocket model
data class OrderBookWS(
    val symbol: String,
    val buy: List<OrderBookLevel>,
    val sell: List<OrderBookLevel>,
    val timestamp: Long
)

data class OrderBookLevel(
    val price: String,
    val size: String
)
