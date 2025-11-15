package com.tradingapp.scalper.data.api.models

import com.google.gson.annotations.SerializedName

// Base Response
data class BaseResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("result")
    val result: T?,
    @SerializedName("error")
    val error: ApiError?
)

data class ApiError(
    @SerializedName("code")
    val code: String,
    @SerializedName("context")
    val context: Map<String, Any>?,
    @SerializedName("message")
    val message: String
)

// Product Models
data class ProductsResponse(
    @SerializedName("result")
    val result: List<Product>
)

data class ProductResponse(
    @SerializedName("result")
    val result: Product
)

data class Product(
    @SerializedName("id")
    val id: Long,
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("contract_type")
    val contractType: String,
    @SerializedName("strike_price")
    val strikePrice: String?,
    @SerializedName("expiry")
    val expiry: String?,
    @SerializedName("underlying_asset")
    val underlyingAsset: UnderlyingAsset,
    @SerializedName("settling_asset")
    val settlingAsset: SettlingAsset,
    @SerializedName("tick_size")
    val tickSize: String,
    @SerializedName("contract_value")
    val contractValue: String,
    @SerializedName("state")
    val state: String
)

data class UnderlyingAsset(
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("precision")
    val precision: Int
)

data class SettlingAsset(
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("precision")
    val precision: Int
)

// Candle Models
data class CandlesResponse(
    @SerializedName("result")
    val result: List<CandleApi>
)

data class CandleApi(
    @SerializedName("time")
    val time: Long,
    @SerializedName("open")
    val open: String,
    @SerializedName("high")
    val high: String,
    @SerializedName("low")
    val low: String,
    @SerializedName("close")
    val close: String,
    @SerializedName("volume")
    val volume: String
)

// Order Book Models
data class OrderBookResponse(
    @SerializedName("result")
    val result: OrderBookResult
)

data class OrderBookResult(
    @SerializedName("buy")
    val buy: List<OrderBookLevelApi>,
    @SerializedName("sell")
    val sell: List<OrderBookLevelApi>,
    @SerializedName("last_updated_at")
    val lastUpdatedAt: Long,
    @SerializedName("product_id")
    val productId: Long,
    @SerializedName("symbol")
    val symbol: String
)

data class OrderBookLevelApi(
    @SerializedName("depth")
    val depth: String,
    @SerializedName("price")
    val price: String,
    @SerializedName("size")
    val size: Long
)

// Ticker Models
data class TickerResponse(
    @SerializedName("result")
    val result: TickerApi
)

data class TickerApi(
    @SerializedName("close")
    val close: String?,
    @SerializedName("high")
    val high: String?,
    @SerializedName("low")
    val low: String?,
    @SerializedName("mark_price")
    val markPrice: String?,
    @SerializedName("open")
    val open: String?,
    @SerializedName("open_interest")
    val openInterest: String?,
    @SerializedName("price_change_24h")
    val priceChange24h: String?,
    @SerializedName("price_change_percent_24h")
    val priceChangePercent24h: String?,
    @SerializedName("product_id")
    val productId: Long,
    @SerializedName("size")
    val size: String?,
    @SerializedName("spot_price")
    val spotPrice: String?,
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("turnover_24h")
    val turnover24h: String?,
    @SerializedName("volume_24h")
    val volume24h: String?
)

// Order Models
data class PlaceOrderRequest(
    @SerializedName("product_id")
    val productId: Long,
    @SerializedName("product_symbol")
    val productSymbol: String,
    @SerializedName("limit_price")
    val limitPrice: String?,
    @SerializedName("size")
    val size: Long,
    @SerializedName("side")
    val side: String, // "buy" or "sell"
    @SerializedName("order_type")
    val orderType: String, // "limit_order", "market_order"
    @SerializedName("time_in_force")
    val timeInForce: String = "gtc", // "gtc", "ioc", "fok"
    @SerializedName("post_only")
    val postOnly: Boolean = false,
    @SerializedName("reduce_only")
    val reduceOnly: Boolean = false,
    @SerializedName("stop_order_type")
    val stopOrderType: String? = null,
    @SerializedName("stop_price")
    val stopPrice: String? = null,
    @SerializedName("bracket_stop_loss_limit_price")
    val bracketStopLossLimitPrice: String? = null,
    @SerializedName("bracket_stop_loss_price")
    val bracketStopLossPrice: String? = null,
    @SerializedName("bracket_take_profit_limit_price")
    val bracketTakeProfitLimitPrice: String? = null,
    @SerializedName("bracket_take_profit_price")
    val bracketTakeProfitPrice: String? = null
)

