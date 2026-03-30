package com.sevis.app.data.remote

import com.sevis.app.data.config.Environment
import com.sevis.app.data.model.InvoiceDetail
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType

private const val BASE = "/orders-service/api/invoices"

class InvoiceApiService(private val client: HttpClient) {

    suspend fun getByJobCard(jobCardId: Long): List<InvoiceDetail> {
        val response = client.get("${Environment.baseUrl}$BASE/job-card/$jobCardId") {
            bearerAuth()
        }
        return safeCall(response)
    }

    suspend fun getById(id: Long): InvoiceDetail {
        val response = client.get("${Environment.baseUrl}$BASE/$id") {
            bearerAuth()
        }
        return safeCall(response)
    }

    suspend fun uploadPdf(pdfBytes: ByteArray): InvoiceDetail {
        val response = client.post("${Environment.baseUrl}$BASE/upload") {
            bearerAuth()
            contentType(ContentType.Application.OctetStream)
            setBody(pdfBytes)
        }
        return safeCall(response)
    }

    suspend fun downloadPdf(id: Long): ByteArray {
        val response = client.get("${Environment.baseUrl}$BASE/$id/pdf") {
            bearerAuth()
        }
        if (response.status.value !in 200..299)
            throw Exception("API ${response.status.value}: Failed to download PDF")
        return response.body()
    }
}
