package com.sevis.app.data.repository

import com.sevis.app.data.auth.TokenManager
import com.sevis.app.data.model.SignupRequest
import com.sevis.app.data.remote.AuthApiService

class AuthRepository(private val api: AuthApiService = AuthApiService()) {

    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        val response = api.login(com.sevis.app.data.model.LoginRequest(email, password))
        val token = response.token ?: throw Exception("No token received from server")
        TokenManager.save(token)
    }

    suspend fun signup(request: SignupRequest): Result<Unit> = runCatching {
        api.signup(request)
    }

    suspend fun logout(): Result<Unit> = runCatching {
        api.logout()
        TokenManager.clear()
    }
}
