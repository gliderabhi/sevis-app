package com.sevis.app.data.repository

import com.sevis.app.data.model.ImportResult
import com.sevis.app.data.model.PagedResponse
import com.sevis.app.data.model.Part
import com.sevis.app.data.remote.PartApiService

class PartRepository(private val api: PartApiService) {

    suspend fun getParts(page: Int, size: Int): Result<PagedResponse<Part>> =
        runCatching { api.getAll(page, size) }

    suspend fun getById(id: Long): Result<Part> =
        runCatching { api.getById(id) }

    suspend fun importCsv(bytes: ByteArray, filename: String): Result<ImportResult> =
        runCatching { api.importCsv(bytes, filename) }
}
