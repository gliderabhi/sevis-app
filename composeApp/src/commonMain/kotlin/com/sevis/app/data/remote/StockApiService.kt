package com.sevis.app.data.remote

import com.sevis.app.data.config.Environment
import com.sevis.app.data.model.StockItem
import com.sevis.app.data.model.StockRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

private const val BASE = "/inventory-service/api/stock"

class StockApiService(private val client: HttpClient) {

    suspend fun getAll(companyId: Long? = null): List<StockItem> {
        val url = if (companyId != null) "${Environment.baseUrl}$BASE?companyId=$companyId"
                  else "${Environment.baseUrl}$BASE"
        return client.get(url) { bearerAuth() }.body()
    }

    suspend fun getByPartNumber(partNumber: String): StockItem =
        client.get("${Environment.baseUrl}$BASE/$partNumber") { bearerAuth() }.body()

    suspend fun upsert(request: StockRequest): StockItem =
        client.post("${Environment.baseUrl}$BASE") {
            contentType(ContentType.Application.Json)
            setBody(request)
            bearerAuth()
        }.body()

    suspend fun delete(partNumber: String) =
        client.delete("${Environment.baseUrl}$BASE/$partNumber") { bearerAuth() }
}
