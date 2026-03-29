package com.sevis.app.data.remote

import com.sevis.app.data.config.Environment
import com.sevis.app.data.model.User
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType

class UserApiService(private val client: HttpClient) {

    // ✅ GET ALL USERS
    suspend fun getAll(): List<User> {
        val response = client.get("${Environment.baseUrl}/user-service/api/users") {
            bearerAuth()
        }
        return safeCall(response)
    }

    // ✅ GET USER BY ID
    suspend fun getById(id: Long): User {
        val response = client.get("${Environment.baseUrl}/user-service/api/users/$id") {
            bearerAuth()
        }
        return safeCall(response)
    }

    // ✅ CREATE USER
    suspend fun create(user: User): User {
        val response = client.post("${Environment.baseUrl}/user-service/api/users") {
            bearerAuth()
            contentType(ContentType.Application.Json)
            setBody(user)
        }
        return safeCall(response)
    }

    // ✅ UPDATE USER
    suspend fun update(id: Long, user: User): User {
        val response = client.put("${Environment.baseUrl}/user-service/api/users/$id") {
            bearerAuth()
            contentType(ContentType.Application.Json)
            setBody(user)
        }
        return safeCall(response)
    }

    // ✅ DELETE USER
    suspend fun delete(id: Long) {
        val response = client.delete("${Environment.baseUrl}/user-service/api/users/$id") {
            bearerAuth()
        }

        // DELETE often returns 204 No Content
        if (response.status.value !in 200..299) {
            val errorText = response.bodyAsText()
            throw Exception("API ${response.status.value}: $errorText")
        }
    }
}