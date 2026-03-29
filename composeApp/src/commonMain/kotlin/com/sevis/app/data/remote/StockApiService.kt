package com.sevis.app.data.remote

import com.sevis.app.data.config.Environment
import com.sevis.app.data.model.StockItem
import com.sevis.app.data.model.StockRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType

private const val BASE = "/inventory-service/api/stock"

class StockApiService(private val client: HttpClient) {

    // ✅ GET ALL (with optional query param)
    suspend fun getAll(companyId: Long? = null): List<StockItem> {
        val response = client.get("${Environment.baseUrl}$BASE") {
            bearerAuth()
            companyId?.let { parameter("companyId", it) }  // ✅ safe param handling
        }
        return safeCall(response)
    }

    // ✅ GET BY PART NUMBER
    suspend fun getByPartNumber(partNumber: String): StockItem {
        val response = client.get("${Environment.baseUrl}$BASE/$partNumber") {
            bearerAuth()
        }
        return safeCall(response)
    }

    // ✅ UPSERT
    suspend fun upsert(request: StockRequest): StockItem {
        val response = client.post("${Environment.baseUrl}$BASE") {
            bearerAuth()
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return safeCall(response)
    }

    // ✅ DELETE
    suspend fun delete(partNumber: String) {
        val response = client.delete("${Environment.baseUrl}$BASE/$partNumber") {
            bearerAuth()
        }

        // handle empty/no-content safely
        if (response.status.value !in 200..299) {
            val errorText = response.bodyAsText()
            throw Exception("API ${response.status.value}: $errorText")
        }
    }
}