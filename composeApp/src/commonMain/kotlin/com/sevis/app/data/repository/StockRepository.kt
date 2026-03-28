package com.sevis.app.data.repository

import com.sevis.app.data.model.StockItem
import com.sevis.app.data.model.StockRequest
import com.sevis.app.data.remote.StockApiService

class StockRepository(private val api: StockApiService) {

    suspend fun getAll(companyId: Long? = null): Result<List<StockItem>> =
        runCatching { api.getAll(companyId) }

    suspend fun upsert(request: StockRequest): Result<StockItem> =
        runCatching { api.upsert(request) }

    suspend fun delete(partNumber: String): Result<Unit> =
        runCatching { api.delete(partNumber) }
}
