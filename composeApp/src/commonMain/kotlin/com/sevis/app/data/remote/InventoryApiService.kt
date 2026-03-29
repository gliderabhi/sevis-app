package com.sevis.app.data.remote

import com.sevis.app.data.config.Environment
import com.sevis.app.data.model.InventoryItem
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType

class InventoryApiService(private val client: HttpClient) {

    // ✅ GET ALL
    suspend fun getAll(): List<InventoryItem> {
        val response = client.get("${Environment.baseUrl}/inventory-service/api/inventory") {
            bearerAuth()
        }
        return safeCall(response)
    }

    // ✅ GET BY ID
    suspend fun getById(id: Long): InventoryItem {
        val response = client.get("${Environment.baseUrl}/inventory-service/api/inventory/$id") {
            bearerAuth()
        }
        return safeCall(response)
    }

    // ✅ CREATE
    suspend fun create(item: InventoryItem): InventoryItem {
        val response = client.post("${Environment.baseUrl}/inventory-service/api/inventory") {
            bearerAuth()
            contentType(ContentType.Application.Json)
            setBody(item)
        }
        return safeCall(response)
    }

    // ✅ UPDATE
    suspend fun update(id: Long, item: InventoryItem): InventoryItem {
        val response = client.put("${Environment.baseUrl}/inventory-service/api/inventory/$id") {
            bearerAuth()
            contentType(ContentType.Application.Json)
            setBody(item)
        }
        return safeCall(response)
    }

    // ✅ DELETE
    suspend fun delete(id: Long) {
        val response = client.delete("${Environment.baseUrl}/inventory-service/api/inventory/$id") {
            bearerAuth()
        }

        // Even if no body, still validate status
        if (response.status.value !in 200..299) {
            val errorText = response.bodyAsText()
            throw Exception("API ${response.status.value}: $errorText")
        }
    }
}