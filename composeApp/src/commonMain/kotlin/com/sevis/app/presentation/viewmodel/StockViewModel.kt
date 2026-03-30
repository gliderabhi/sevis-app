package com.sevis.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sevis.app.data.model.StockImportResult
import com.sevis.app.data.model.StockItem
import com.sevis.app.data.model.StockRequest
import com.sevis.app.data.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StockState(
    val items: List<StockItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val lastSaved: StockItem? = null,
    val isImporting: Boolean = false,
    val importResult: StockImportResult? = null,
    val importError: String? = null
)

class StockViewModel(
    private val repository: StockRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StockState())
    val state: StateFlow<StockState> = _state.asStateFlow()

    init {
        load()
    }

    fun load(companyId: Long? = null) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            repository.getAll(companyId)
                .onSuccess { _state.value = _state.value.copy(isLoading = false, items = it) }
                .onFailure { _state.value = _state.value.copy(isLoading = false, error = it.message) }
        }
    }

    fun upsert(partNumber: String, quantity: Int, purchasePrice: Double?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, saveError = null, lastSaved = null)
            repository.upsert(StockRequest(partNumber, quantity, purchasePrice))
                .onSuccess { saved ->
                    val updated = _state.value.items.map { if (it.partNumber == saved.partNumber) saved else it }
                    val newList = if (_state.value.items.any { it.partNumber == saved.partNumber }) updated
                                  else _state.value.items + saved
                    _state.value = _state.value.copy(isSaving = false, items = newList, lastSaved = saved)
                }
                .onFailure { _state.value = _state.value.copy(isSaving = false, saveError = it.message) }
        }
    }

    fun delete(partNumber: String) {
        viewModelScope.launch {
            repository.delete(partNumber)
                .onSuccess {
                    _state.value = _state.value.copy(
                        items = _state.value.items.filter { it.partNumber != partNumber }
                    )
                }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }

    fun clearSaveResult() {
        _state.value = _state.value.copy(saveError = null, lastSaved = null)
    }

    fun importXlsx(bytes: ByteArray, filename: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isImporting = true, importResult = null, importError = null)
            repository.importXlsx(bytes, filename)
                .onSuccess { result ->
                    _state.value = _state.value.copy(isImporting = false, importResult = result)
                    load()
                }
                .onFailure { _state.value = _state.value.copy(isImporting = false, importError = it.message) }
        }
    }

    fun clearImportResult() {
        _state.value = _state.value.copy(importResult = null, importError = null)
    }
}
