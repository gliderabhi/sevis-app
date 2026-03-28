package com.sevis.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sevis.app.data.model.ImportResult
import com.sevis.app.data.model.Part
import com.sevis.app.data.repository.PartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PartsState(
    val parts: List<Part> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true,
    val currentPage: Int = 0,
    val isImporting: Boolean = false,
    val importResult: ImportResult? = null,
    val importError: String? = null
)

class PartsViewModel(
    private val repository: PartRepository
) : ViewModel() {

    companion object {
        private const val PAGE_SIZE = 20
    }

    private val _state = MutableStateFlow(PartsState())
    val state: StateFlow<PartsState> = _state.asStateFlow()

    init {
        loadFirstPage()
    }

    fun loadFirstPage() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null,
                parts = emptyList(),
                hasMore = true,
                currentPage = 0
            )
            repository.getParts(0, PAGE_SIZE)
                .onSuccess { response ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        parts = response.content,
                        hasMore = !response.last,
                        currentPage = 0
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load parts"
                    )
                }
        }
    }

    fun loadNextPage() {
        val current = _state.value
        if (current.isLoadingMore || !current.hasMore) return

        val nextPage = current.currentPage + 1
        viewModelScope.launch {
            _state.value = current.copy(isLoadingMore = true)
            repository.getParts(nextPage, PAGE_SIZE)
                .onSuccess { response ->
                    _state.value = current.copy(
                        parts = current.parts + response.content,
                        isLoadingMore = false,
                        hasMore = !response.last,
                        currentPage = nextPage
                    )
                }
                .onFailure { e ->
                    _state.value = current.copy(
                        isLoadingMore = false,
                        error = e.message ?: "Failed to load more parts"
                    )
                }
        }
    }

    fun importCsv(bytes: ByteArray, filename: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isImporting = true, importResult = null, importError = null)
            repository.importCsv(bytes, filename)
                .onSuccess { result ->
                    _state.value = _state.value.copy(isImporting = false, importResult = result)
                    loadFirstPage()
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        isImporting = false,
                        importError = e.message ?: "Import failed"
                    )
                }
        }
    }

    fun clearImportResult() {
        _state.value = _state.value.copy(importResult = null, importError = null)
    }
}
