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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sevis.app.data.model.JobCardDetail
import com.sevis.app.data.model.JobCardSummary
import com.sevis.app.presentation.viewmodel.JobCardScreen
import com.sevis.app.presentation.viewmodel.JobCardViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OrdersScreen(
    modifier: Modifier = Modifier,
    viewModel: JobCardViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    when (val screen = state.screen) {
        is JobCardScreen.List -> JobCardListContent(
            modifier    = modifier,
            jobCards    = state.jobCards,
            isLoading   = state.isLoading,
            error       = state.error,
            onCreateClick   = { viewModel.navigateToCreate() },
            onJobCardClick  = { id -> viewModel.navigateToDetail(id) },
            onRefresh       = { viewModel.loadJobCards() },
            onClearError    = { viewModel.clearError() }
        )
        is JobCardScreen.Create -> CreateJobCardScreen(
            modifier   = modifier,
            isCreating = state.isCreating,
            error      = state.error,
            onSubmit   = { request -> viewModel.createJobCard(request) },
            onBack     = { viewModel.navigateBack() }
        )
        is JobCardScreen.Detail -> JobCardDetailContent(
            modifier    = modifier,
            jobCard     = state.selectedJobCard,
            isLoading   = state.isLoading,
            error       = state.error,
            onBack      = { viewModel.navigateBack() },
            onStatusUpdate = { status -> viewModel.updateStatus(screen.jobCardId, status) }
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
    error: String?,
    onCreateClick: () -> Unit,
    onJobCardClick: (Long) -> Unit,
    onRefresh: () -> Unit,
    onClearError: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateClick) {
                Icon(Icons.Default.Add, contentDescription = "New Job Card")
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
                    Text("₹${"%,.0f".format(jc.grandTotal)}", style = MaterialTheme.typography.titleSmall)
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
    isLoading: Boolean,
    error: String?,
    onBack: () -> Unit,
    onStatusUpdate: (String) -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(jobCard?.jobCardNumber ?: "Job Card") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

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
                if (jobCard.labourItems.isNotEmpty()) {
                    DetailSection("Labour / Work Done") {
                        jobCard.labourItems.forEach { l ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(l.description, style = MaterialTheme.typography.bodyMedium)
                                    Text("${l.type}  ×${l.quantity} @ ₹${l.rate}", style = MaterialTheme.typography.bodySmall)
                                }
                                Text("₹${l.amount}", style = MaterialTheme.typography.bodyMedium)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }

                // Parts
                if (jobCard.parts.isNotEmpty()) {
                    DetailSection("Parts Used") {
                        jobCard.parts.forEach { p ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${p.partNumber}  •  ${p.description}", style = MaterialTheme.typography.bodyMedium)
                                    Text("${p.partType}  ×${p.quantity} @ ₹${p.unitPrice}", style = MaterialTheme.typography.bodySmall)
                                }
                                Text("₹${p.totalPrice}", style = MaterialTheme.typography.bodyMedium)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }

                // Ancillary
                if (jobCard.ancillaryItems.isNotEmpty()) {
                    DetailSection("Ancillary Services") {
                        jobCard.ancillaryItems.forEach { a ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(a.description, style = MaterialTheme.typography.bodyMedium)
                                Text("₹${a.amount}", style = MaterialTheme.typography.bodyMedium)
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
                        DetailRow("Labour",      "₹${"%,.2f".format(b.labourTotal)}")
                        DetailRow("Parts",       "₹${"%,.2f".format(b.partsTotal)}")
                        DetailRow("Ancillary",   "₹${"%,.2f".format(b.ancillaryTotal)}")
                        DetailRow("Sub Total",   "₹${"%,.2f".format(b.subTotal)}")
                        if (b.discount > 0)  DetailRow("Discount",  "-₹${"%,.2f".format(b.discount)}")
                        DetailRow("Taxable",     "₹${"%,.2f".format(b.taxableAmount)}")
                        if (b.cgstRate > 0)  DetailRow("CGST ${b.cgstRate}%",  "₹${"%,.2f".format(b.cgstAmount)}")
                        if (b.sgstRate > 0)  DetailRow("SGST ${b.sgstRate}%",  "₹${"%,.2f".format(b.sgstAmount)}")
                        if (b.igstRate > 0)  DetailRow("IGST ${b.igstRate}%",  "₹${"%,.2f".format(b.igstAmount)}")
                        HorizontalDivider()
                        DetailRow("Grand Total", "₹${"%,.2f".format(b.grandTotal)}", bold = true)
                        if (b.advanceAmount > 0) DetailRow("Advance", "₹${"%,.2f".format(b.advanceAmount)}")
                        DetailRow("Balance Due", "₹${"%,.2f".format(b.balanceDue)}", bold = true)
                        DetailRow("Payment", b.paymentType)
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
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
