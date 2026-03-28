package com.sevis.app.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.sevis.app.data.model.InventoryItem
import com.sevis.app.data.repository.InventoryRepository
import com.sevis.app.presentation.BaseViewModel
import kotlinx.coroutines.launch

class InventoryViewModel(
    private val repository: InventoryRepository
) : BaseViewModel<List<InventoryItem>>() {

    init {
        loadItems()
    }

    fun loadItems() {
        viewModelScope.launch {
            setLoading()
            repository.getAll()
                .onSuccess { setData(it) }
                .onFailure { setError(it.message ?: "Failed to load inventory") }
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
                .onSuccess { loadItems() }
                .onFailure { setError(it.message ?: "Failed to delete item") }
        }
    }
}
