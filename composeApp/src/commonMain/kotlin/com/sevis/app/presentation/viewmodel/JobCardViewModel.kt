package com.sevis.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sevis.app.data.model.AncillaryItemRequest
import com.sevis.app.data.model.CreateJobCardRequest
import com.sevis.app.data.model.InvoiceDetail
import com.sevis.app.data.model.JobCardDetail
import com.sevis.app.data.model.JobCardSummary
import com.sevis.app.data.model.LabourItemRequest
import com.sevis.app.data.model.Part
import com.sevis.app.data.model.PartItemRequest
import com.sevis.app.data.repository.InvoiceRepository
import com.sevis.app.data.repository.JobCardRepository
import com.sevis.app.data.repository.PartRepository
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
    val isGeneratingInvoice: Boolean = false,
    val isUpdating: Boolean = false,
    val isSearchingParts: Boolean = false,
    val partSearchResults: List<Part> = emptyList(),
    val isDownloadingPdf: Boolean = false,
    val pdfBytes: ByteArray? = null,
    val pdfFileName: String = "",
    val pdfSaveMessage: String? = null,
    val error: String? = null,
    val invoiceError: String? = null,
    val screen: JobCardScreen = JobCardScreen.List
)

class JobCardViewModel(
    private val repository: JobCardRepository,
    private val invoiceRepository: InvoiceRepository,
    private val partRepository: PartRepository
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

    fun generateInvoice(jobCardId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isGeneratingInvoice = true, invoiceError = null) }
            invoiceRepository.generateInvoice(jobCardId)
                .onSuccess { inv ->
                    _state.update { it.copy(isGeneratingInvoice = false) }
                    loadInvoices(jobCardId)
                }
                .onFailure { e ->
                    _state.update { it.copy(isGeneratingInvoice = false, invoiceError = e.message ?: "Failed to generate invoice") }
                }
        }
    }

    fun searchParts(query: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSearchingParts = true) }
            partRepository.search(query)
                .onSuccess { results -> _state.update { it.copy(isSearchingParts = false, partSearchResults = results) } }
                .onFailure { _state.update { it.copy(isSearchingParts = false, partSearchResults = emptyList()) } }
        }
    }

    fun clearPartSearch() {
        _state.update { it.copy(partSearchResults = emptyList(), isSearchingParts = false) }
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

    // ── Download bill from job card ───────────────────────────────────────────

    fun downloadJobCardPdf(jobCardId: Long, jobCardNumber: String) {
        viewModelScope.launch {
            _state.update { it.copy(isDownloadingPdf = true) }
            repository.downloadPdf(jobCardId)
                .onSuccess { bytes -> _state.update { it.copy(isDownloadingPdf = false, pdfBytes = bytes, pdfFileName = "bill-$jobCardNumber.pdf") } }
                .onFailure { e  -> _state.update { it.copy(isDownloadingPdf = false, error = e.message ?: "Download failed") } }
        }
    }

    // ── Edit: Labour ──────────────────────────────────────────────────────────

    fun addLabour(jobCardId: Long, req: LabourItemRequest) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            repository.addLabour(jobCardId, req)
                .onSuccess { updated -> _state.update { it.copy(isUpdating = false, selectedJobCard = updated) } }
                .onFailure { e -> _state.update { it.copy(isUpdating = false, error = e.message ?: "Failed to add labour") } }
        }
    }

    fun deleteLabour(jobCardId: Long, labourId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            repository.deleteLabour(jobCardId, labourId)
                .onSuccess { updated -> _state.update { it.copy(isUpdating = false, selectedJobCard = updated) } }
                .onFailure { e -> _state.update { it.copy(isUpdating = false, error = e.message ?: "Failed to delete labour") } }
        }
    }

    // ── Edit: Parts ───────────────────────────────────────────────────────────

    fun addPart(jobCardId: Long, req: PartItemRequest) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            repository.addPart(jobCardId, req)
                .onSuccess { updated -> _state.update { it.copy(isUpdating = false, selectedJobCard = updated) } }
                .onFailure { e -> _state.update { it.copy(isUpdating = false, error = e.message ?: "Failed to add part") } }
        }
    }

    fun deletePart(jobCardId: Long, partId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            repository.deletePart(jobCardId, partId)
                .onSuccess { updated -> _state.update { it.copy(isUpdating = false, selectedJobCard = updated) } }
                .onFailure { e -> _state.update { it.copy(isUpdating = false, error = e.message ?: "Failed to delete part") } }
        }
    }

    // ── Edit: Ancillary ───────────────────────────────────────────────────────

    fun addAncillary(jobCardId: Long, req: AncillaryItemRequest) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            repository.addAncillary(jobCardId, req)
                .onSuccess { updated -> _state.update { it.copy(isUpdating = false, selectedJobCard = updated) } }
                .onFailure { e -> _state.update { it.copy(isUpdating = false, error = e.message ?: "Failed to add item") } }
        }
    }

    fun deleteAncillary(jobCardId: Long, ancId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true) }
            repository.deleteAncillary(jobCardId, ancId)
                .onSuccess { updated -> _state.update { it.copy(isUpdating = false, selectedJobCard = updated) } }
                .onFailure { e -> _state.update { it.copy(isUpdating = false, error = e.message ?: "Failed to delete item") } }
        }
    }

    // ── Download invoice PDF ──────────────────────────────────────────────────

    fun downloadInvoicePdf(invoiceId: Long, invoiceNumber: String) {
        viewModelScope.launch {
            _state.update { it.copy(isDownloadingPdf = true) }
            invoiceRepository.downloadPdf(invoiceId)
                .onSuccess { bytes ->
                    _state.update { it.copy(
                        isDownloadingPdf = false,
                        pdfBytes  = bytes,
                        pdfFileName = "invoice-$invoiceNumber.pdf"
                    )}
                }
                .onFailure { e ->
                    _state.update { it.copy(
                        isDownloadingPdf = false,
                        invoiceError = e.message ?: "Failed to download PDF"
                    )}
                }
        }
    }

    fun onPdfSaved(fileName: String) {
        _state.update { it.copy(pdfBytes = null, pdfFileName = "", pdfSaveMessage = "Saved: $fileName") }
    }

    fun onPdfSaveError(msg: String) {
        _state.update { it.copy(pdfBytes = null, pdfFileName = "", invoiceError = msg) }
    }

    fun clearPdfMessage() {
        _state.update { it.copy(pdfSaveMessage = null) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
