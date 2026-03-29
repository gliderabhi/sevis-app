package com.sevis.app.data.repository

import com.sevis.app.data.model.CreateJobCardRequest
import com.sevis.app.data.model.JobCardDetail
import com.sevis.app.data.model.JobCardSummary
import com.sevis.app.data.remote.JobCardApiService

class JobCardRepository(private val api: JobCardApiService) {

    suspend fun getAll(): Result<List<JobCardSummary>> = runCatching { api.getAll() }

    suspend fun getById(id: Long): Result<JobCardDetail> = runCatching { api.getById(id) }

    suspend fun create(request: CreateJobCardRequest): Result<JobCardDetail> =
        runCatching { api.create(request) }

    suspend fun updateStatus(id: Long, status: String): Result<JobCardDetail> =
        runCatching { api.updateStatus(id, status) }
}
