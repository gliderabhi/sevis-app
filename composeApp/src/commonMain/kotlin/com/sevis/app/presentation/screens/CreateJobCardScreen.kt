package com.sevis.app.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sevis.app.data.model.*
import com.sevis.app.presentation.components.SevisButton
import com.sevis.app.presentation.components.SevisDropdownField
import com.sevis.app.presentation.components.SevisTextField

private val SERVICE_TYPES  = listOf("PERIODIC_SERVICE", "RUNNING_REPAIR", "BODYWORK", "INSPECTION", "ACCIDENTAL", "WARRANTY")
private val FUEL_TYPES     = listOf("PETROL", "DIESEL", "CNG", "ELECTRIC", "HYBRID")
private val LABOUR_TYPES   = listOf("LABOUR", "INSPECTION", "SUBLET")
private val PART_TYPES     = listOf("OEM", "AM", "DEALER_SUPPLY")
private val PAYMENT_TYPES  = listOf("CASH", "CARD", "ONLINE", "CREDIT")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateJobCardScreen(
    modifier: Modifier = Modifier,
    isCreating: Boolean,
    error: String?,
    onSubmit: (CreateJobCardRequest) -> Unit,
    onBack: () -> Unit
) {
    // Customer fields
    var custPhone   by remember { mutableStateOf("") }
    var custName    by remember { mutableStateOf("") }
    var custEmail   by remember { mutableStateOf("") }
    var custAddress by remember { mutableStateOf("") }

    // Vehicle fields
    var vehReg      by remember { mutableStateOf("") }
    var vehMake     by remember { mutableStateOf("") }
    var vehModel    by remember { mutableStateOf("") }
    var vehVariant  by remember { mutableStateOf("") }
    var vehYear     by remember { mutableStateOf("") }
    var vehChassis  by remember { mutableStateOf("") }
    var vehEngine   by remember { mutableStateOf("") }
    var vehColor    by remember { mutableStateOf("") }
    var vehFuel     by remember { mutableStateOf(FUEL_TYPES.first()) }

    // Job info
    var serviceType    by remember { mutableStateOf(SERVICE_TYPES.first()) }
    var kmIn           by remember { mutableStateOf("") }
    var expectedDate   by remember { mutableStateOf("") }
    var complaint      by remember { mutableStateOf("") }
    var advisorName    by remember { mutableStateOf("") }

    // Labour items — mutable list of (description, type, qty, rate)
    val labourItems = remember { mutableStateListOf<LabourItemDraft>() }

    // Parts — mutable list of (partNumber, description, partType, qty, unitPrice)
    val partItems = remember { mutableStateListOf<PartItemDraft>() }

    // Ancillary
    val ancillaryItems = remember { mutableStateListOf<AncillaryItemDraft>() }

    // Checks
    var fuelLevel    by remember { mutableStateOf("") }
    var tireFLPsi    by remember { mutableStateOf("") }
    var tireRLPsi    by remember { mutableStateOf("") }
    var tireFRPsi    by remember { mutableStateOf("") }
    var tireRRPsi    by remember { mutableStateOf("") }
    var tireSparePsi by remember { mutableStateOf("") }
    var hasToolKit   by remember { mutableStateOf(false) }
    var hasStepney   by remember { mutableStateOf(false) }
    var hasBrochure  by remember { mutableStateOf(false) }
    var hasInsurance by remember { mutableStateOf(false) }
    var hasPUC       by remember { mutableStateOf(false) }
    var hasRC        by remember { mutableStateOf(false) }
    var checkNotes   by remember { mutableStateOf("") }

    // Billing
    var discount      by remember { mutableStateOf("") }
    var cgstRate      by remember { mutableStateOf("") }
    var sgstRate      by remember { mutableStateOf("") }
    var igstRate      by remember { mutableStateOf("") }
    var advanceAmt    by remember { mutableStateOf("") }
    var paymentType   by remember { mutableStateOf(PAYMENT_TYPES.first()) }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("New Job Card") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Customer ─────────────────────────────────────────────────────
            SectionHeader("Customer")
            SevisTextField(custPhone,   { custPhone = it },   "Phone Number *", keyboardType = KeyboardType.Phone)
            SevisTextField(custName,    { custName = it },    "Full Name *")
            SevisTextField(custEmail,   { custEmail = it },   "Email", keyboardType = KeyboardType.Email)
            SevisTextField(custAddress, { custAddress = it }, "Address")

            // ── Vehicle ──────────────────────────────────────────────────────
            SectionHeader("Vehicle")
            SevisTextField(vehReg,     { vehReg = it.uppercase() }, "Registration Number *")
            SevisTextField(vehMake,    { vehMake = it },    "Make *")
            SevisTextField(vehModel,   { vehModel = it },   "Model *")
            SevisTextField(vehVariant, { vehVariant = it }, "Variant")
            SevisTextField(vehYear,    { vehYear = it },    "Year", keyboardType = KeyboardType.Number)
            SevisTextField(vehChassis, { vehChassis = it }, "Chassis No")
            SevisTextField(vehEngine,  { vehEngine = it },  "Engine No")
            SevisTextField(vehColor,   { vehColor = it },   "Color")
            SevisDropdownField(vehFuel, { vehFuel = it }, "Fuel Type", FUEL_TYPES)

            // ── Job Info ─────────────────────────────────────────────────────
            SectionHeader("Job Details")
            SevisDropdownField(serviceType, { serviceType = it }, "Service Type *", SERVICE_TYPES)
            SevisTextField(kmIn,         { kmIn = it },         "KM Reading *", keyboardType = KeyboardType.Number)
            SevisTextField(expectedDate, { expectedDate = it }, "Expected Delivery (YYYY-MM-DD)")
            SevisTextField(complaint,    { complaint = it },    "Customer Complaint")
            SevisTextField(advisorName,  { advisorName = it },  "Service Advisor Name")

            // ── Labour Items ─────────────────────────────────────────────────
            SectionHeader("Labour / Work Done")
            labourItems.forEachIndexed { idx, item ->
                LabourItemRow(
                    item     = item,
                    onUpdate = { labourItems[idx] = it },
                    onRemove = { labourItems.removeAt(idx) }
                )
            }
            OutlinedButton(
                onClick = { labourItems.add(LabourItemDraft()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Add Labour Item")
            }

            // ── Parts ─────────────────────────────────────────────────────────
            SectionHeader("Parts Used")
            partItems.forEachIndexed { idx, item ->
                PartItemRow(
                    item     = item,
                    onUpdate = { partItems[idx] = it },
                    onRemove = { partItems.removeAt(idx) }
                )
            }
            OutlinedButton(
                onClick = { partItems.add(PartItemDraft()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Add Part")
            }

            // ── Ancillary ─────────────────────────────────────────────────────
            SectionHeader("Ancillary Services")
            ancillaryItems.forEachIndexed { idx, item ->
                AncillaryItemRow(
                    item     = item,
                    onUpdate = { ancillaryItems[idx] = it },
                    onRemove = { ancillaryItems.removeAt(idx) }
                )
            }
            OutlinedButton(
                onClick = { ancillaryItems.add(AncillaryItemDraft()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Add Ancillary Item")
            }

            // ── Pre-delivery Checks ───────────────────────────────────────────
            SectionHeader("Pre-delivery Checks")
            SevisTextField(fuelLevel,    { fuelLevel = it },    "Fuel Level (0-100%)", keyboardType = KeyboardType.Number)
            SevisTextField(tireFLPsi,    { tireFLPsi = it },    "Front-Left Tyre PSI",  keyboardType = KeyboardType.Number)
            SevisTextField(tireRLPsi,    { tireRLPsi = it },    "Rear-Left Tyre PSI",   keyboardType = KeyboardType.Number)
            SevisTextField(tireFRPsi,    { tireFRPsi = it },    "Front-Right Tyre PSI", keyboardType = KeyboardType.Number)
            SevisTextField(tireRRPsi,    { tireRRPsi = it },    "Rear-Right Tyre PSI",  keyboardType = KeyboardType.Number)
            SevisTextField(tireSparePsi, { tireSparePsi = it }, "Spare Tyre PSI",       keyboardType = KeyboardType.Number)

            CheckboxRow("Tool Kit Present",  hasToolKit)  { hasToolKit  = it }
            CheckboxRow("Stepney Present",   hasStepney)  { hasStepney  = it }
            CheckboxRow("Brochure / Manual", hasBrochure) { hasBrochure = it }
            CheckboxRow("Insurance Papers",  hasInsurance){ hasInsurance = it }
            CheckboxRow("PUC Certificate",   hasPUC)      { hasPUC      = it }
            CheckboxRow("RC Book",           hasRC)       { hasRC       = it }

            SevisTextField(checkNotes, { checkNotes = it }, "Damage / Other Notes")

            // ── Billing ───────────────────────────────────────────────────────
            SectionHeader("Billing")
            SevisTextField(discount,   { discount = it },   "Discount (₹)",  keyboardType = KeyboardType.Decimal)
            SevisTextField(cgstRate,   { cgstRate = it },   "CGST Rate (%)", keyboardType = KeyboardType.Decimal)
            SevisTextField(sgstRate,   { sgstRate = it },   "SGST Rate (%)", keyboardType = KeyboardType.Decimal)
            SevisTextField(igstRate,   { igstRate = it },   "IGST Rate (%)", keyboardType = KeyboardType.Decimal)
            SevisTextField(advanceAmt, { advanceAmt = it }, "Advance Amount (₹)", keyboardType = KeyboardType.Decimal)
            SevisDropdownField(paymentType, { paymentType = it }, "Payment Type", PAYMENT_TYPES)

            if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            SevisButton(
                text      = "Create Job Card",
                isLoading = isCreating,
                enabled   = !isCreating,
                onClick   = {
                    val req = buildRequest(
                        custPhone, custName, custEmail, custAddress,
                        vehReg, vehMake, vehModel, vehVariant, vehYear, vehChassis, vehEngine, vehColor, vehFuel,
                        serviceType, kmIn, expectedDate, complaint, advisorName,
                        labourItems, partItems, ancillaryItems,
                        fuelLevel, tireFLPsi, tireRLPsi, tireFRPsi, tireRRPsi, tireSparePsi,
                        hasToolKit, hasStepney, hasBrochure, hasInsurance, hasPUC, hasRC, checkNotes,
                        discount, cgstRate, sgstRate, igstRate, advanceAmt, paymentType
                    )
                    if (req != null) onSubmit(req)
                }
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Builder ───────────────────────────────────────────────────────────────────

private fun buildRequest(
    custPhone: String, custName: String, custEmail: String, custAddress: String,
    vehReg: String, vehMake: String, vehModel: String, vehVariant: String,
    vehYear: String, vehChassis: String, vehEngine: String, vehColor: String, vehFuel: String,
    serviceType: String, kmIn: String, expectedDate: String, complaint: String, advisorName: String,
    labourItems: List<LabourItemDraft>, partItems: List<PartItemDraft>, ancillaryItems: List<AncillaryItemDraft>,
    fuelLevel: String, tireFLPsi: String, tireRLPsi: String, tireFRPsi: String,
    tireRRPsi: String, tireSparePsi: String,
    hasToolKit: Boolean, hasStepney: Boolean, hasBrochure: Boolean,
    hasInsurance: Boolean, hasPUC: Boolean, hasRC: Boolean, checkNotes: String,
    discount: String, cgstRate: String, sgstRate: String, igstRate: String,
    advanceAmt: String, paymentType: String
): CreateJobCardRequest? {
    if (custPhone.isBlank() || custName.isBlank()) return null
    if (vehReg.isBlank() || vehMake.isBlank() || vehModel.isBlank()) return null
    val kmValue = kmIn.toIntOrNull() ?: return null

    return CreateJobCardRequest(
        customer = CustomerRequest(
            phone   = custPhone.trim(),
            name    = custName.trim(),
            email   = custEmail.ifBlank { null },
            address = custAddress.ifBlank { null }
        ),
        vehicle = VehicleRequest(
            regNumber  = vehReg.trim().uppercase(),
            make       = vehMake.trim(),
            model      = vehModel.trim(),
            variant    = vehVariant.ifBlank { null },
            year       = vehYear.toIntOrNull(),
            chassisNo  = vehChassis.ifBlank { null },
            engineNo   = vehEngine.ifBlank { null },
            color      = vehColor.ifBlank { null },
            fuelType   = vehFuel
        ),
        serviceType       = serviceType,
        kmIn              = kmValue,
        expectedDelivery  = expectedDate.ifBlank { null },
        customerComplaint = complaint.ifBlank { null },
        advisorName       = advisorName.ifBlank { null },
        labourItems = labourItems.mapNotNull { d ->
            if (d.description.isBlank()) null
            else LabourItemRequest(d.description, d.type, d.quantity.toIntOrNull() ?: 1, d.rate.toDoubleOrNull() ?: 0.0)
        },
        parts = partItems.mapNotNull { d ->
            if (d.partNumber.isBlank() || d.description.isBlank()) null
            else PartItemRequest(d.partNumber, d.description, d.partType, d.quantity.toIntOrNull() ?: 1, d.unitPrice.toDoubleOrNull() ?: 0.0)
        },
        ancillaryItems = ancillaryItems.mapNotNull { d ->
            if (d.description.isBlank()) null
            else AncillaryItemRequest(d.description, d.amount.toDoubleOrNull() ?: 0.0)
        },
        checks = ChecksRequest(
            fuelLevel    = fuelLevel.toIntOrNull() ?: 0,
            tireFLPsi    = tireFLPsi.ifBlank { null },
            tireRLPsi    = tireRLPsi.ifBlank { null },
            tireFRPsi    = tireFRPsi.ifBlank { null },
            tireRRPsi    = tireRRPsi.ifBlank { null },
            tireSparePsi = tireSparePsi.ifBlank { null },
            hasToolKit   = hasToolKit,
            hasStepney   = hasStepney,
            hasBrochure  = hasBrochure,
            hasInsurance = hasInsurance,
            hasPUC       = hasPUC,
            hasRC        = hasRC,
            notes        = checkNotes.ifBlank { null }
        ),
        billing = BillingRequest(
            discount      = discount.toDoubleOrNull() ?: 0.0,
            cgstRate      = cgstRate.toDoubleOrNull() ?: 0.0,
            sgstRate      = sgstRate.toDoubleOrNull() ?: 0.0,
            igstRate      = igstRate.toDoubleOrNull() ?: 0.0,
            advanceAmount = advanceAmt.toDoubleOrNull() ?: 0.0,
            paymentType   = paymentType
        )
    )
}

// ── Draft data classes ────────────────────────────────────────────────────────

data class LabourItemDraft(
    val description: String = "",
    val type: String = "LABOUR",
    val quantity: String = "1",
    val rate: String = ""
)

data class PartItemDraft(
    val partNumber: String = "",
    val description: String = "",
    val partType: String = "OEM",
    val quantity: String = "1",
    val unitPrice: String = ""
)

data class AncillaryItemDraft(
    val description: String = "",
    val amount: String = ""
)

// ── Row composables ───────────────────────────────────────────────────────────

@Composable
private fun LabourItemRow(item: LabourItemDraft, onUpdate: (LabourItemDraft) -> Unit, onRemove: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Labour Item", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                }
            }
            SevisTextField(item.description, { onUpdate(item.copy(description = it)) }, "Description *")
            SevisDropdownField(item.type, { onUpdate(item.copy(type = it)) }, "Type", LABOUR_TYPES)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SevisTextField(item.quantity, { onUpdate(item.copy(quantity = it)) }, "Qty",
                    modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number)
                SevisTextField(item.rate, { onUpdate(item.copy(rate = it)) }, "Rate (₹)",
                    modifier = Modifier.weight(2f), keyboardType = KeyboardType.Decimal)
            }
        }
    }
}

@Composable
private fun PartItemRow(item: PartItemDraft, onUpdate: (PartItemDraft) -> Unit, onRemove: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Part", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                }
            }
            SevisTextField(item.partNumber, { onUpdate(item.copy(partNumber = it)) }, "Part Number *")
            SevisTextField(item.description, { onUpdate(item.copy(description = it)) }, "Description *")
            SevisDropdownField(item.partType, { onUpdate(item.copy(partType = it)) }, "Part Type", PART_TYPES)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SevisTextField(item.quantity, { onUpdate(item.copy(quantity = it)) }, "Qty",
                    modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number)
                SevisTextField(item.unitPrice, { onUpdate(item.copy(unitPrice = it)) }, "Unit Price (₹)",
                    modifier = Modifier.weight(2f), keyboardType = KeyboardType.Decimal)
            }
        }
    }
}

@Composable
private fun AncillaryItemRow(item: AncillaryItemDraft, onUpdate: (AncillaryItemDraft) -> Unit, onRemove: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SevisTextField(item.description, { onUpdate(item.copy(description = it)) }, "Description", modifier = Modifier.weight(2f))
            SevisTextField(item.amount, { onUpdate(item.copy(amount = it)) }, "Amount (₹)",
                modifier = Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text  = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
    HorizontalDivider()
}

@Composable
private fun CheckboxRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onChecked)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}
