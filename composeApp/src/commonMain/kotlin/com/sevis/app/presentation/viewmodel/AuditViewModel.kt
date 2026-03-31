package com.sevis.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sevis.app.data.model.AuditSummary
import com.sevis.app.data.repository.AuditRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuditState(
    val summary: AuditSummary? = null,
    val stockValue: Double? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuditViewModel(private val repository: AuditRepository) : ViewModel() {

    private val _state = MutableStateFlow(AuditState())
    val state: StateFlow<AuditState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val summaryDeferred    = async { repository.getSummary() }
            val stockValDeferred   = async { repository.getStockValue() }
            val summaryResult      = summaryDeferred.await()
            val stockValResult     = stockValDeferred.await()
            _state.update { it.copy(
                isLoading  = false,
                summary    = summaryResult.getOrNull(),
                stockValue = stockValResult.getOrNull(),
                error      = summaryResult.exceptionOrNull()?.message
            )}
        }
    }
}
