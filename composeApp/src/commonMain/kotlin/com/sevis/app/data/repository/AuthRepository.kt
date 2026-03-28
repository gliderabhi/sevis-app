package com.sevis.app.data.repository

import com.sevis.app.data.auth.TokenManager
import com.sevis.app.data.model.LoginRequest
import com.sevis.app.data.model.SignupRequest
import com.sevis.app.data.remote.AuthApiService

class AuthRepository(private val api: AuthApiService) {

    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        val response = api.login(LoginRequest(email, password))
        val token = response.token ?: throw Exception("No token received from server")
        TokenManager.save(
            token       = token,
            userId      = response.userId ?: 0L,
            role        = response.role ?: "",
            accountType = response.accountType ?: "",
            name        = response.name ?: ""
        )
    }

    suspend fun signup(request: SignupRequest): Result<Unit> = runCatching {
        api.signup(request)
    }

    suspend fun logout(): Result<Unit> = runCatching {
        api.logout()
        TokenManager.clear()
    }
}
