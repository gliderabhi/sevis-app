package com.sevis.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Part(
    val id: Long = 0,
    val partNumber: String = "",
    val description: String = "",
    val mrpPrice: Double = 0.0,
    val purchasePrice: Double = 0.0,
    val uom: String = "",
    val productGroup: String = "",
    val hsnCode: String = "",
    val taxSlab: String = ""
)

@Serializable
data class PartBatchRow(
    val partNumber: String,
    val description: String,
    val mrpPrice: Double = 0.0,
    val purchasePrice: Double = 0.0,
    val uom: String = "",
    val productGroup: String = "",
    val hsnCode: String = "",
    val taxSlab: String = ""
)

@Serializable
data class PagedResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean,
    val number: Int        // current page
)
