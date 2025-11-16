package com.tradingapp.scalper.domain.model

data class OrderBookData(
    val symbol: String,
    val bids: List<OrderBookLevel>,
    val asks: List<OrderBookLevel>
)

data class OrderBookLevel(
    val price: Double,
    val quantity: Double,
    val total: Double
)
