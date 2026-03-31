package com.sevis.app.data.repository

import com.sevis.app.data.model.AncillaryItemRequest
import com.sevis.app.data.model.CreateJobCardRequest
import com.sevis.app.data.model.JobCardDetail
import com.sevis.app.data.model.JobCardSummary
import com.sevis.app.data.model.LabourItemRequest
import com.sevis.app.data.model.PartItemRequest
import com.sevis.app.data.remote.JobCardApiService

class JobCardRepository(private val api: JobCardApiService) {

    suspend fun getAll(): Result<List<JobCardSummary>> = runCatching { api.getAll() }

    suspend fun getById(id: Long): Result<JobCardDetail> = runCatching { api.getById(id) }

    suspend fun create(request: CreateJobCardRequest): Result<JobCardDetail> =
        runCatching { api.create(request) }

    suspend fun updateStatus(id: Long, status: String): Result<JobCardDetail> =
        runCatching { api.updateStatus(id, status) }

    suspend fun downloadPdf(id: Long): Result<ByteArray> =
        runCatching { api.downloadPdf(id) }

    suspend fun addLabour(id: Long, req: LabourItemRequest): Result<JobCardDetail> =
        runCatching { api.addLabour(id, req) }

    suspend fun deleteLabour(id: Long, labourId: Long): Result<JobCardDetail> =
        runCatching { api.deleteLabour(id, labourId) }

    suspend fun addPart(id: Long, req: PartItemRequest): Result<JobCardDetail> =
        runCatching { api.addPart(id, req) }

    suspend fun deletePart(id: Long, partId: Long): Result<JobCardDetail> =
        runCatching { api.deletePart(id, partId) }

    suspend fun addAncillary(id: Long, req: AncillaryItemRequest): Result<JobCardDetail> =
        runCatching { api.addAncillary(id, req) }

    suspend fun deleteAncillary(id: Long, ancId: Long): Result<JobCardDetail> =
        runCatching { api.deleteAncillary(id, ancId) }
}
