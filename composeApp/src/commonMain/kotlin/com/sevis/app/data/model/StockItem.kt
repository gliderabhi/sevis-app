package com.sevis.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class StockItem(
    val id: Long = 0,
    val companyId: Long = 0,
    val partNumber: String = "",
    val quantity: Int = 0,
    val purchasePrice: Double? = null,
    val lastUpdated: String = "",
    // Denormalized part details included by the API
    val description: String = "",
    val mrpPrice: Double = 0.0,
    val uom: String = "",
    val productGroup: String = "",
    val hsnCode: String = "",
    val taxSlab: String = ""
)

@Serializable
data class StockRequest(
    val partNumber: String,
    val quantity: Int,
    val purchasePrice: Double? = null
)

@Serializable
data class StockImportResult(
    val created: Int = 0,
    val updated: Int = 0,
    val skipped: Int = 0,
    val total: Int = 0,
    val message: String = ""
)
