package com.tradingapp.scalper.domain.model

data class Position(
    val symbol: String,
    val quantity: Double,
    val averagePrice: Double,
    val currentPrice: Double,
    val unrealizedPnL: Double,
    val realizedPnL: Double = 0.0,
    val side: OrderSide,
    val timestamp: Long = System.currentTimeMillis()
)
