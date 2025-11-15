package com.tradingapp.scalper.domain.model

data class Account(
    val id: String,
    val balance: Double,
    val availableMargin: Double,
    val usedMargin: Double,
    val unrealizedPnL: Double,
    val realizedPnL: Double
)
