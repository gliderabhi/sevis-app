package com.sevis.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class InvoiceLineItemInfo(
    val id: Long = 0,
    val lineNumber: Int? = null,
    val hsnCode: String? = null,
    val partNumber: String? = null,
    val description: String? = null,
    val type: String? = null,
    val quantity: Double? = null,
    val rate: Double? = null,
    val baseAmount: Double? = null,
    val discountAmount: Double? = null,
    val taxableAmount: Double? = null,
    val cgstRate: Double? = null,
    val cgstAmount: Double? = null,
    val sgstRate: Double? = null,
    val sgstAmount: Double? = null,
    val totalAmount: Double? = null
)

@Serializable
data class InvoiceDetail(
    val id: Long = 0,
    val invoiceNumber: String = "",
    val invoiceDate: String? = null,
    val originalJobCardNumber: String? = null,
    val jobCardDate: String? = null,
    val jobCardId: Long? = null,
    val dealerGstin: String? = null,
    val dealerPan: String? = null,
    val dealerName: String? = null,
    val serviceType: String? = null,
    val paymentMethod: String? = null,
    val vehicleRegNo: String? = null,
    val chassisNo: String? = null,
    val kms: Int? = null,
    val placeOfSupply: String? = null,
    val preparedBy: String? = null,
    val partsNetTaxableAmount: Double? = null,
    val totalTaxAmount: Double? = null,
    val grandTotal: Double? = null,
    val adjustments: Double? = null,
    val lineItems: List<InvoiceLineItemInfo> = emptyList()
)
