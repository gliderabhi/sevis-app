package com.sevis.app.data.remote

import com.sevis.app.data.model.AuthResponse
import com.sevis.app.data.model.LoginRequest
import com.sevis.app.data.model.SignupRequest
import com.sevis.app.data.auth.TokenManager
import com.sevis.app.data.config.Environment
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class AuthApiService {

    suspend fun login(request: LoginRequest): AuthResponse =
        httpClient.post("${Environment.baseUrl}/user-service/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun signup(request: SignupRequest): AuthResponse =
        httpClient.post("${Environment.baseUrl}/user-service/api/auth/signup") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun logout() {
        TokenManager.token?.let { token ->
            httpClient.post("${Environment.baseUrl}/user-service/api/auth/logout") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }
}
