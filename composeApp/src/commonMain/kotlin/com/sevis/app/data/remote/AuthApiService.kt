package com.sevis.app.data.remote

import com.sevis.app.data.auth.TokenManager
import com.sevis.app.data.config.Environment
import com.sevis.app.data.model.ApiError
import com.sevis.app.data.model.AuthResponse
import com.sevis.app.data.model.LoginRequest
import com.sevis.app.data.model.SignupRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
class AuthApiService(private val client: HttpClient) {

    suspend fun login(request: LoginRequest): AuthResponse {
        val response = client.post("${Environment.baseUrl}/user-service/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return safeCall(response)
    }

    // ✅ SIGNUP
    suspend fun signup(request: SignupRequest): AuthResponse {
        val response = client.post("${Environment.baseUrl}/user-service/api/auth/signup") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return safeCall(response)
    }

    // ✅ LOGOUT (no body expected)
    suspend fun logout() {
        TokenManager.token?.let { token ->
            val response = client.post("${Environment.baseUrl}/user-service/api/auth/logout") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }

            // Optional: still validate response
            if (response.status.value !in 200..299) {
                val error = runCatching { response.body<ApiError>() }.getOrNull()
                val message = error?.error ?: response.bodyAsText()
                throw Exception("Logout failed: $message")
            }
        }
    }
}
