package com.sevis.app.data.remote

import com.sevis.app.data.config.Environment
import com.sevis.app.data.model.ImportResult
import com.sevis.app.data.model.PagedResponse
import com.sevis.app.data.model.Part
import com.sevis.app.data.model.PartBatchRow
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class PartApiService(private val client: HttpClient) {

    suspend fun getAll(page: Int, size: Int): PagedResponse<Part> =
        client.get("${Environment.baseUrl}/inventory-service/api/parts?page=$page&size=$size") { bearerAuth() }.body()

    suspend fun getById(id: Long): Part =
        client.get("${Environment.baseUrl}/inventory-service/api/parts/$id") { bearerAuth() }.body()

    suspend fun importBatch(rows: List<PartBatchRow>): ImportResult =
        client.post("${Environment.baseUrl}/inventory-service/api/parts/batch") {
            bearerAuth()
            contentType(ContentType.Application.Json)
            setBody(rows)
        }.body()
}
