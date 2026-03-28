package com.sevis.app.data.remote

import com.sevis.app.data.config.Environment
import com.sevis.app.data.model.Bill
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class BillingApiService(private val client: HttpClient) {

    suspend fun getAll(): List<Bill> =
        client.get("${Environment.baseUrl}/billing-service/api/bills") { bearerAuth() }.body()

    suspend fun getById(id: Long): Bill =
        client.get("${Environment.baseUrl}/billing-service/api/bills/$id") { bearerAuth() }.body()

    suspend fun getByUserId(userId: Long): List<Bill> =
        client.get("${Environment.baseUrl}/billing-service/api/bills/user/$userId") { bearerAuth() }.body()

    suspend fun create(bill: Bill): Bill =
        client.post("${Environment.baseUrl}/billing-service/api/bills") {
            contentType(ContentType.Application.Json)
            setBody(bill)
            bearerAuth()
        }.body()
}
