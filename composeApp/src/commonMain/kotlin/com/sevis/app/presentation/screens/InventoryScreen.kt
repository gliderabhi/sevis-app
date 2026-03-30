package com.sevis.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sevis.app.data.model.Part
import com.sevis.app.data.model.StockItem
import com.sevis.app.presentation.util.FilePicker
import com.sevis.app.presentation.util.fmtRs
import com.sevis.app.presentation.viewmodel.PartsViewModel
import com.sevis.app.presentation.viewmodel.StockViewModel
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun InventoryScreen(
    modifier: Modifier = Modifier,
    partsViewModel: PartsViewModel = koinViewModel(),
    stockViewModel: StockViewModel = koinViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Catalogue") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("My Stock") })
        }

        val partsState by partsViewModel.state.collectAsState()

        when (selectedTab) {
            0 -> CatalogueTab(viewModel = partsViewModel)
            1 -> StockTab(viewModel = stockViewModel, catalogueParts = partsState.parts)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Tab 1 — Parts Catalogue (paginated, infinite scroll)
// ─────────────────────────────────────────────────────────────

@Composable
private fun CatalogueTab(viewModel: PartsViewModel) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems  = listState.layoutInfo.totalItemsCount
            lastVisible >= totalItems - 4 && totalItems > 0
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadNextPage()
    }

    LaunchedEffect(state.importResult, state.importError) {
        val result = state.importResult
        val error  = state.importError
        when {
            result != null -> {
                snackbarMessage = "Imported: ${result.inserted} new, ${result.updated} updated, ${result.skipped} skipped"
                delay(4000); snackbarMessage = null; viewModel.clearImportResult()
            }
            error != null -> {
                snackbarMessage = "Import failed: $error"
                delay(4000); snackbarMessage = null; viewModel.clearImportResult()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            state.error != null && state.parts.isEmpty() -> Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                Button(onClick = { viewModel.loadFirstPage() }) { Text("Retry") }
            }
            state.parts.isEmpty() -> Text(
                "No parts found",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            else -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(state.parts) { _, part -> PartCard(part) }

                if (state.isLoadingMore) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp))
                        }
                    }
                }

                if (!state.hasMore && state.parts.isNotEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                            Text("${state.parts.size} parts loaded", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        if (state.isImporting) {
            Column(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter)) {
                if (state.importTotal > 0) {
                    LinearProgressIndicator(
                        progress = { state.importProgress.toFloat() / state.importTotal },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "${state.importProgress} / ${state.importTotal} rows",
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }

        snackbarMessage?.let { message ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                action = { TextButton(onClick = { snackbarMessage = null }) { Text("Dismiss") } },
                containerColor = if (state.importError != null) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
                contentColor   = if (state.importError != null) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
            ) { Text(message) }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Tab 2 — My Stock (company-scoped)
// ─────────────────────────────────────────────────────────────

@Composable
private fun StockTab(viewModel: StockViewModel, catalogueParts: List<Part>) {
    val state by viewModel.state.collectAsState()
    var showAddDialog    by remember { mutableStateOf(false) }
    var showFilePicker   by remember { mutableStateOf(false) }
    var snackbarMessage  by remember { mutableStateOf<String?>(null) }

    if (showAddDialog) {
        AddStockDialog(
            catalogueParts = catalogueParts,
            onConfirm = { partNumber, qty, price ->
                showAddDialog = false
                viewModel.upsert(partNumber, qty, price)
            },
            onDismiss = { showAddDialog = false }
        )
    }

    FilePicker(
        show = showFilePicker,
        onFilePicked = { name, bytes ->
            showFilePicker = false
            viewModel.importXlsx(bytes, name)
        },
        onDismiss = { showFilePicker = false }
    )

    LaunchedEffect(state.importResult, state.importError) {
        val result = state.importResult
        val error  = state.importError
        when {
            result != null -> {
                snackbarMessage = "PO imported: ${result.created} created, ${result.updated} updated, ${result.skipped} skipped"
                delay(4000); snackbarMessage = null; viewModel.clearImportResult()
            }
            error != null -> {
                snackbarMessage = "Import failed: $error"
                delay(4000); snackbarMessage = null; viewModel.clearImportResult()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            state.error != null && state.items.isEmpty() -> Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                Button(onClick = { viewModel.load() }) { Text("Retry") }
            }
            state.items.isEmpty() -> Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("No stock entries yet", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = { showAddDialog = true }) { Text("Add First Part") }
                Button(onClick = { showFilePicker = true }) { Text("Import PO (.xlsx)") }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.items, key = { it.partNumber }) { item ->
                    StockCard(item = item, onDelete = { viewModel.delete(item.partNumber) }, onEdit = { qty, price ->
                        viewModel.upsert(item.partNumber, qty, price)
                    })
                }
            }
        }

        // Action buttons
        if (state.items.isNotEmpty()) {
            Row(
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { showFilePicker = true }) {
                    Text("Import PO")
                }
                Button(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Add Part")
                }
            }
        }

        if (state.isSaving || state.isImporting) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
        }

        snackbarMessage?.let { message ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 72.dp, start = 16.dp, end = 16.dp),
                action = { TextButton(onClick = { snackbarMessage = null }) { Text("Dismiss") } },
                containerColor = if (state.importError != null) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
                contentColor   = if (state.importError != null) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
            ) { Text(message) }
        }
    }
}

