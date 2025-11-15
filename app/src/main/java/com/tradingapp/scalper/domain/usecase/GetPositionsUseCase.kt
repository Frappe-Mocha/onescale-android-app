package com.tradingapp.scalper.domain.usecase

import com.tradingapp.scalper.domain.repository.ITradingRepository
import javax.inject.Inject

class GetPositionsUseCase @Inject constructor(
    private val repository: ITradingRepository
) {
    fun getPositions() = repository.getPositions()

    fun getPosition(symbol: String) = repository.getPosition(symbol)
}
