package com.sevis.app.data.repository

import com.sevis.app.data.model.AuditSummary
import com.sevis.app.data.remote.AuditApiService

class AuditRepository(private val api: AuditApiService) {
    suspend fun getSummary(): Result<AuditSummary> = runCatching { api.getSummary() }
    suspend fun getStockValue(): Result<Double>    = runCatching { api.getStockValue() }
}
