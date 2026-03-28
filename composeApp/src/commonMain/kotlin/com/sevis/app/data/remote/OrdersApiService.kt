package com.sevis.app.data.remote

import com.sevis.app.data.config.Environment
import com.sevis.app.data.model.Order
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class OrdersApiService(private val client: HttpClient) {

    suspend fun getAll(): List<Order> =
        client.get("${Environment.baseUrl}/orders-service/api/orders") { bearerAuth() }.body()

    suspend fun getById(id: Long): Order =
        client.get("${Environment.baseUrl}/orders-service/api/orders/$id") { bearerAuth() }.body()

    suspend fun getByUserId(userId: Long): List<Order> =
        client.get("${Environment.baseUrl}/orders-service/api/orders/user/$userId") { bearerAuth() }.body()

    suspend fun create(order: Order): Order =
        client.post("${Environment.baseUrl}/orders-service/api/orders") {
            contentType(ContentType.Application.Json)
            setBody(order)
            bearerAuth()
        }.body()

    suspend fun updateStatus(id: Long, status: String): Order =
        client.put("${Environment.baseUrl}/orders-service/api/orders/$id/status?status=$status") { bearerAuth() }.body()
}
