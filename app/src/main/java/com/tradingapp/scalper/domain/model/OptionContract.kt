package com.tradingapp.scalper.domain.model

data class OptionContract(
    val symbol: String,
    val strikePrice: Double,
    val type: String, // "CE" or "PE"
    val expiry: String,
    val lastPrice: Double,
    val bidPrice: Double = 0.0,
    val askPrice: Double = 0.0,
    val volume: Double = 0.0,
    val openInterest: Double = 0.0,
    val impliedVolatility: Double = 0.0
)
