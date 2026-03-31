package com.sevis.app.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sevis.app.data.model.AuditSummary
import com.sevis.app.presentation.util.fmtRs
import com.sevis.app.presentation.viewmodel.AuditViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditScreen(
    modifier: Modifier = Modifier,
    viewModel: AuditViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.load() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.error != null -> Column(
                    Modifier.align(Alignment.Center).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.load() }) { Text("Retry") }
                }
                else -> AuditContent(
                    summary    = state.summary,
                    stockValue = state.stockValue
                )
            }
        }
    }
}

@Composable
private fun AuditContent(summary: AuditSummary?, stockValue: Double?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Hero revenue card ─────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Total Revenue (Delivered + Closed)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(
                    "₹${(summary?.totalRevenue ?: 0.0).fmtRs()}",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (stockValue != null) {
                    Spacer(Modifier.height(4.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f))
                    Spacer(Modifier.height(4.dp))
                    Text("Stock Value on Hand", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        "₹${stockValue.fmtRs()}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // ── This month vs last month ──────────────────────────────────────────
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                modifier   = Modifier.weight(1f),
                title      = "This Month",
                value      = "₹${(summary?.revenueThisMonth ?: 0.0).fmtRs()}",
                subtitle   = "${summary?.invoicesThisMonth ?: 0} invoices",
                color      = MaterialTheme.colorScheme.secondaryContainer
            )
            MetricCard(
                modifier   = Modifier.weight(1f),
                title      = "Last Month",
                value      = "₹${(summary?.revenuePreviousMonth ?: 0.0).fmtRs()}",
                subtitle   = "${summary?.invoicesPreviousMonth ?: 0} invoices",
                color      = MaterialTheme.colorScheme.tertiaryContainer
            )
        }

        // ── Revenue breakdown ─────────────────────────────────────────────────
        AuditSection("Revenue Breakdown") {
            AuditRow("Labour Charges",   "₹${(summary?.labourChargesTotal  ?: 0.0).fmtRs()}")
            AuditRow("Parts Revenue",    "₹${(summary?.partsRevenueTotal   ?: 0.0).fmtRs()}")
            AuditRow("Ancillary Revenue","₹${(summary?.ancillaryRevenueTotal ?: 0.0).fmtRs()}")
            HorizontalDivider()
            AuditRow("Avg Invoice Value","₹${(summary?.averageInvoiceValue ?: 0.0).fmtRs()}", bold = true)
            AuditRow("Total Invoices",   "${summary?.totalInvoices ?: 0}", bold = true)
        }

        // ── Job card status counts ────────────────────────────────────────────
        AuditSection("Job Cards by Status") {
            AuditRow("Open",         "${summary?.openJobCards ?: 0}",        color = MaterialTheme.colorScheme.primary)
            AuditRow("In Progress",  "${summary?.inProgressJobCards ?: 0}",  color = MaterialTheme.colorScheme.tertiary)
            AuditRow("Ready",        "${summary?.readyJobCards ?: 0}",        color = MaterialTheme.colorScheme.secondary)
            AuditRow("Delivered",    "${summary?.deliveredJobCards ?: 0}")
            AuditRow("Closed",       "${summary?.closedJobCards ?: 0}")
            HorizontalDivider()
            AuditRow("Total",        "${summary?.totalJobCards ?: 0}", bold = true)
        }

        Spacer(Modifier.height(72.dp)) // FAB clearance
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier,
    title: String,
    value: String,
    subtitle: String,
    color: androidx.compose.ui.graphics.Color
) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title,    style = MaterialTheme.typography.labelSmall)
            Text(value,    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AuditSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            HorizontalDivider()
            content()
        }
    }
}

@Composable
private fun AuditRow(
    label: String,
    value: String,
    bold: Boolean = false,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(
            value,
            style = if (bold) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium,
            color = color
        )
    }
}
