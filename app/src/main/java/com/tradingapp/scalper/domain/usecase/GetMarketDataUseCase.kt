package com.tradingapp.scalper.domain.usecase

import com.tradingapp.scalper.domain.repository.ITradingRepository
import javax.inject.Inject

class GetMarketDataUseCase @Inject constructor(
    private val repository: ITradingRepository
) {
    fun getCandles(symbol: String, timeframe: String) =
        repository.getCandles(symbol, timeframe)

    fun subscribeTicker(symbol: String) =
        repository.subscribeToTicker(symbol)

    fun getOrderBook(symbol: String) =
        repository.getOrderBook(symbol)
}
