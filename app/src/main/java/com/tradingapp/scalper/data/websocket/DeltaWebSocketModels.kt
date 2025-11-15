package com.tradingapp.scalper.data.websocket

import com.google.gson.annotations.SerializedName

data class CandleData(
    @SerializedName("candle_start_time")
    val candle_start_time: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double?
)

data class TickerDataWS(
    val symbol: String?,
    @SerializedName("mark_price")
    val mark_price: String?,
    val quotes: Quotes?,
    val volume: String?,
    val high: Double?,
    val low: Double?,
    @SerializedName("mark_change_24h")
    val mark_change_24h: String?
)

data class Quotes(
    @SerializedName("best_bid")
    val best_bid: String?,
    @SerializedName("best_ask")
    val best_ask: String?
)
