package com.sevis.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AuditSummary(
    val openJobCards: Long = 0,
    val inProgressJobCards: Long = 0,
    val readyJobCards: Long = 0,
    val deliveredJobCards: Long = 0,
    val closedJobCards: Long = 0,
    val totalJobCards: Long = 0,
    val labourChargesTotal: Double = 0.0,
    val partsRevenueTotal: Double = 0.0,
    val ancillaryRevenueTotal: Double = 0.0,
    val totalRevenue: Double = 0.0,
    val revenueThisMonth: Double = 0.0,
    val invoicesThisMonth: Long = 0,
    val revenuePreviousMonth: Double = 0.0,
    val invoicesPreviousMonth: Long = 0,
    val totalInvoices: Long = 0,
    val averageInvoiceValue: Double = 0.0
)
