package com.sevis.app.data.remote

import com.sevis.app.data.config.Environment
import com.sevis.app.data.model.ImportResult
import com.sevis.app.data.model.PagedResponse
import com.sevis.app.data.model.Part
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

class PartApiService(private val client: HttpClient) {

    suspend fun getAll(page: Int, size: Int): PagedResponse<Part> =
        client.get("${Environment.baseUrl}/inventory-service/api/parts?page=$page&size=$size") { bearerAuth() }.body()

    suspend fun getById(id: Long): Part =
        client.get("${Environment.baseUrl}/inventory-service/api/parts/$id") { bearerAuth() }.body()

    suspend fun importCsv(bytes: ByteArray, filename: String): ImportResult =
        client.submitFormWithBinaryData(
            url = "${Environment.baseUrl}/inventory-service/api/parts/import",
            formData = formData {
                // Use the (key, bytes, contentType, filename) overload so Ktor generates a single
                // Content-Disposition: form-data; name="file"; filename="..."
                // Without a filename, Tomcat treats the part as "parameter data" (not a file upload)
                // and rejects it against the 2MB maxPostSize limit.
                append(
                    key   = "file",
                    value = bytes,
                    headers = Headers.build {
                        append(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString())
                        append(HttpHeaders.ContentDisposition, "form-data; name=\"file\"; filename=\"$filename\"")
                    }
                )
            }
        ) { bearerAuth() }.body()
}
