package com.sevis.app.data.remote

import com.sevis.app.data.config.Environment
import com.sevis.app.data.model.Bill
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType

class BillingApiService(private val client: HttpClient) {

    // ✅ GET ALL
    suspend fun getAll(): List<Bill> {
        val response = client.get("${Environment.baseUrl}/billing-service/api/bills") {
            bearerAuth()
        }
        return safeCall(response)
    }

    // ✅ GET BY ID
    suspend fun getById(id: Long): Bill {
        val response = client.get("${Environment.baseUrl}/billing-service/api/bills/$id") {
            bearerAuth()
        }
        return safeCall(response)
    }

    // ✅ GET BY USER ID
    suspend fun getByUserId(userId: Long): List<Bill> {
        val response = client.get("${Environment.baseUrl}/billing-service/api/bills/user/$userId") {
            bearerAuth()
        }
        return safeCall(response)
    }

    // ✅ CREATE
    suspend fun create(bill: Bill): Bill {
        val response = client.post("${Environment.baseUrl}/billing-service/api/bills") {
            bearerAuth()
            contentType(ContentType.Application.Json)
            setBody(bill)
        }
        return safeCall(response)
    }
}