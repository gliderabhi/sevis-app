package com.sevis.app.data.remote

import com.sevis.app.data.config.Environment
import com.sevis.app.data.model.InventoryItem
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class InventoryApiService(private val client: HttpClient) {

    suspend fun getAll(): List<InventoryItem> =
        client.get("${Environment.baseUrl}/inventory-service/api/inventory") { bearerAuth() }.body()

    suspend fun getById(id: Long): InventoryItem =
        client.get("${Environment.baseUrl}/inventory-service/api/inventory/$id") { bearerAuth() }.body()

    suspend fun create(item: InventoryItem): InventoryItem =
        client.post("${Environment.baseUrl}/inventory-service/api/inventory") {
            contentType(ContentType.Application.Json)
            setBody(item)
            bearerAuth()
        }.body()

    suspend fun update(id: Long, item: InventoryItem): InventoryItem =
        client.put("${Environment.baseUrl}/inventory-service/api/inventory/$id") {
            contentType(ContentType.Application.Json)
            setBody(item)
            bearerAuth()
        }.body()

    suspend fun delete(id: Long) =
        client.delete("${Environment.baseUrl}/inventory-service/api/inventory/$id") { bearerAuth() }
}
