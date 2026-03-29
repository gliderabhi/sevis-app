package com.sevis.app.data.remote

import com.sevis.app.data.config.Environment
import com.sevis.app.data.model.Order
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType

class OrdersApiService(private val client: HttpClient) {

    // ✅ GET ALL
    suspend fun getAll(): List<Order> {
        val response = client.get("${Environment.baseUrl}/orders-service/api/orders") {
            bearerAuth()
        }
        return safeCall(response)
    }

    // ✅ GET BY ID
    suspend fun getById(id: Long): Order {
        val response = client.get("${Environment.baseUrl}/orders-service/api/orders/$id") {
            bearerAuth()
        }
        return safeCall(response)
    }

    // ✅ GET BY USER ID
    suspend fun getByUserId(userId: Long): List<Order> {
        val response = client.get("${Environment.baseUrl}/orders-service/api/orders/user/$userId") {
            bearerAuth()
        }
        return safeCall(response)
    }

    // ✅ CREATE
    suspend fun create(order: Order): Order {
        val response = client.post("${Environment.baseUrl}/orders-service/api/orders") {
            bearerAuth()
            contentType(ContentType.Application.Json)
            setBody(order)
        }
        return safeCall(response)
    }

    // ✅ UPDATE STATUS (cleaner query param handling)
    suspend fun updateStatus(id: Long, status: String): Order {
        val response = client.put("${Environment.baseUrl}/orders-service/api/orders/$id/status") {
            bearerAuth()
            parameter("status", status)   // ✅ better than manual string concat
        }
        return safeCall(response)
    }
}