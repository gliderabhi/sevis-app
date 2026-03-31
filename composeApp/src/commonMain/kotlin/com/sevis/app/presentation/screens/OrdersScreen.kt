package com.sevis.app.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sevis.app.presentation.util.fmtRs
import androidx.compose.ui.unit.dp
import com.sevis.app.data.model.AncillaryItemRequest
import com.sevis.app.data.model.InvoiceDetail
import com.sevis.app.data.model.JobCardDetail
import com.sevis.app.data.model.JobCardSummary
import com.sevis.app.data.model.LabourItemRequest
import com.sevis.app.data.model.PartItemRequest
import com.sevis.app.presentation.util.FilePicker
import com.sevis.app.presentation.util.FileSaver
import com.sevis.app.presentation.viewmodel.JobCardScreen
import com.sevis.app.presentation.viewmodel.JobCardViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OrdersScreen(
    modifier: Modifier = Modifier,
    viewModel: JobCardViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadJobCards()
    }

    when (val screen = state.screen) {
        is JobCardScreen.List -> JobCardListContent(
            modifier    = modifier,
            jobCards    = state.jobCards,
            isLoading   = state.isLoading,
            isUploadingInvoice = state.isUploadingInvoice,
            invoiceError = state.invoiceError,
            error       = state.error,
            onCreateClick   = { viewModel.navigateToCreate() },
            onJobCardClick  = { id -> viewModel.navigateToDetail(id) },
            onRefresh       = { viewModel.loadJobCards() },
            onClearError    = { viewModel.clearError() },
            onUploadInvoice = { bytes -> viewModel.uploadInvoicePdf(bytes) },
            onClearInvoiceError = { viewModel.clearInvoiceError() }
        )
        is JobCardScreen.Create -> CreateJobCardScreen(
            modifier   = modifier,
            isCreating = state.isCreating,
            error      = state.error,
            onSubmit   = { request -> viewModel.createJobCard(request) },
            onBack     = { viewModel.navigateBack() }
        )
        is JobCardScreen.Detail -> JobCardDetailContent(
            modifier         = modifier,
            jobCard          = state.selectedJobCard,
            invoices         = state.invoices,
            isLoading        = state.isLoading,
            isUpdating       = state.isUpdating,
            isGeneratingInvoice = state.isGeneratingInvoice,
            isDownloadingPdf = state.isDownloadingPdf,
            pdfBytes         = state.pdfBytes,
            pdfFileName      = state.pdfFileName,
            pdfSaveMessage   = state.pdfSaveMessage,
            error            = state.error,
            invoiceError     = state.invoiceError,
            onBack           = { viewModel.navigateBack() },
            onStatusUpdate   = { status -> viewModel.updateStatus(screen.jobCardId, status) },
            onDownloadBill   = { viewModel.downloadJobCardPdf(screen.jobCardId, state.selectedJobCard?.jobCardNumber ?: "") },
            onDownloadInvoicePdf = { id, num -> viewModel.downloadInvoicePdf(id, num) },
            onPdfSaved       = { viewModel.onPdfSaved(it) },
            onPdfSaveError   = { viewModel.onPdfSaveError(it) },
            onClearPdfMessage   = { viewModel.clearPdfMessage() },
            onClearInvoiceError = { viewModel.clearInvoiceError() },
            onGenerateInvoice = { viewModel.generateInvoice(screen.jobCardId) },
            onAddLabour      = { req -> viewModel.addLabour(screen.jobCardId, req) },
            onDeleteLabour   = { lid -> viewModel.deleteLabour(screen.jobCardId, lid) },
            onAddPart        = { req -> viewModel.addPart(screen.jobCardId, req) },
            onDeletePart     = { pid -> viewModel.deletePart(screen.jobCardId, pid) },
            onAddAncillary   = { req -> viewModel.addAncillary(screen.jobCardId, req) },
            onDeleteAncillary= { aid -> viewModel.deleteAncillary(screen.jobCardId, aid) }
        )
    }
}

