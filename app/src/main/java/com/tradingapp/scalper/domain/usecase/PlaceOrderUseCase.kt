package com.tradingapp.scalper.domain.usecase

import com.tradingapp.scalper.domain.model.Order
import com.tradingapp.scalper.domain.model.OrderType
import com.tradingapp.scalper.domain.repository.ITradingRepository
import javax.inject.Inject

class PlaceOrderUseCase @Inject constructor(
    private val repository: ITradingRepository
) {
    suspend operator fun invoke(order: Order): Result<Order> {
        // Add business logic validation here
        if (order.quantity <= 0) {
            return Result.failure(IllegalArgumentException("Quantity must be greater than 0"))
        }

        if (order.type == OrderType.LIMIT && order.price == null) {
            return Result.failure(IllegalArgumentException("Limit order must have a price"))
        }

        return repository.placeOrder(order)
    }
}
