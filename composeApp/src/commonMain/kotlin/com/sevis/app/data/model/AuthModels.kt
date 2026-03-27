package com.sevis.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class SignupRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: String,
    val accountType: String,
    val companyName: String? = null
)

@Serializable
data class AuthResponse(
    val token: String? = null,
    val message: String? = null
)
