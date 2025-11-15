package com.tradingapp.scalper.domain.model

data class TickerData(
    val symbol: String,
    val lastPrice: Double,
    val bidPrice: Double,
    val askPrice: Double,
    val volume: Double,
    val high24h: Double,
    val low24h: Double,
    val change24h: Double
)
