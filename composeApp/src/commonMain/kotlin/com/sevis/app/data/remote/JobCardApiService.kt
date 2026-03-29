package com.sevis.app.data.remote

import com.sevis.app.data.config.Environment
import com.sevis.app.data.model.CreateJobCardRequest
import com.sevis.app.data.model.JobCardDetail
import com.sevis.app.data.model.JobCardSummary
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class JobCardApiService(private val client: HttpClient) {

    suspend fun getAll(): List<JobCardSummary> =
        client.get("${Environment.baseUrl}/orders-service/api/job-cards") { bearerAuth() }.body()

    suspend fun getById(id: Long): JobCardDetail =
        client.get("${Environment.baseUrl}/orders-service/api/job-cards/$id") { bearerAuth() }.body()

    suspend fun create(request: CreateJobCardRequest): JobCardDetail =
        client.post("${Environment.baseUrl}/orders-service/api/job-cards") {
            bearerAuth()
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun updateStatus(id: Long, status: String): JobCardDetail =
        client.patch("${Environment.baseUrl}/orders-service/api/job-cards/$id/status") {
            bearerAuth()
            parameter("status", status)
        }.body()
}
