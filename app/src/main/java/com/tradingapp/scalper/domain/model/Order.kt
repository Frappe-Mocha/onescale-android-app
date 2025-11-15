package com.tradingapp.scalper.domain.model

data class Order(
    val id: String = "",
    val symbol: String,
    val type: OrderType,
    val side: OrderSide,
    val quantity: Double,
    val price: Double? = null,
    val status: OrderStatus = OrderStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis()
)

enum class OrderType {
    MARKET,
    LIMIT,
    STOP_MARKET,
    STOP_LIMIT
}

enum class OrderSide {
    BUY,
    SELL
}

enum class OrderStatus {
    PENDING,
    OPEN,
    FILLED,
    PARTIALLY_FILLED,
    CANCELLED,
    REJECTED
}

data class OrderModification(
    val price: Double? = null,
    val quantity: Double? = null,
    val stopPrice: Double? = null
)