// ── List screen ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JobCardListContent(
    modifier: Modifier,
    jobCards: List<JobCardSummary>,
    isLoading: Boolean,
    isUploadingInvoice: Boolean,
    invoiceError: String?,
    error: String?,
    onCreateClick: () -> Unit,
    onJobCardClick: (Long) -> Unit,
    onRefresh: () -> Unit,
    onClearError: () -> Unit,
    onUploadInvoice: (ByteArray) -> Unit,
    onClearInvoiceError: () -> Unit
) {
    var showFilePicker by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    FilePicker(
        show = showFilePicker,
        onFilePicked = { _, bytes -> showFilePicker = false; onUploadInvoice(bytes) },
        onDismiss = { showFilePicker = false }
    )

    LaunchedEffect(invoiceError) {
        if (invoiceError != null) {
            snackbarMessage = invoiceError
        }
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallFloatingActionButton(onClick = { showFilePicker = true }) {
                    if (isUploadingInvoice) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Default.Upload, contentDescription = "Upload Invoice PDF")
                }
                FloatingActionButton(onClick = onCreateClick) {
                    Icon(Icons.Default.Add, contentDescription = "New Job Card")
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                error != null -> Column(
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { onClearError(); onRefresh() }) { Text("Retry") }
                }
                jobCards.isEmpty() -> Text(
                    "No job cards yet. Tap + to create one.",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(jobCards, key = { it.id }) { jc ->
                        JobCardSummaryCard(jc, onClick = { onJobCardClick(jc.id) })
                    }
                }
            }

            snackbarMessage?.let { msg ->
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    action = { TextButton(onClick = { snackbarMessage = null; onClearInvoiceError() }) { Text("Dismiss") } },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) { Text(msg) }
            }
        }
    }
}

