package com.tradingapp.scalper.presentation.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingapp.scalper.data.websocket.ConnectionState
import com.tradingapp.scalper.data.websocket.DeltaWebSocketService
import com.tradingapp.scalper.domain.model.Candle
import com.tradingapp.scalper.domain.model.Order
import com.tradingapp.scalper.domain.model.OrderSide
import com.tradingapp.scalper.domain.model.OrderType
import com.tradingapp.scalper.domain.model.Position
import com.tradingapp.scalper.domain.model.TickerData
import com.tradingapp.scalper.domain.usecase.GetMarketDataUseCase
import com.tradingapp.scalper.domain.usecase.PlaceOrderUseCase
import com.tradingapp.scalper.domain.usecase.GetPositionsUseCase
import com.tradingapp.scalper.domain.usecase.GetAccountInfoUseCase
import com.tradingapp.scalper.presentation.chart.bridge.ChartWebViewBridge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ChartUiState(
    val candles: List<Candle> = emptyList(),
    val currentSymbol: String = "BTCUSD",
    val currentTimeframe: String = "1m",
    val tickerData: TickerData? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val isLoading: Boolean = false,
    val error: String? = null,

    // Trading state
    val selectedPrice: Double? = null,
    val entryPrice: Double? = null,
    val targetPrice: Double? = null,
    val stopLossPrice: Double? = null,
    val lots: Int = 1,
    val lotSize: Int = 100,

    // Positions
    val positions: List<Position> = emptyList(),
    val totalPnL: Double = 0.0,

    // Account
    val availableFunds: Double = 100000.0,
    val marginRequired: Double = 0.0
)

