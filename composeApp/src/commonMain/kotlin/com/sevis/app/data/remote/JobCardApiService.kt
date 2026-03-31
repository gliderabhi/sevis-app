package com.sevis.app.data.remote

import com.sevis.app.data.config.Environment
import com.sevis.app.data.model.AncillaryItemRequest
import com.sevis.app.data.model.ApiError
import com.sevis.app.data.model.CreateJobCardRequest
import com.sevis.app.data.model.JobCardDetail
import com.sevis.app.data.model.JobCardSummary
import com.sevis.app.data.model.LabourItemRequest
import com.sevis.app.data.model.PartItemRequest
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
        val response = client.patch("${Environment.baseUrl}/orders-service/api/job-cards/$id/status") {
            bearerAuth()
            parameter("status", status)
        }
        return safeCall(response)
    }

    // ✅ DOWNLOAD BILL PDF
    suspend fun downloadPdf(id: Long): ByteArray {
        val response = client.get("${Environment.baseUrl}/orders-service/api/job-cards/$id/pdf") {
            bearerAuth()
        }
        if (response.status.value !in 200..299)
            throw Exception("API ${response.status.value}: Failed to download bill")
        return response.body()
    }

    // ✅ LABOUR CRUD
    suspend fun addLabour(id: Long, req: LabourItemRequest): JobCardDetail {
        val response = client.post("${Environment.baseUrl}/orders-service/api/job-cards/$id/labour") {
            bearerAuth(); contentType(ContentType.Application.Json); setBody(req)
        }
        return safeCall(response)
    }

    suspend fun deleteLabour(id: Long, labourId: Long): JobCardDetail {
        val response = client.delete("${Environment.baseUrl}/orders-service/api/job-cards/$id/labour/$labourId") {
            bearerAuth()
        }
        return safeCall(response)
    }

    // ✅ PARTS CRUD
    suspend fun addPart(id: Long, req: PartItemRequest): JobCardDetail {
        val response = client.post("${Environment.baseUrl}/orders-service/api/job-cards/$id/parts") {
            bearerAuth(); contentType(ContentType.Application.Json); setBody(req)
        }
        return safeCall(response)
    }

    suspend fun deletePart(id: Long, partId: Long): JobCardDetail {
        val response = client.delete("${Environment.baseUrl}/orders-service/api/job-cards/$id/parts/$partId") {
            bearerAuth()
        }
        return safeCall(response)
    }

    // ✅ ANCILLARY CRUD
    suspend fun addAncillary(id: Long, req: AncillaryItemRequest): JobCardDetail {
        val response = client.post("${Environment.baseUrl}/orders-service/api/job-cards/$id/ancillary") {
            bearerAuth(); contentType(ContentType.Application.Json); setBody(req)
        }
        return safeCall(response)
    }

    suspend fun deleteAncillary(id: Long, ancId: Long): JobCardDetail {
        val response = client.delete("${Environment.baseUrl}/orders-service/api/job-cards/$id/ancillary/$ancId") {
            bearerAuth()
        }
        return safeCall(response)
    }
}