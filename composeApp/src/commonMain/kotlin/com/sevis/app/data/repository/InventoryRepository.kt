package com.sevis.app.data.repository

import com.sevis.app.data.model.InventoryItem
import com.sevis.app.data.remote.InventoryApiService

class InventoryRepository(private val api: InventoryApiService) {

    suspend fun getAll(): Result<List<InventoryItem>> = runCatching { api.getAll() }

    suspend fun getById(id: Long): Result<InventoryItem> = runCatching { api.getById(id) }

    suspend fun create(item: InventoryItem): Result<InventoryItem> = runCatching { api.create(item) }

    suspend fun update(id: Long, item: InventoryItem): Result<InventoryItem> = runCatching { api.update(id, item) }

    suspend fun delete(id: Long): Result<Unit> = runCatching { api.delete(id) }
}