@Composable
private fun AddStockDialog(
    catalogueParts: List<Part>,
    onConfirm: (partNumber: String, quantity: Int, purchasePrice: Double?) -> Unit,
    onDismiss: () -> Unit
) {
    var query        by remember { mutableStateOf("") }
    var selectedPart by remember { mutableStateOf<Part?>(null) }
    var quantity     by remember { mutableStateOf("") }
    var price        by remember { mutableStateOf("") }

    val suggestions = remember(query, selectedPart) {
        if (selectedPart != null || query.length < 2) emptyList()
        else {
            val q = query.uppercase()
            catalogueParts.filter {
                it.partNumber.contains(q, ignoreCase = true) ||
                it.description.contains(q, ignoreCase = true)
            }.take(6)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add / Update Stock") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                // Part picker
                if (selectedPart == null) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Search Part Number or Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (catalogueParts.isEmpty()) {
                        Text(
                            "Parts catalogue is empty — upload a CSV first",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else if (query.length >= 2 && suggestions.isEmpty()) {
                        Text(
                            "No matching parts found",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (suggestions.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            tonalElevation = 4.dp,
                            modifier = Modifier.fillMaxWidth().heightIn(max = 220.dp)
                        ) {
                            LazyColumn {
                                items(suggestions) { part ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedPart = part; query = part.partNumber }
                                            .padding(horizontal = 12.dp, vertical = 10.dp)
                                    ) {
                                        Text(part.partNumber, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        if (part.description.isNotBlank()) {
                                            Text(part.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                }
                            }
                        }
                    }
                } else {
                    // Selected part chip with clear button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(selectedPart!!.partNumber, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                if (selectedPart!!.description.isNotBlank()) {
                                    Text(selectedPart!!.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                            IconButton(onClick = { selectedPart = null; query = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Purchase Price (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantity.toIntOrNull() ?: 0
                    onConfirm(selectedPart!!.partNumber, qty, price.toDoubleOrNull())
                },
                enabled = selectedPart != null && quantity.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun StockCard(
    item: StockItem,
    onDelete: () -> Unit,
    onEdit: (quantity: Int, purchasePrice: Double?) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        var qty   by remember { mutableStateOf(item.quantity.toString()) }
        var price by remember { mutableStateOf(item.purchasePrice?.toString() ?: "") }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Stock — ${item.partNumber}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = qty,
                        onValueChange = { qty = it },
                        label = { Text("Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Purchase Price (optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onEdit(qty.toIntOrNull() ?: item.quantity, price.toDoubleOrNull())
                    showEditDialog = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("Cancel") } }
        )
    }

    Card(
        onClick = { showEditDialog = true },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(item.partNumber, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    if (item.productGroup.isNotBlank()) {
                        Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                            Text(item.productGroup, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
                if (item.description.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LabelValue("MRP", "₹${item.mrpPrice.fmtRs()}")
                    item.purchasePrice?.let { LabelValue("Purchase", "₹${it.fmtRs()}") }
                    if (item.uom.isNotBlank()) LabelValue("UoM", item.uom)
                }
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = RoundedCornerShape(8.dp), color = if (item.quantity > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer) {
                    Text(
                        "Qty: ${item.quantity}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (item.quantity > 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Shared Part Card (Catalogue tab)
// ─────────────────────────────────────────────────────────────

@Composable
private fun PartCard(part: Part) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(part.partNumber, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                if (part.productGroup.isNotBlank()) {
                    Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                        Text(part.productGroup, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
            if (part.description.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(part.description, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PriceChip("MRP", "₹${part.mrpPrice.fmtRs()}")
                PriceChip("Purchase", "₹${part.purchasePrice.fmtRs()}")
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (part.uom.isNotBlank())          MetaTag("UoM", part.uom)
                if (part.hsnCode.isNotBlank())      MetaTag("HSN", part.hsnCode)
                if (part.taxSlab.isNotBlank())      MetaTag("Tax", part.taxSlab)
            }
        }
    }
}

@Composable
private fun LabelValue(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PriceChip(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MetaTag(label: String, value: String) {
    Row(
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(4.dp))
        Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
    }
}
