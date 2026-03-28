package com.sevis.app.data.remote

import com.sevis.app.data.auth.TokenManager
import com.sevis.app.data.config.Environment
import com.sevis.app.data.model.Bill
import com.sevis.app.data.model.InventoryItem
import com.sevis.app.data.model.Order
import com.sevis.app.data.model.User
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

// All requests go through the gateway at localhost:8080
// Gateway routes: /user-service/**, /inventory-service/**, /billing-service/**, /orders-service/**

private fun HttpRequestBuilder.bearerAuth() {
    TokenManager.token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
}

class UserApiService {
    suspend fun getAll(): List<User> =
        httpClient.get("${Environment.baseUrl}/user-service/api/users") { bearerAuth() }.body()

    suspend fun getById(id: Long): User =
        httpClient.get("${Environment.baseUrl}/user-service/api/users/$id") { bearerAuth() }.body()

    suspend fun create(user: User): User =
        httpClient.post("${Environment.baseUrl}/user-service/api/users") {
            contentType(ContentType.Application.Json)
            setBody(user)
            bearerAuth()
        }.body()

    suspend fun update(id: Long, user: User): User =
        httpClient.put("${Environment.baseUrl}/user-service/api/users/$id") {
            contentType(ContentType.Application.Json)
            setBody(user)
            bearerAuth()
        }.body()

    suspend fun delete(id: Long) =
        httpClient.delete("${Environment.baseUrl}/user-service/api/users/$id") { bearerAuth() }
}

class InventoryApiService {
    suspend fun getAll(): List<InventoryItem> =
        httpClient.get("${Environment.baseUrl}/inventory-service/api/inventory").body()

    suspend fun getById(id: Long): InventoryItem =
        httpClient.get("${Environment.baseUrl}/inventory-service/api/inventory/$id").body()

    suspend fun create(item: InventoryItem): InventoryItem =
        httpClient.post("${Environment.baseUrl}/inventory-service/api/inventory") {
            contentType(ContentType.Application.Json)
            setBody(item)
        }.body()

    suspend fun update(id: Long, item: InventoryItem): InventoryItem =
        httpClient.put("${Environment.baseUrl}/inventory-service/api/inventory/$id") {
            contentType(ContentType.Application.Json)
            setBody(item)
        }.body()

    suspend fun delete(id: Long) =
        httpClient.delete("${Environment.baseUrl}/inventory-service/api/inventory/$id")
}

class BillingApiService {
    suspend fun getAll(): List<Bill> =
        httpClient.get("${Environment.baseUrl}/billing-service/api/bills").body()

    suspend fun getById(id: Long): Bill =
        httpClient.get("${Environment.baseUrl}/billing-service/api/bills/$id").body()

    suspend fun getByUserId(userId: Long): List<Bill> =
        httpClient.get("${Environment.baseUrl}/billing-service/api/bills/user/$userId").body()

    suspend fun create(bill: Bill): Bill =
        httpClient.post("${Environment.baseUrl}/billing-service/api/bills") {
            contentType(ContentType.Application.Json)
            setBody(bill)
        }.body()
}

class OrdersApiService {
    suspend fun getAll(): List<Order> =
        httpClient.get("${Environment.baseUrl}/orders-service/api/orders").body()

    suspend fun getById(id: Long): Order =
        httpClient.get("${Environment.baseUrl}/orders-service/api/orders/$id").body()

    suspend fun getByUserId(userId: Long): List<Order> =
        httpClient.get("${Environment.baseUrl}/orders-service/api/orders/user/$userId").body()

    suspend fun create(order: Order): Order =
        httpClient.post("${Environment.baseUrl}/orders-service/api/orders") {
            contentType(ContentType.Application.Json)
            setBody(order)
        }.body()

    suspend fun updateStatus(id: Long, status: String): Order =
        httpClient.put("${Environment.baseUrl}/orders-service/api/orders/$id/status?status=$status").body()
}
