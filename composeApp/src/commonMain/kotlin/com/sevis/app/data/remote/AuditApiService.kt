package com.sevis.app.data.remote

import com.sevis.app.data.config.Environment
import com.sevis.app.data.model.AuditSummary
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*

class AuditApiService(private val client: HttpClient) {

    suspend fun getSummary(): AuditSummary {
        val response = client.get("${Environment.baseUrl}/orders-service/api/audit/summary") {
            bearerAuth()
        }
        return safeCall(response)
    }

    suspend fun getStockValue(): Double {
        val response = client.get("${Environment.baseUrl}/inventory-service/api/stock/value") {
            bearerAuth()
        }
        return response.body()
    }
}
