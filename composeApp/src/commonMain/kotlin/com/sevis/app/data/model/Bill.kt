package com.sevis.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Bill(
    val id: Long = 0,
    val userId: Long = 0,
    val orderId: Long = 0,
    val amount: Double = 0.0,
    val status: String = "",
    val createdAt: String = ""
)
