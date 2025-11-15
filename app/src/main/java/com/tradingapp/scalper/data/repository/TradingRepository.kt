package com.tradingapp.scalper.data.repository

import com.tradingapp.scalper.data.api.DeltaApiService
import com.tradingapp.scalper.data.api.models.PlaceOrderRequest
import com.tradingapp.scalper.data.api.models.ModifyOrderRequest
import com.tradingapp.scalper.data.api.models.CloseAllPositionsRequest
import com.tradingapp.scalper.data.websocket.DeltaWebSocketService
import com.tradingapp.scalper.domain.model.*
import com.tradingapp.scalper.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TradingRepository @Inject constructor(
    private val webSocketService: DeltaWebSocketService,
    private val apiService: DeltaApiService,
    private val authToken: String? = null // TODO: Implement proper auth management
) : ITradingRepository {

    private val candleCache = mutableListOf<Candle>()

    override fun getCandles(symbol: String, timeframe: String): Flow<List<Candle>> {
        return flow {
            try {
                // First, fetch historical candles from API
                val endTime = System.currentTimeMillis() / 1000
                val startTime = endTime - (24 * 60 * 60) // Last 24 hours

                val resolution = when (timeframe) {
                    "1m" -> "1"
                    "5m" -> "5"
                    "15m" -> "15"
                    "30m" -> "30"
                    "1h" -> "60"
                    "4h" -> "240"
                    "1d" -> "D"
                    else -> "1"
                }

                val response = apiService.getCandles(symbol, resolution, startTime, endTime)

                if (response.isSuccessful && response.body() != null) {
                    val candles = response.body()!!.result.map { apiCandle ->
                        Candle(
                            time = apiCandle.time,
                            open = apiCandle.open.toDouble(),
                            high = apiCandle.high.toDouble(),
                            low = apiCandle.low.toDouble(),
                            close = apiCandle.close.toDouble(),
                            volume = apiCandle.volume.toDouble()
                        )
                    }
                    candleCache.clear()
                    candleCache.addAll(candles)
                    emit(candles)
                }

                // Then subscribe to live updates via WebSocket
                webSocketService.subscribeToCandles(symbol, resolution)

                webSocketService.candleFlow.collect { candleData ->
                    candleData?.let {
                        val newCandle = Candle(
                            time = it.candle_start_time / 1000,
                            open = it.open,
                            high = it.high,
                            low = it.low,
                            close = it.close,
                            volume = it.volume ?: 0.0
                        )

                        // Update or add the new candle
                        val existingIndex = candleCache.indexOfFirst { c -> c.time == newCandle.time }
                        if (existingIndex >= 0) {
                            candleCache[existingIndex] = newCandle
                        } else {
                            candleCache.add(newCandle)
                        }

                        emit(candleCache.toList())
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error fetching candles")
                emit(emptyList())
            }
        }
    }

    override fun subscribeToTicker(symbol: String): Flow<TickerData> {
        webSocketService.subscribeToTicker(symbol)

        return webSocketService.tickerFlow.map { ticker ->
            TickerData(
                symbol = ticker?.symbol ?: symbol,
                lastPrice = ticker?.mark_price?.toDoubleOrNull() ?: 0.0,
                bidPrice = ticker?.quotes?.best_bid?.toDoubleOrNull() ?: 0.0,
                askPrice = ticker?.quotes?.best_ask?.toDoubleOrNull() ?: 0.0,
                volume = ticker?.volume?.toDouble() ?: 0.0,
                high24h = ticker?.high ?: 0.0,
                low24h = ticker?.low ?: 0.0,
                change24h = ticker?.mark_change_24h?.toDoubleOrNull() ?: 0.0
            )
        }
    }

    override fun getOrderBook(symbol: String): Flow<OrderBookData> {
        return flow {
            try {
                // Fetch order book from API first
                val response = apiService.getOrderBook(symbol, depth = 20)

                if (response.isSuccessful && response.body() != null) {
                    val orderBookResult = response.body()!!.result
                    val buyLevels = orderBookResult.buy.map { level ->
                        OrderBookLevel(
                            price = level.price.toDouble(),
                            quantity = level.size.toDouble(),
                            total = level.depth.toDouble()
                        )
                    }
                    val sellLevels = orderBookResult.sell.map { level ->
                        OrderBookLevel(
                            price = level.price.toDouble(),
                            quantity = level.size.toDouble(),
                            total = level.depth.toDouble()
                        )
                    }
                    emit(OrderBookData(symbol, buyLevels, sellLevels))
                }

                // Subscribe to live order book updates
                webSocketService.subscribeToOrderBook(symbol)

                webSocketService.orderBookFlow.collect { orderBookWS ->
                    orderBookWS?.let {
                        val buyLevels = it.buy.map { level ->
                            OrderBookLevel(
                                price = level.price.toDouble(),
                                quantity = level.size.toDouble(),
                                total = 0.0
                            )
                        }
                        val sellLevels = it.sell.map { level ->
                            OrderBookLevel(
                                price = level.price.toDouble(),
                                quantity = level.size.toDouble(),
                                total = 0.0
                            )
                        }
                        emit(OrderBookData(symbol, buyLevels, sellLevels))
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error fetching order book")
                emit(OrderBookData(symbol, emptyList(), emptyList()))
            }
        }
    }

    override suspend fun placeOrder(order: Order): Result<Order> {
        return try {
            val token = authToken ?: return Result.failure(Exception("Not authenticated"))

            // Get product ID from symbol (you may need to fetch this from products API)
            val productId = getProductId(order.symbol)

            val request = PlaceOrderRequest(
                productId = productId,
                productSymbol = order.symbol,
                limitPrice = if (order.type == OrderType.LIMIT) order.price.toString() else null,
                size = order.quantity.toLong(),
                side = if (order.side == OrderSide.BUY) "buy" else "sell",
                orderType = when (order.type) {
                    OrderType.MARKET -> "market_order"
                    OrderType.LIMIT -> "limit_order"
                    OrderType.STOP_LOSS -> "stop_market_order"
                    OrderType.STOP_LIMIT -> "stop_limit_order"
                }
            )

            val response = apiService.placeOrder("Bearer $token", request)

            if (response.isSuccessful && response.body() != null) {
                val orderApi = response.body()!!.result
                val placedOrder = Order(
                    id = orderApi.id,
                    symbol = orderApi.productSymbol,
                    side = if (orderApi.side == "buy") OrderSide.BUY else OrderSide.SELL,
                    type = order.type,
                    price = orderApi.limitPrice?.toDouble() ?: 0.0,
                    quantity = orderApi.size.toDouble(),
                    filledQuantity = (orderApi.size - orderApi.unfilledSize).toDouble(),
                    status = mapOrderStatus(orderApi.state),
                    timestamp = System.currentTimeMillis()
                )
                Result.success(placedOrder)
            } else {
                Result.failure(Exception("Failed to place order: ${response.message()}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error placing order")
            Result.failure(e)
        }
    }

    override suspend fun cancelOrder(orderId: String): Result<Boolean> {
        return try {
            val token = authToken ?: return Result.failure(Exception("Not authenticated"))

            // Note: You need to provide product_id, which should be stored with the order
            val response = apiService.cancelOrder("Bearer $token", 0, orderId)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.result.success)
            } else {
                Result.failure(Exception("Failed to cancel order: ${response.message()}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error cancelling order")
            Result.failure(e)
        }
    }

    override suspend fun modifyOrder(
        orderId: String,
        modifications: OrderModification
    ): Result<Order> {
        return try {
            val token = authToken ?: return Result.failure(Exception("Not authenticated"))

            val request = ModifyOrderRequest(
                id = orderId,
                productId = 0, // Need to get this from somewhere
                limitPrice = modifications.price?.toString(),
                size = modifications.quantity?.toLong()
            )

            val response = apiService.modifyOrder("Bearer $token", request)

            if (response.isSuccessful && response.body() != null) {
                val orderApi = response.body()!!.result
                val modifiedOrder = Order(
                    id = orderApi.id,
                    symbol = orderApi.productSymbol,
                    side = if (orderApi.side == "buy") OrderSide.BUY else OrderSide.SELL,
                    type = OrderType.LIMIT,
                    price = orderApi.limitPrice?.toDouble() ?: 0.0,
                    quantity = orderApi.size.toDouble(),
                    filledQuantity = (orderApi.size - orderApi.unfilledSize).toDouble(),
                    status = mapOrderStatus(orderApi.state),
                    timestamp = System.currentTimeMillis()
                )
                Result.success(modifiedOrder)
            } else {
                Result.failure(Exception("Failed to modify order: ${response.message()}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error modifying order")
            Result.failure(e)
        }
    }

    override fun getPositions(): Flow<List<Position>> {
        return flow {
            try {
                val token = authToken ?: throw Exception("Not authenticated")

                val response = apiService.getPositions("Bearer $token")

                if (response.isSuccessful && response.body() != null) {
                    val positions = response.body()!!.result.map { positionApi ->
                        Position(
                            symbol = positionApi.productSymbol,
                            quantity = positionApi.size.toDouble(),
                            averagePrice = positionApi.entryPrice?.toDouble() ?: 0.0,
                            currentPrice = 0.0, // Need to get from ticker
                            unrealizedPnL = positionApi.unrealizedPnl.toDouble(),
                            realizedPnL = positionApi.realizedPnl.toDouble(),
                            side = if (positionApi.size > 0) OrderSide.BUY else OrderSide.SELL,
                            timestamp = System.currentTimeMillis()
                        )
                    }
                    emit(positions)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error fetching positions")
                emit(emptyList())
            }
        }
    }

    override fun getPosition(symbol: String): Flow<Position?> {
        return flow {
            getPositions().collect { positions ->
                emit(positions.firstOrNull { it.symbol == symbol })
            }
        }
    }

    override suspend fun getAccountInfo(): Result<Account> {
        return try {
            val token = authToken ?: return Result.failure(Exception("Not authenticated"))

            val response = apiService.getBalances("Bearer $token")

            if (response.isSuccessful && response.body() != null) {
                val balances = response.body()!!.result
                val mainBalance = balances.firstOrNull { it.assetSymbol == "USDT" }

                mainBalance?.let { balance ->
                    val account = Account(
                        id = "user",
                        balance = balance.balance.toDouble(),
                        availableMargin = balance.availableBalance.toDouble(),
                        usedMargin = balance.orderMargin.toDouble() + balance.positionMargin.toDouble(),
                        unrealizedPnL = 0.0, // Calculate from positions
                        realizedPnL = 0.0
                    )
                    Result.success(account)
                } ?: Result.success(
                    Account(
                        id = "demo",
                        balance = 100000.0,
                        availableMargin = 100000.0,
                        usedMargin = 0.0,
                        unrealizedPnL = 0.0,
                        realizedPnL = 0.0
                    )
                )
            } else {
                Result.failure(Exception("Failed to fetch account info"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching account info")
            // Return demo account on error
            Result.success(
                Account(
                    id = "demo",
                    balance = 100000.0,
                    availableMargin = 100000.0,
                    usedMargin = 0.0,
                    unrealizedPnL = 0.0,
                    realizedPnL = 0.0
                )
            )
        }
    }

    override suspend fun getOptionChain(
        symbol: String,
        expiry: String
    ): Result<List<OptionContract>> {
        return try {
            val response = apiService.getOptionChain()

            if (response.isSuccessful && response.body() != null) {
                val options = response.body()!!.result
                    .filter { it.underlyingAsset.symbol == symbol }
                    .map { product ->
                        OptionContract(
                            symbol = product.symbol,
                            strikePrice = product.strikePrice?.toDouble() ?: 0.0,
                            type = if (product.contractType.contains("call", ignoreCase = true)) "CE" else "PE",
                            expiry = product.expiry ?: "",
                            lastPrice = 0.0 // Get from ticker
                        )
                    }
                Result.success(options)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching option chain")
            Result.success(emptyList())
        }
    }

    override suspend fun getOption(
        symbol: String,
        strike: Double,
        type: String
    ): Result<OptionContract> {
        return try {
            Result.success(
                OptionContract(
                    symbol = "${symbol}_${strike}${type}",
                    strikePrice = strike,
                    type = type,
                    expiry = "Weekly",
                    lastPrice = 100.0
                )
            )
        } catch (e: Exception) {
            Timber.e(e, "Error fetching option")
            Result.failure(e)
        }
    }

    private fun mapOrderStatus(state: String): OrderStatus {
        return when (state.lowercase()) {
            "open" -> OrderStatus.OPEN
            "pending" -> OrderStatus.PENDING
            "closed" -> OrderStatus.FILLED
            "cancelled" -> OrderStatus.CANCELLED
            else -> OrderStatus.PENDING
        }
    }

    private suspend fun getProductId(symbol: String): Long {
        return try {
            val response = apiService.getProduct(symbol)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.result.id
            } else {
                0L
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching product ID")
            0L
        }
    }
}
