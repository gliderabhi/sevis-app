package com.sevis.app.data.repository

import com.sevis.app.data.model.Order
import com.sevis.app.data.remote.OrdersApiService

class OrdersRepository(private val api: OrdersApiService = OrdersApiService()) {

    suspend fun getAll(): Result<List<Order>> = runCatching { api.getAll() }

    suspend fun getById(id: Long): Result<Order> = runCatching { api.getById(id) }

    suspend fun getByUserId(userId: Long): Result<List<Order>> = runCatching { api.getByUserId(userId) }

    suspend fun create(order: Order): Result<Order> = runCatching { api.create(order) }

    suspend fun updateStatus(id: Long, status: String): Result<Order> = runCatching { api.updateStatus(id, status) }
}
