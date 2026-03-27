package com.sevis.app.data.repository

import com.sevis.app.data.model.User
import com.sevis.app.data.remote.UserApiService

class UserRepository(private val api: UserApiService = UserApiService()) {

    suspend fun getAll(): Result<List<User>> = runCatching { api.getAll() }

    suspend fun getById(id: Long): Result<User> = runCatching { api.getById(id) }

    suspend fun create(user: User): Result<User> = runCatching { api.create(user) }

    suspend fun update(id: Long, user: User): Result<User> = runCatching { api.update(id, user) }

    suspend fun delete(id: Long): Result<Unit> = runCatching { api.delete(id) }
}
