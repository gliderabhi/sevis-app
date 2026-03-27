package com.sevis.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: Long = 0,
    val userId: Long = 0,
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: String = "",
    val createdAt: String = ""
)

@Serializable
data class OrderItem(
    val inventoryItemId: Long = 0,
    val quantity: Int = 0,
    val unitPrice: Double = 0.0
)
