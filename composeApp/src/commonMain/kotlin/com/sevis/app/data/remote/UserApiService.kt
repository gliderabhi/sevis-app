package com.sevis.app.data.remote

import com.sevis.app.data.config.Environment
import com.sevis.app.data.model.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class UserApiService(private val client: HttpClient) {

    suspend fun getAll(): List<User> =
        client.get("${Environment.baseUrl}/user-service/api/users") { bearerAuth() }.body()

    suspend fun getById(id: Long): User =
        client.get("${Environment.baseUrl}/user-service/api/users/$id") { bearerAuth() }.body()

    suspend fun create(user: User): User =
        client.post("${Environment.baseUrl}/user-service/api/users") {
            contentType(ContentType.Application.Json)
            setBody(user)
            bearerAuth()
        }.body()

    suspend fun update(id: Long, user: User): User =
        client.put("${Environment.baseUrl}/user-service/api/users/$id") {
            contentType(ContentType.Application.Json)
            setBody(user)
            bearerAuth()
        }.body()

    suspend fun delete(id: Long) =
        client.delete("${Environment.baseUrl}/user-service/api/users/$id") { bearerAuth() }
}
