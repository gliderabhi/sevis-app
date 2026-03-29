package com.sevis.app.data.model

import kotlinx.serialization.Serializable

// ✅ Error model
@Serializable
data class ApiError(
    val status: Int,
    val error: String
)