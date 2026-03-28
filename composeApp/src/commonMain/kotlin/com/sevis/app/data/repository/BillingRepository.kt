package com.sevis.app.data.repository

import com.sevis.app.data.model.Bill
import com.sevis.app.data.remote.BillingApiService

class BillingRepository(private val api: BillingApiService) {

    suspend fun getAll(): Result<List<Bill>> = runCatching { api.getAll() }

    suspend fun getById(id: Long): Result<Bill> = runCatching { api.getById(id) }

    suspend fun getByUserId(userId: Long): Result<List<Bill>> = runCatching { api.getByUserId(userId) }

    suspend fun create(bill: Bill): Result<Bill> = runCatching { api.create(bill) }
}
