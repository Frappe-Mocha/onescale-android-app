package com.tradingapp.scalper.domain.usecase

import com.tradingapp.scalper.domain.repository.ITradingRepository
import javax.inject.Inject

class GetAccountInfoUseCase @Inject constructor(
    private val repository: ITradingRepository
) {
    suspend operator fun invoke() = repository.getAccountInfo()
}