@Composable
private fun JobCardSummaryCard(jc: JobCardSummary, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(jc.jobCardNumber, style = MaterialTheme.typography.titleMedium)
                Text(jc.customerName, style = MaterialTheme.typography.bodyMedium)
                Text("${jc.vehicleRegNumber}  •  ${jc.vehicleMakeModel}", style = MaterialTheme.typography.bodySmall)
                Text(jc.serviceType.replace("_", " "), style = MaterialTheme.typography.bodySmall)
                if (jc.dateIn != null) Text("In: ${jc.dateIn}  KM: ${jc.kmIn}", style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                StatusChip(jc.status)
                if (jc.grandTotal != null) {
                    Spacer(Modifier.height(4.dp))
                    Text("₹${jc.grandTotal.fmtRs(0)}", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val color = when (status) {
        "OPEN"        -> MaterialTheme.colorScheme.primary
        "IN_PROGRESS" -> MaterialTheme.colorScheme.tertiary
        "READY"       -> MaterialTheme.colorScheme.secondary
        "DELIVERED"   -> MaterialTheme.colorScheme.outline
        "CLOSED"      -> MaterialTheme.colorScheme.outline
        else          -> MaterialTheme.colorScheme.primary
    }
    SuggestionChip(
        onClick = {},
        label   = { Text(status.replace("_", " "), style = MaterialTheme.typography.labelSmall) },
        colors  = SuggestionChipDefaults.suggestionChipColors(labelColor = color)
    )
}

// ── Detail screen ─────────────────────────────────────────────────────────────

private val STATUS_TRANSITIONS = mapOf(
    "OPEN"        to listOf("IN_PROGRESS"),
    "IN_PROGRESS" to listOf("READY"),
    "READY"       to listOf("DELIVERED"),
    "DELIVERED"   to listOf("CLOSED"),
    "CLOSED"      to emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JobCardDetailContent(
    modifier: Modifier,
    jobCard: JobCardDetail?,
    invoices: List<InvoiceDetail>,
    isLoading: Boolean,
    isUpdating: Boolean,
    isGeneratingInvoice: Boolean,
    isDownloadingPdf: Boolean,
    pdfBytes: ByteArray?,
    pdfFileName: String,
    pdfSaveMessage: String?,
    error: String?,
    invoiceError: String?,
    onBack: () -> Unit,
    onStatusUpdate: (String) -> Unit,
    onDownloadBill: () -> Unit,
    onDownloadInvoicePdf: (Long, String) -> Unit,
    onPdfSaved: (String) -> Unit,
    onPdfSaveError: (String) -> Unit,
    onClearPdfMessage: () -> Unit,
    onClearInvoiceError: () -> Unit,
    onGenerateInvoice: () -> Unit,
    onAddLabour: (LabourItemRequest) -> Unit,
    onDeleteLabour: (Long) -> Unit,
    onAddPart: (PartItemRequest) -> Unit,
    onDeletePart: (Long) -> Unit,
    onAddAncillary: (AncillaryItemRequest) -> Unit,
    onDeleteAncillary: (Long) -> Unit
) {
    // Dialog states
    var showAddLabour    by remember { mutableStateOf(false) }
    var showAddPart      by remember { mutableStateOf(false) }
    var showAddAncillary by remember { mutableStateOf(false) }

    FileSaver(
        bytes    = pdfBytes,
        fileName = pdfFileName,
        onSaved  = onPdfSaved,
        onError  = onPdfSaveError
    )

    // ── Add Labour dialog ─────────────────────────────────────────────────────
    if (showAddLabour) {
        var desc by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("LABOUR") }
        var qty  by remember { mutableStateOf("1") }
        var rate by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddLabour = false },
            title = { Text("Add Labour") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Type (LABOUR / EXTERNAL)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = qty,  onValueChange = { qty  = it }, label = { Text("Qty") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = rate, onValueChange = { rate = it }, label = { Text("Rate") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val q = qty.toIntOrNull() ?: 1
                    val r = rate.toDoubleOrNull() ?: 0.0
                    if (desc.isNotBlank() && r > 0) {
                        onAddLabour(LabourItemRequest(description = desc, type = type.ifBlank { "LABOUR" }, quantity = q, rate = r))
                        showAddLabour = false
                    }
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddLabour = false }) { Text("Cancel") } }
        )
    }

    // ── Add Part dialog ───────────────────────────────────────────────────────
    if (showAddPart) {
        var partNo   by remember { mutableStateOf("") }
        var desc     by remember { mutableStateOf("") }
        var partType by remember { mutableStateOf("OEM") }
        var qty      by remember { mutableStateOf("1") }
        var price    by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddPart = false },
            title = { Text("Add Part") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = partNo,   onValueChange = { partNo   = it }, label = { Text("Part Number") },               singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = desc,     onValueChange = { desc     = it }, label = { Text("Description") },               singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = partType, onValueChange = { partType = it }, label = { Text("Type (OEM / AFTERMARKET)") },   singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = qty,      onValueChange = { qty      = it }, label = { Text("Qty") },                       singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = price,    onValueChange = { price    = it }, label = { Text("Unit Price") },                singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val q = qty.toIntOrNull() ?: 1
                    val p = price.toDoubleOrNull() ?: 0.0
                    if (desc.isNotBlank() && p > 0) {
                        onAddPart(PartItemRequest(partNumber = partNo, description = desc, partType = partType.ifBlank { "OEM" }, quantity = q, unitPrice = p))
                        showAddPart = false
                    }
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddPart = false }) { Text("Cancel") } }
        )
    }

    // ── Add Ancillary dialog ──────────────────────────────────────────────────
    if (showAddAncillary) {
        var desc   by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddAncillary = false },
            title = { Text("Add Ancillary") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = desc,   onValueChange = { desc   = it }, label = { Text("Description") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") },      singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val a = amount.toDoubleOrNull() ?: 0.0
                    if (desc.isNotBlank() && a > 0) {
                        onAddAncillary(AncillaryItemRequest(description = desc, amount = a))
                        showAddAncillary = false
                    }
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddAncillary = false }) { Text("Cancel") } }
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(jobCard?.jobCardNumber ?: "Job Card") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = onDownloadBill, enabled = !isDownloadingPdf && jobCard != null) {
                    if (isDownloadingPdf)
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else
                        Icon(Icons.Default.Download, contentDescription = "Download Bill")
                }
            }
        )

        if (isUpdating) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(error, color = MaterialTheme.colorScheme.error)
            }
            jobCard == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Status row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusChip(jobCard.status)
                    val nextStates = STATUS_TRANSITIONS[jobCard.status] ?: emptyList()
                    nextStates.forEach { next ->
                        Button(onClick = { onStatusUpdate(next) }) {
                            Text("Mark ${next.replace("_", " ")}")
                        }
                    }
                }

                // Customer & Vehicle
                DetailSection("Customer") {
                    DetailRow("Name",    jobCard.customer.name)
                    DetailRow("Phone",   jobCard.customer.phone)
                    if (jobCard.customer.email != null)   DetailRow("Email",   jobCard.customer.email)
                    if (jobCard.customer.address != null) DetailRow("Address", jobCard.customer.address)
                }
                DetailSection("Vehicle") {
                    DetailRow("Reg No",    jobCard.vehicle.regNumber)
                    DetailRow("Make/Model","${jobCard.vehicle.make} ${jobCard.vehicle.model}" +
                            (jobCard.vehicle.variant?.let { " $it" } ?: ""))
                    if (jobCard.vehicle.year != null)      DetailRow("Year",       jobCard.vehicle.year.toString())
                    if (jobCard.vehicle.chassisNo != null) DetailRow("Chassis No", jobCard.vehicle.chassisNo)
                    if (jobCard.vehicle.engineNo != null)  DetailRow("Engine No",  jobCard.vehicle.engineNo)
                    if (jobCard.vehicle.fuelType != null)  DetailRow("Fuel",       jobCard.vehicle.fuelType)
                    DetailRow("KM In",   jobCard.kmIn.toString())
                }

                // Job info
                DetailSection("Job Details") {
                    DetailRow("Service Type", jobCard.serviceType.replace("_", " "))
                    DetailRow("Date In",      jobCard.dateIn ?: "-")
                    if (jobCard.expectedDelivery != null)  DetailRow("Expected Delivery", jobCard.expectedDelivery)
                    if (jobCard.dateOut != null)           DetailRow("Date Out",           jobCard.dateOut)
                    if (jobCard.advisorName != null)       DetailRow("Advisor",            jobCard.advisorName)
                    if (jobCard.customerComplaint != null) DetailRow("Complaint",          jobCard.customerComplaint)
                    if (jobCard.technicianRemarks != null) DetailRow("Remarks",            jobCard.technicianRemarks)
                }

                // Labour items
                EditableSection(
                    title    = "Labour / Work Done",
                    onAddClick = { showAddLabour = true }
                ) {
                    if (jobCard.labourItems.isEmpty()) {
                        Text("No labour items yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    } else {
                        jobCard.labourItems.forEach { l ->
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(l.description, style = MaterialTheme.typography.bodyMedium)
                                    Text("${l.type}  ×${l.quantity} @ ₹${l.rate}", style = MaterialTheme.typography.bodySmall)
                                }
                                Text("₹${l.amount}", style = MaterialTheme.typography.bodyMedium)
                                IconButton(onClick = { onDeleteLabour(l.id) }, enabled = !isUpdating) {
                                    Icon(Icons.Default.Close, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }

                // Parts
                EditableSection(
                    title    = "Parts Used",
                    onAddClick = { showAddPart = true }
                ) {
                    if (jobCard.parts.isEmpty()) {
                        Text("No parts yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    } else {
                        jobCard.parts.forEach { p ->
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${p.partNumber}  •  ${p.description}", style = MaterialTheme.typography.bodyMedium)
                                    Text("${p.partType}  ×${p.quantity} @ ₹${p.unitPrice}", style = MaterialTheme.typography.bodySmall)
                                }
                                Text("₹${p.totalPrice}", style = MaterialTheme.typography.bodyMedium)
                                IconButton(onClick = { onDeletePart(p.id) }, enabled = !isUpdating) {
                                    Icon(Icons.Default.Close, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }

                // Ancillary
                EditableSection(
                    title    = "Ancillary Services",
                    onAddClick = { showAddAncillary = true }
                ) {
                    if (jobCard.ancillaryItems.isEmpty()) {
                        Text("No ancillary items yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    } else {
                        jobCard.ancillaryItems.forEach { a ->
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(a.description, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                Text("₹${a.amount}", style = MaterialTheme.typography.bodyMedium)
                                IconButton(onClick = { onDeleteAncillary(a.id) }, enabled = !isUpdating) {
                                    Icon(Icons.Default.Close, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                // Checks
                jobCard.checks?.let { c ->
                    DetailSection("Pre-delivery Checks") {
                        DetailRow("Fuel Level", "${c.fuelLevel}%")
                        if (c.tireFLPsi != null)    DetailRow("FL Tyre",    "${c.tireFLPsi} PSI")
                        if (c.tireFRPsi != null)    DetailRow("FR Tyre",    "${c.tireFRPsi} PSI")
                        if (c.tireRLPsi != null)    DetailRow("RL Tyre",    "${c.tireRLPsi} PSI")
                        if (c.tireRRPsi != null)    DetailRow("RR Tyre",    "${c.tireRRPsi} PSI")
                        if (c.tireSparePsi != null) DetailRow("Spare Tyre", "${c.tireSparePsi} PSI")
                        CheckDetail("Tool Kit",      c.hasToolKit)
                        CheckDetail("Stepney",       c.hasStepney)
                        CheckDetail("Brochure",      c.hasBrochure)
                        CheckDetail("Insurance",     c.hasInsurance)
                        CheckDetail("PUC",           c.hasPUC)
                        CheckDetail("RC Book",       c.hasRC)
                        if (c.notes != null) DetailRow("Notes", c.notes)
                    }
                }

                // Billing
                jobCard.billing?.let { b ->
                    DetailSection("Billing Summary") {
                        DetailRow("Labour",      "₹${b.labourTotal.fmtRs()}")
                        DetailRow("Parts",       "₹${b.partsTotal.fmtRs()}")
                        DetailRow("Ancillary",   "₹${b.ancillaryTotal.fmtRs()}")
                        DetailRow("Sub Total",   "₹${b.subTotal.fmtRs()}")
                        if (b.discount > 0)  DetailRow("Discount",  "-₹${b.discount.fmtRs()}")
                        DetailRow("Taxable",     "₹${b.taxableAmount.fmtRs()}")
                        if (b.cgstRate > 0)  DetailRow("CGST ${b.cgstRate}%",  "₹${b.cgstAmount.fmtRs()}")
                        if (b.sgstRate > 0)  DetailRow("SGST ${b.sgstRate}%",  "₹${b.sgstAmount.fmtRs()}")
                        if (b.igstRate > 0)  DetailRow("IGST ${b.igstRate}%",  "₹${b.igstAmount.fmtRs()}")
                        HorizontalDivider()
                        DetailRow("Grand Total", "₹${b.grandTotal.fmtRs()}", bold = true)
                        if (b.advanceAmount > 0) DetailRow("Advance", "₹${b.advanceAmount.fmtRs()}")
                        DetailRow("Balance Due", "₹${b.balanceDue.fmtRs()}", bold = true)
                        DetailRow("Payment", b.paymentType)
                    }
                }

                // Invoices
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Invoices", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            Button(
                                onClick = onGenerateInvoice,
                                enabled = !isGeneratingInvoice,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                if (isGeneratingInvoice) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                    Spacer(Modifier.width(6.dp))
                                }
                                Text(
                                    if (invoices.isEmpty()) "Generate Invoice" else "Update Invoice",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                        HorizontalDivider()
                        invoiceError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                        pdfSaveMessage?.let {
                            Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                            LaunchedEffect(it) {
                                kotlinx.coroutines.delay(3000)
                                onClearPdfMessage()
                            }
                        }
                        if (invoices.isEmpty()) {
                            Text("No invoices attached.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        } else {
                            invoices.forEach { inv ->
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(inv.invoiceNumber, style = MaterialTheme.typography.bodyMedium)
                                        if (inv.invoiceDate != null) Text(inv.invoiceDate, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (inv.grandTotal != null) Text("₹${inv.grandTotal}", style = MaterialTheme.typography.bodyMedium)
                                        Spacer(Modifier.width(8.dp))
                                        IconButton(
                                            onClick = { onDownloadInvoicePdf(inv.id, inv.invoiceNumber) },
                                            enabled = !isDownloadingPdf
                                        ) {
                                            if (isDownloadingPdf)
                                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                            else
                                                Icon(Icons.Default.Download, contentDescription = "Download Invoice PDF", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun EditableSection(
    title: String,
    onAddClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = onAddClick, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(18.dp))
                }
            }
            HorizontalDivider()
            content()
        }
    }
}

// ── Small helper composables ──────────────────────────────────────────────────

@Composable
private fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            HorizontalDivider()
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.weight(1f))
        Text(
            value,
            style = if (bold) MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    else MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1.5f)
        )
    }
}

@Composable
private fun CheckDetail(label: String, value: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(if (value) "✓" else "✗",
            color = if (value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}
