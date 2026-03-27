package com.sevis.app.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sevis.app.presentation.viewmodel.InventoryViewModel

@Composable
fun InventoryScreen(
    modifier: Modifier = Modifier,
    viewModel: InventoryViewModel = viewModel { InventoryViewModel() }
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            state.isLoading -> CircularProgressIndicator()
            state.error != null -> Text(
                text = state.error!!,
                color = MaterialTheme.colorScheme.error
            )
            state.data.isNullOrEmpty() -> Text("No inventory items found")
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.data!!) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(item.name, style = MaterialTheme.typography.titleMedium)
                                Text("SKU: ${item.sku}", style = MaterialTheme.typography.bodySmall)
                                Text("$${item.price}", style = MaterialTheme.typography.bodyMedium)
                            }
                            Badge(containerColor = if (item.quantity > 0)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error
                            ) {
                                Text("Qty: ${item.quantity}")
                            }
                        }
                    }
                }
            }
        }
    }
}
