package com.sevis.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class InventoryItem(
    val id: Long = 0,
    val name: String = "",
    val sku: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0
)