@HiltViewModel
class ChartViewModel @Inject constructor(
    private val getMarketDataUseCase: GetMarketDataUseCase,
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val getPositionsUseCase: GetPositionsUseCase,
    private val getAccountInfoUseCase: GetAccountInfoUseCase,
    private val webSocketService: DeltaWebSocketService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChartUiState())
    val uiState: StateFlow<ChartUiState> = _uiState.asStateFlow()

    private val _candleData = MutableStateFlow<List<Candle>>(emptyList())
    val candleData: StateFlow<List<Candle>> = _candleData.asStateFlow()

    private var chartBridge: ChartWebViewBridge? = null

    init {
        connectWebSocket()
        loadMarketData()
        observePositions()
        observeAccountInfo()
    }

    fun setChartBridge(bridge: ChartWebViewBridge) {
        chartBridge = bridge
    }

    fun onChartReady() {
        Timber.d("Chart is ready")
        // Initial chart setup
        loadMarketData()
    }

    private fun connectWebSocket() {
        viewModelScope.launch {
            webSocketService.connectionStatus.collect { status ->
                _uiState.update { it.copy(
                    connectionStatus = when (status) {
                        ConnectionState.CONNECTED -> ConnectionStatus.CONNECTED
                        ConnectionState.CONNECTING -> ConnectionStatus.CONNECTING
                        ConnectionState.DISCONNECTED -> ConnectionStatus.DISCONNECTED
                        ConnectionState.ERROR -> ConnectionStatus.ERROR
                    }
                )}
            }
        }

        webSocketService.connect()
    }

    private fun loadMarketData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Fetch candles
                getMarketDataUseCase.getCandles(
                    _uiState.value.currentSymbol,
                    _uiState.value.currentTimeframe
                ).collect { candles ->
                    _candleData.value = candles
                    _uiState.update { it.copy(
                        candles = candles,
                        isLoading = false,
                        error = null
                    )}
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading market data")
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message
                )}
            }
        }

        // Subscribe to ticker updates
        viewModelScope.launch {
            try {
                getMarketDataUseCase.getCandles(_uiState.value.currentSymbol, "5m")
                    .collect { ticker ->
                        _uiState.update { it.copy(candles = ticker) }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error subscribing to ticker")
            }
        }
    }

    private fun observePositions() {
        viewModelScope.launch {
            try {
                getPositionsUseCase.getPositions().collect { positions ->
                    val totalPnL = positions.sumOf { it.unrealizedPnL }
                    _uiState.update { it.copy(
                        positions = positions,
                        totalPnL = totalPnL
                    )}
                }
            } catch (e: Exception) {
                Timber.e(e, "Error observing positions")
            }
        }
    }

    private fun observeAccountInfo() {
        viewModelScope.launch {
            try {
                val accountResult = getAccountInfoUseCase.invoke()
                accountResult.onSuccess { account ->
                    _uiState.update { it.copy(
                        availableFunds = account.availableMargin
                    )}
                }
            } catch (e: Exception) {
                Timber.e(e, "Error fetching account info")
            }
        }
    }

    fun onSymbolChange(symbol: String) {
        _uiState.update { it.copy(currentSymbol = symbol) }
        loadMarketData()
    }

    fun onTimeframeChange(timeframe: String) {
        _uiState.update { it.copy(currentTimeframe = timeframe) }
        loadMarketData()
    }

    fun onPriceLevelTapped(price: Double) {
        Timber.d("Price level tapped: $price")
        _uiState.update { it.copy(selectedPrice = price) }
    }

    fun onCallOptionSelected(strike: Double) {
        calculateDefaultLevels(strike, isCall = true)
    }

    fun onPutOptionSelected(strike: Double) {
        calculateDefaultLevels(strike, isCall = false)
    }

    private fun calculateDefaultLevels(entryPrice: Double, isCall: Boolean) {
        val defaultTargetPercent = if (isCall) 0.05 else -0.05 // 5% move
        val defaultStopLossPercent = if (isCall) -0.02 else 0.02 // 2% stop loss

        val targetPrice = entryPrice * (1 + defaultTargetPercent)
        val stopLossPrice = entryPrice * (1 + defaultStopLossPercent)

        _uiState.update { it.copy(
            entryPrice = entryPrice,
            targetPrice = targetPrice,
            stopLossPrice = stopLossPrice
        )}

        // Update chart overlays
        chartBridge?.addPriceLine(entryPrice, "#2196F3", "Entry", "entry")
        chartBridge?.addPriceLine(targetPrice, "#4CAF50", "Target", "target")
        chartBridge?.addPriceLine(stopLossPrice, "#F44336", "SL", "stoploss")
    }

    fun onTargetPriceDragged(newPrice: Double) {
        _uiState.update { it.copy(targetPrice = newPrice) }
        chartBridge?.updatePriceLine("target", newPrice)
        calculateMarginRequired()
    }

    fun onStopLossPriceDragged(newPrice: Double) {
        _uiState.update { it.copy(stopLossPrice = newPrice) }
        chartBridge?.updatePriceLine("stoploss", newPrice)
        calculateMarginRequired()
    }

    fun onLotsChanged(lots: Int) {
        _uiState.update { it.copy(lots = lots) }
        calculateMarginRequired()
    }

    private fun calculateMarginRequired() {
        val currentState = _uiState.value
        val entryPrice = currentState.entryPrice ?: return
        val lots = currentState.lots
        val lotSize = currentState.lotSize

        // Simplified margin calculation (actual formula depends on exchange)
        val orderValue = entryPrice * lots * lotSize
        val marginRequired = orderValue * 0.1 // Assuming 10x leverage

        _uiState.update { it.copy(marginRequired = marginRequired) }
    }

    fun placeBuyOrder() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val entryPrice = currentState.entryPrice ?: return@launch

                val order = Order(
                    id = "",
                    symbol = currentState.currentSymbol,
                    side = OrderSide.BUY,
                    type = OrderType.LIMIT,
                    price = entryPrice,
                    quantity = (currentState.lots * currentState.lotSize).toDouble(),
                    filledQuantity = 0.0,
                    status = com.tradingapp.scalper.domain.model.OrderStatus.PENDING,
                    timestamp = System.currentTimeMillis()
                )

                val result = placeOrderUseCase.invoke(order)
                result.onSuccess {
                    Timber.d("Order placed successfully: $it")
                    // Show success message
                }.onFailure { error ->
                    Timber.e(error, "Failed to place order")
                    _uiState.update { it.copy(error = error.message) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error placing buy order")
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun placeSellOrder() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val entryPrice = currentState.entryPrice ?: return@launch

                val order = Order(
                    id = "",
                    symbol = currentState.currentSymbol,
                    side = OrderSide.SELL,
                    type = OrderType.LIMIT,
                    price = entryPrice,
                    quantity = (currentState.lots * currentState.lotSize).toDouble(),
                    filledQuantity = 0.0,
                    status = com.tradingapp.scalper.domain.model.OrderStatus.PENDING,
                    timestamp = System.currentTimeMillis()
                )

                val result = placeOrderUseCase.invoke(order)
                result.onSuccess {
                    Timber.d("Order placed successfully: $it")
                    // Show success message
                }.onFailure { error ->
                    Timber.e(error, "Failed to place order")
                    _uiState.update { it.copy(error = error.message) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error placing sell order")
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketService.disconnect()
    }
}
