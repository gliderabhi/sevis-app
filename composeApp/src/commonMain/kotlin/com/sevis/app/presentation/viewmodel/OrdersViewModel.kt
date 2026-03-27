package com.sevis.app.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.sevis.app.data.model.Order
import com.sevis.app.data.repository.OrdersRepository
import com.sevis.app.presentation.BaseViewModel
import kotlinx.coroutines.launch

class OrdersViewModel(
    private val repository: OrdersRepository = OrdersRepository()
) : BaseViewModel<List<Order>>() {

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            setLoading()
            repository.getAll()
                .onSuccess { setData(it) }
                .onFailure { setError(it.message ?: "Failed to load orders") }
        }
    }

    fun loadOrdersByUser(userId: Long) {
        viewModelScope.launch {
            setLoading()
            repository.getByUserId(userId)
                .onSuccess { setData(it) }
                .onFailure { setError(it.message ?: "Failed to load orders") }
        }
    }

    fun updateStatus(id: Long, status: String) {
        viewModelScope.launch {
            repository.updateStatus(id, status)
                .onSuccess { loadOrders() }
                .onFailure { setError(it.message ?: "Failed to update order") }
        }
    }
}
