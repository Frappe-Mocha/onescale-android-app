package com.tradingapp.scalper.domain.repository

import com.tradingapp.scalper.domain.model.*
import kotlinx.coroutines.flow.Flow

interface ITradingRepository {
    // Market Data
    fun getCandles(symbol: String, timeframe: String): Flow<List<Candle>>
    fun subscribeToTicker(symbol: String): Flow<TickerData>
    fun getOrderBook(symbol: String): Flow<OrderBookData>

    // Order Management
    suspend fun placeOrder(order: Order): Result<Order>
    suspend fun cancelOrder(orderId: String): Result<Boolean>
    suspend fun modifyOrder(orderId: String, modifications: OrderModification): Result<Order>

    // Positions
    fun getPositions(): Flow<List<Position>>
    fun getPosition(symbol: String): Flow<Position?>

    // Account
    suspend fun getAccountInfo(): Result<Account>

    // Options
    suspend fun getOptionChain(symbol: String, expiry: String): Result<List<OptionContract>>
    suspend fun getOption(symbol: String, strike: Double, type: String): Result<OptionContract>
}
