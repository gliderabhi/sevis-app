package com.sevis.app.data.repository

import com.sevis.app.data.model.ImportResult
import com.sevis.app.data.model.PagedResponse
import com.sevis.app.data.model.Part
import com.sevis.app.data.model.PartBatchRow
import com.sevis.app.data.remote.PartApiService

class PartRepository(private val api: PartApiService) {

    suspend fun getParts(page: Int, size: Int): Result<PagedResponse<Part>> =
        runCatching { api.getAll(page, size) }

    suspend fun getById(id: Long): Result<Part> =
        runCatching { api.getById(id) }

    suspend fun search(query: String): Result<List<Part>> =
        runCatching { api.search(query) }

    suspend fun importBatch(rows: List<PartBatchRow>): Result<ImportResult> =
        runCatching { api.importBatch(rows) }
}
