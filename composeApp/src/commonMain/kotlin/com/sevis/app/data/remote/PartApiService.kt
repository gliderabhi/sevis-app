package com.sevis.app.data.remote

import com.sevis.app.data.config.Environment
import com.sevis.app.data.model.ImportResult
import com.sevis.app.data.model.PagedResponse
import com.sevis.app.data.model.Part
import com.sevis.app.data.model.PartBatchRow
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType

class PartApiService(private val client: HttpClient) {

    // ✅ GET ALL (paginated)
    suspend fun getAll(page: Int, size: Int): PagedResponse<Part> {
        val response = client.get("${Environment.baseUrl}/inventory-service/api/parts") {
            bearerAuth()
            parameter("page", page)   // ✅ safer than string concat
            parameter("size", size)
        }
        return safeCall(response)
    }

    // ✅ GET BY ID
    suspend fun getById(id: Long): Part {
        val response = client.get("${Environment.baseUrl}/inventory-service/api/parts/$id") {
            bearerAuth()
        }
        return safeCall(response)
    }

    // ✅ IMPORT BATCH
    suspend fun importBatch(rows: List<PartBatchRow>): ImportResult {
        val response = client.post("${Environment.baseUrl}/inventory-service/api/parts/batch") {
            bearerAuth()
            contentType(ContentType.Application.Json)
            setBody(rows)
        }
        return safeCall(response)
    }
}