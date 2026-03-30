package com.sevis.app.data.repository

import com.sevis.app.data.model.InvoiceDetail
import com.sevis.app.data.remote.InvoiceApiService

class InvoiceRepository(private val api: InvoiceApiService) {

    suspend fun getByJobCard(jobCardId: Long): Result<List<InvoiceDetail>> =
        runCatching { api.getByJobCard(jobCardId) }

    suspend fun getById(id: Long): Result<InvoiceDetail> =
        runCatching { api.getById(id) }

    suspend fun uploadPdf(pdfBytes: ByteArray): Result<InvoiceDetail> =
        runCatching { api.uploadPdf(pdfBytes) }
}
