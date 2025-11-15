package com.tradingapp.scalper.presentation.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tradingapp.scalper.domain.model.OptionContract
import com.tradingapp.scalper.domain.model.OrderSide
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class OrderState(
    val selectedOption: OptionContract? = null,
    val lots: Int = 1,
    val lotSize: Int = 100,
    val orderType: String = "MARKET",
    val productType: String = "INTRA",
    val marginRequired: Double = 0.0,
    val availableFunds: Double = 100000.0,
    val showOrderSheet: Boolean = false
)

@HiltViewModel
class OrderViewModel @Inject constructor() : ViewModel() {

    private val _orderState = MutableStateFlow(OrderState())
    val orderState: StateFlow<OrderState> = _orderState.asStateFlow()

    fun showOptionSelector(price: Double) {
        // Show popup to select CE or PE
    }

    fun selectCallOption(strike: Double) {
        val option = OptionContract(
            symbol = "BTCUSD_${strike}CE",
            strikePrice = strike,
            type = "CE",
            expiry = "Weekly",
            lastPrice = 100.0
        )
        _orderState.update { it.copy(selectedOption = option, showOrderSheet = true) }
        calculateMargin()
    }

    fun selectPutOption(strike: Double) {
        val option = OptionContract(
            symbol = "BTCUSD_${strike}PE",
            strikePrice = strike,
            type = "PE",
            expiry = "Weekly",
            lastPrice = 100.0
        )
        _orderState.update { it.copy(selectedOption = option, showOrderSheet = true) }
        calculateMargin()
    }

    fun updateLots(lots: Int) {
        _orderState.update { it.copy(lots = lots) }
        calculateMargin()
    }

    fun updateOrderType(orderType: String) {
        _orderState.update { it.copy(orderType = orderType) }
    }

    fun updateProductType(productType: String) {
        _orderState.update { it.copy(productType = productType) }
    }

    private fun calculateMargin() {
        val option = _orderState.value.selectedOption ?: return
        val lots = _orderState.value.lots
        val lotSize = _orderState.value.lotSize

        val orderValue = option.lastPrice * lots * lotSize
        val marginRequired = orderValue * 0.2 // Simplified calculation

        _orderState.update { it.copy(marginRequired = marginRequired) }
    }

    fun dismissOrderSheet() {
        _orderState.update { it.copy(showOrderSheet = false) }
    }
}
