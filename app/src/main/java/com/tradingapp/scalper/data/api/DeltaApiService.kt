package com.tradingapp.scalper.data.api

import com.tradingapp.scalper.data.api.models.*
import retrofit2.Response
import retrofit2.http.*

interface DeltaApiService {

    // Market Data Endpoints
    @GET("v2/products")
    suspend fun getProducts(): Response<ProductsResponse>

    @GET("v2/products/{symbol}")
    suspend fun getProduct(@Path("symbol") symbol: String): Response<ProductResponse>

    @GET("v2/history/candles")
    suspend fun getCandles(
        @Query("symbol") symbol: String,
        @Query("resolution") resolution: String,
        @Query("start") start: Long,
        @Query("end") end: Long
    ): Response<CandlesResponse>

    @GET("v2/l2orderbook/{symbol}")
    suspend fun getOrderBook(
        @Path("symbol") symbol: String,
        @Query("depth") depth: Int = 20
    ): Response<OrderBookResponse>

    @GET("v2/tickers/{symbol}")
    suspend fun getTicker(@Path("symbol") symbol: String): Response<TickerResponse>

    // Order Management Endpoints
    @POST("v2/orders")
    suspend fun placeOrder(
        @Header("Authorization") authorization: String,
        @Body request: PlaceOrderRequest
    ): Response<OrderResponse>

    @DELETE("v2/orders")
    suspend fun cancelOrder(
        @Header("Authorization") authorization: String,
        @Query("product_id") productId: Long,
        @Query("order_id") orderId: String
    ): Response<CancelOrderResponse>

    @PUT("v2/orders")
    suspend fun modifyOrder(
        @Header("Authorization") authorization: String,
        @Body request: ModifyOrderRequest
    ): Response<OrderResponse>

    @GET("v2/orders")
    suspend fun getOrders(
        @Header("Authorization") authorization: String,
        @Query("product_id") productId: Long? = null,
        @Query("state") state: String? = null
    ): Response<OrdersResponse>

    @GET("v2/orders/{order_id}")
    suspend fun getOrder(
        @Header("Authorization") authorization: String,
        @Path("order_id") orderId: String
    ): Response<OrderResponse>

    // Position Endpoints
    @GET("v2/positions")
    suspend fun getPositions(
        @Header("Authorization") authorization: String,
        @Query("product_id") productId: Long? = null
    ): Response<PositionsResponse>

    @POST("v2/positions/change_margin")
    suspend fun changeMargin(
        @Header("Authorization") authorization: String,
        @Body request: ChangeMarginRequest
    ): Response<PositionResponse>

    @POST("v2/positions/close_all")
    suspend fun closeAllPositions(
        @Header("Authorization") authorization: String,
        @Body request: CloseAllPositionsRequest
    ): Response<PositionsResponse>

    // Account Endpoints
    @GET("v2/wallet/balances")
    suspend fun getBalances(
        @Header("Authorization") authorization: String
    ): Response<BalancesResponse>

    @GET("v2/profile")
    suspend fun getProfile(
        @Header("Authorization") authorization: String
    ): Response<ProfileResponse>

    // Options Chain
    @GET("v2/products")
    suspend fun getOptionChain(
        @Query("contract_types") contractTypes: String = "put_options,call_options",
        @Query("states") states: String = "live"
    ): Response<ProductsResponse>
}