data class ModifyOrderRequest(
    @SerializedName("id")
    val id: String,
    @SerializedName("product_id")
    val productId: Long,
    @SerializedName("limit_price")
    val limitPrice: String?,
    @SerializedName("size")
    val size: Long?
)

data class OrderResponse(
    @SerializedName("result")
    val result: OrderApi
)

data class OrdersResponse(
    @SerializedName("result")
    val result: List<OrderApi>
)

data class OrderApi(
    @SerializedName("id")
    val id: String,
    @SerializedName("product_id")
    val productId: Long,
    @SerializedName("product_symbol")
    val productSymbol: String,
    @SerializedName("limit_price")
    val limitPrice: String?,
    @SerializedName("avg_fill_price")
    val avgFillPrice: String?,
    @SerializedName("size")
    val size: Long,
    @SerializedName("unfilled_size")
    val unfilledSize: Long,
    @SerializedName("side")
    val side: String,
    @SerializedName("order_type")
    val orderType: String,
    @SerializedName("state")
    val state: String, // "open", "pending", "closed", "cancelled"
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("commission")
    val commission: String?
)

data class CancelOrderResponse(
    @SerializedName("result")
    val result: CancelOrderResult
)

data class CancelOrderResult(
    @SerializedName("order_id")
    val orderId: String,
    @SerializedName("success")
    val success: Boolean
)

// Position Models
data class PositionsResponse(
    @SerializedName("result")
    val result: List<PositionApi>
)

data class PositionResponse(
    @SerializedName("result")
    val result: PositionApi
)

data class PositionApi(
    @SerializedName("product_id")
    val productId: Long,
    @SerializedName("product_symbol")
    val productSymbol: String,
    @SerializedName("size")
    val size: Long,
    @SerializedName("entry_price")
    val entryPrice: String?,
    @SerializedName("margin")
    val margin: String,
    @SerializedName("liquidation_price")
    val liquidationPrice: String?,
    @SerializedName("unrealized_pnl")
    val unrealizedPnl: String,
    @SerializedName("realized_pnl")
    val realizedPnl: String,
    @SerializedName("realized_cashflow")
    val realizedCashflow: String
)

data class ChangeMarginRequest(
    @SerializedName("product_id")
    val productId: Long,
    @SerializedName("delta_margin")
    val deltaMargin: String
)

data class CloseAllPositionsRequest(
    @SerializedName("close_all_portfolio")
    val closeAllPortfolio: Boolean = true
)

// Account Models
data class BalancesResponse(
    @SerializedName("result")
    val result: List<BalanceApi>
)

data class BalanceApi(
    @SerializedName("asset_id")
    val assetId: Long,
    @SerializedName("asset_symbol")
    val assetSymbol: String,
    @SerializedName("available_balance")
    val availableBalance: String,
    @SerializedName("balance")
    val balance: String,
    @SerializedName("commission")
    val commission: String,
    @SerializedName("order_margin")
    val orderMargin: String,
    @SerializedName("position_margin")
    val positionMargin: String
)

data class ProfileResponse(
    @SerializedName("result")
    val result: ProfileApi
)

data class ProfileApi(
    @SerializedName("id")
    val id: Long,
    @SerializedName("email")
    val email: String,
    @SerializedName("kyc_status")
    val kycStatus: String,
    @SerializedName("default_currency")
    val defaultCurrency: String
)
