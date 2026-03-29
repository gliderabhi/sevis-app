package com.sevis.app.data.remote

import com.sevis.app.data.config.Environment
import com.sevis.app.data.model.ApiError
import com.sevis.app.data.model.CreateJobCardRequest
import com.sevis.app.data.model.JobCardDetail
import com.sevis.app.data.model.JobCardSummary
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.HttpResponse
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

class JobCardApiService(private val client: HttpClient) {

    suspend fun getAll(): List<JobCardSummary> {
        val response = client.get("${Environment.baseUrl}/orders-service/api/job-cards") {
            bearerAuth()
        }
        return safeCall(response)
    }

    // ✅ GET BY ID
    suspend fun getById(id: Long): JobCardDetail {
        val response = client.get("${Environment.baseUrl}/orders-service/api/job-cards/$id") {
            bearerAuth()
        }
        return safeCall(response)
    }

    // ✅ CREATE
    suspend fun create(request: CreateJobCardRequest): JobCardDetail {
        val response = client.post("${Environment.baseUrl}/orders-service/api/job-cards") {
            bearerAuth()
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return safeCall(response)
    }

    // ✅ UPDATE STATUS
    suspend fun updateStatus(id: Long, status: String): JobCardDetail {
        val response =
            client.patch("${Environment.baseUrl}/orders-service/api/job-cards/$id/status") {
                bearerAuth()
                parameter("status", status)
            }
        return safeCall(response)
    }
}