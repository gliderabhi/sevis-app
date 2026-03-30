package com.sevis.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sevis.app.data.model.CreateJobCardRequest
import com.sevis.app.data.model.InvoiceDetail
import com.sevis.app.data.model.JobCardDetail
import com.sevis.app.data.model.JobCardSummary
import com.sevis.app.data.repository.InvoiceRepository
import com.sevis.app.data.repository.JobCardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class JobCardScreen {
    object List    : JobCardScreen()
    object Create  : JobCardScreen()
    data class Detail(val jobCardId: Long) : JobCardScreen()
}

data class JobCardState(
    val jobCards: kotlin.collections.List<JobCardSummary> = emptyList(),
    val selectedJobCard: JobCardDetail? = null,
    val invoices: kotlin.collections.List<InvoiceDetail> = emptyList(),
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val isUploadingInvoice: Boolean = false,
    val error: String? = null,
    val invoiceError: String? = null,
    val screen: JobCardScreen = JobCardScreen.List
)

class JobCardViewModel(
    private val repository: JobCardRepository,
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(JobCardState())
    val state: StateFlow<JobCardState> = _state.asStateFlow()

    init {
        loadJobCards()
    }

    fun loadJobCards() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getAll()
                .onSuccess { cards -> _state.update { it.copy(isLoading = false, jobCards = cards) } }
                .onFailure { e  -> _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to load job cards") } }
        }
    }

    fun navigateToCreate() {
        _state.update { it.copy(screen = JobCardScreen.Create, error = null) }
    }

    fun navigateToDetail(id: Long) {
        _state.update { it.copy(screen = JobCardScreen.Detail(id), isLoading = true, error = null, invoices = emptyList(), invoiceError = null) }
        viewModelScope.launch {
            repository.getById(id)
                .onSuccess { detail ->
                    _state.update { it.copy(isLoading = false, selectedJobCard = detail) }
                    loadInvoices(id)
                }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to load job card") } }
        }
    }

    fun loadInvoices(jobCardId: Long) {
        viewModelScope.launch {
            invoiceRepository.getByJobCard(jobCardId)
                .onSuccess { list -> _state.update { it.copy(invoices = list, invoiceError = null) } }
                .onFailure { e   -> _state.update { it.copy(invoiceError = e.message) } }
        }
    }

    fun uploadInvoicePdf(pdfBytes: ByteArray) {
        viewModelScope.launch {
            _state.update { it.copy(isUploadingInvoice = true, invoiceError = null) }
            invoiceRepository.uploadPdf(pdfBytes)
                .onSuccess { _ -> _state.update { it.copy(isUploadingInvoice = false) }; loadJobCards() }
                .onFailure { e -> _state.update { it.copy(isUploadingInvoice = false, invoiceError = e.message ?: "Upload failed") } }
        }
    }

    fun clearInvoiceError() {
        _state.update { it.copy(invoiceError = null) }
    }

    fun navigateBack() {
        _state.update { it.copy(screen = JobCardScreen.List, error = null, selectedJobCard = null) }
    }

    fun createJobCard(request: CreateJobCardRequest) {
        viewModelScope.launch {
            _state.update { it.copy(isCreating = true, error = null) }
            repository.create(request)
                .onSuccess { created ->
                    _state.update { it.copy(isCreating = false, screen = JobCardScreen.List) }
                    loadJobCards()
                }
                .onFailure { e ->
                    _state.update { it.copy(isCreating = false, error = e.message ?: "Failed to create job card") }
                }
        }
    }

    fun updateStatus(id: Long, status: String) {
        viewModelScope.launch {
            repository.updateStatus(id, status)
                .onSuccess { updated ->
                    _state.update { it.copy(selectedJobCard = updated) }
                    loadJobCards()
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message ?: "Failed to update status") }
                }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
