package com.sevis.app.data.model

import kotlinx.serialization.Serializable

// ── Request models ────────────────────────────────────────────────────────────

@Serializable
data class CustomerRequest(
    val phone: String,
    val name: String,
    val email: String? = null,
    val address: String? = null
)

@Serializable
data class VehicleRequest(
    val regNumber: String,
    val make: String,
    val model: String,
    val variant: String? = null,
    val year: Int? = null,
    val chassisNo: String? = null,
    val engineNo: String? = null,
    val color: String? = null,
    val fuelType: String? = null
)

@Serializable
data class LabourItemRequest(
    val description: String,
    val type: String = "LABOUR",
    val quantity: Int = 1,
    val rate: Double
)

@Serializable
data class PartItemRequest(
    val partNumber: String,
    val description: String,
    val partType: String = "OEM",
    val quantity: Int = 1,
    val unitPrice: Double
)

@Serializable
data class AncillaryItemRequest(
    val description: String,
    val amount: Double
)

@Serializable
data class ChecksRequest(
    val fuelLevel: Int = 0,
    val tireFLPsi: String? = null,
    val tireRLPsi: String? = null,
    val tireFRPsi: String? = null,
    val tireRRPsi: String? = null,
    val tireSparePsi: String? = null,
    val hasToolKit: Boolean = false,
    val hasStepney: Boolean = false,
    val hasBrochure: Boolean = false,
    val hasInsurance: Boolean = false,
    val hasPUC: Boolean = false,
    val hasRC: Boolean = false,
    val notes: String? = null
)

@Serializable
data class BillingRequest(
    val discount: Double = 0.0,
    val cgstRate: Double = 0.0,
    val sgstRate: Double = 0.0,
    val igstRate: Double = 0.0,
    val advanceAmount: Double = 0.0,
    val paymentType: String = "CASH"
)

@Serializable
data class CreateJobCardRequest(
    val customer: CustomerRequest,
    val vehicle: VehicleRequest,
    val serviceType: String,
    val kmIn: Int,
    val expectedDelivery: String? = null,
    val customerComplaint: String? = null,
    val advisorName: String? = null,
    val labourItems: List<LabourItemRequest> = emptyList(),
    val parts: List<PartItemRequest> = emptyList(),
    val ancillaryItems: List<AncillaryItemRequest> = emptyList(),
    val checks: ChecksRequest? = null,
    val billing: BillingRequest? = null
)

// ── Response models ───────────────────────────────────────────────────────────

@Serializable
data class JobCardSummary(
    val id: Long,
    val jobCardNumber: String,
    val customerName: String,
    val customerPhone: String,
    val vehicleRegNumber: String,
    val vehicleMakeModel: String,
    val serviceType: String,
    val status: String,
    val dateIn: String? = null,
    val kmIn: Int,
    val grandTotal: Double? = null
)

@Serializable
data class CustomerInfo(
    val id: Long,
    val name: String,
    val phone: String,
    val email: String? = null,
    val address: String? = null
)

@Serializable
data class VehicleInfo(
    val id: Long,
    val regNumber: String,
    val make: String,
    val model: String,
    val variant: String? = null,
    val year: Int? = null,
    val chassisNo: String? = null,
    val engineNo: String? = null,
    val color: String? = null,
    val fuelType: String? = null
)

@Serializable
data class LabourItemInfo(
    val id: Long,
    val description: String,
    val type: String,
    val quantity: Int,
    val rate: Double,
    val amount: Double
)

@Serializable
data class PartItemInfo(
    val id: Long,
    val partNumber: String,
    val description: String,
    val partType: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double
)

@Serializable
data class AncillaryItemInfo(
    val id: Long,
    val description: String,
    val amount: Double
)

@Serializable
data class ChecksInfo(
    val fuelLevel: Int,
    val tireFLPsi: String? = null,
    val tireRLPsi: String? = null,
    val tireFRPsi: String? = null,
    val tireRRPsi: String? = null,
    val tireSparePsi: String? = null,
    val hasToolKit: Boolean,
    val hasStepney: Boolean,
    val hasBrochure: Boolean,
    val hasInsurance: Boolean,
    val hasPUC: Boolean,
    val hasRC: Boolean,
    val notes: String? = null
)

@Serializable
data class BillingInfo(
    val labourTotal: Double,
    val partsTotal: Double,
    val ancillaryTotal: Double,
    val subTotal: Double,
    val discount: Double,
    val taxableAmount: Double,
    val cgstRate: Double,
    val cgstAmount: Double,
    val sgstRate: Double,
    val sgstAmount: Double,
    val igstRate: Double,
    val igstAmount: Double,
    val grandTotal: Double,
    val advanceAmount: Double,
    val balanceDue: Double,
    val paymentType: String
)

@Serializable
data class JobCardDetail(
    val id: Long,
    val jobCardNumber: String,
    val serviceType: String,
    val status: String,
    val kmIn: Int,
    val expectedDelivery: String? = null,
    val customerComplaint: String? = null,
    val technicianRemarks: String? = null,
    val advisorName: String? = null,
    val dateIn: String? = null,
    val dateOut: String? = null,
    val customer: CustomerInfo,
    val vehicle: VehicleInfo,
    val labourItems: List<LabourItemInfo> = emptyList(),
    val parts: List<PartItemInfo> = emptyList(),
    val ancillaryItems: List<AncillaryItemInfo> = emptyList(),
    val checks: ChecksInfo? = null,
    val billing: BillingInfo? = null
)
