package com.sevis.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sevis.app.data.model.ImportResult
import com.sevis.app.data.model.Part
import com.sevis.app.data.model.PartBatchRow
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
    val importProgress: Int = 0,
    val importTotal: Int = 0,
    val importResult: ImportResult? = null,
    val importError: String? = null
)

class PartsViewModel(
    private val repository: PartRepository
) : ViewModel() {

    companion object {
        private const val PAGE_SIZE = 20
        private const val UPLOAD_BATCH = 150
    }

    private val _state = MutableStateFlow(PartsState())
    val state: StateFlow<PartsState> = _state.asStateFlow()

    init {
        loadFirstPage()
    }

    fun loadFirstPage() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true, error = null,
                parts = emptyList(), hasMore = true, currentPage = 0
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
                    _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Failed to load parts")
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
                    _state.value = current.copy(isLoadingMore = false, error = e.message ?: "Failed to load more parts")
                }
        }
    }

    fun importCsv(bytes: ByteArray, filename: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isImporting = true, importResult = null, importError = null,
                importProgress = 0, importTotal = 0
            )

            val rows = parseRows(bytes)
            if (rows.isEmpty()) {
                _state.value = _state.value.copy(isImporting = false, importError = "No valid rows found in file")
                return@launch
            }

            _state.value = _state.value.copy(importTotal = rows.size)

            var totalInserted = 0
            var totalUpdated  = 0
            var totalSkipped  = 0

            try {
                for (batch in rows.chunked(UPLOAD_BATCH)) {
                    repository.importBatch(batch)
                        .onSuccess { result ->
                            totalInserted += result.inserted
                            totalUpdated  += result.updated
                            totalSkipped  += result.skipped
                            _state.value = _state.value.copy(
                                importProgress = totalInserted + totalUpdated + totalSkipped
                            )
                        }
                        .onFailure { throw it }
                }
                _state.value = _state.value.copy(
                    isImporting = false,
                    importResult = ImportResult(totalInserted, totalUpdated, totalSkipped, "Import complete")
                )
                loadFirstPage()
            } catch (e: Exception) {
                _state.value = _state.value.copy(isImporting = false, importError = e.message ?: "Import failed")
            }
        }
    }

    fun clearImportResult() {
        _state.value = _state.value.copy(importResult = null, importError = null, importProgress = 0, importTotal = 0)
    }

    // ── CSV / TSV parser ──────────────────────────────────────────────────────

    private fun parseRows(bytes: ByteArray): List<PartBatchRow> {
        val content = decodeBytes(bytes)
        val lines = content.lines()
        if (lines.size < 2) return emptyList()

        // Auto-detect separator: prefer tab, fall back to comma
        val sep = if (lines[0].contains('\t')) '\t' else ','

        return lines.drop(1)
            .filter { it.isNotBlank() }
            .mapNotNull { parseLine(it, sep) }
    }

    private fun decodeBytes(bytes: ByteArray): String = when {
        // UTF-16 LE BOM: FF FE
        bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte() -> {
            val data = bytes.copyOfRange(2, bytes.size)
            val chars = CharArray(data.size / 2) { i ->
                ((data[i * 2].toInt() and 0xFF) or ((data[i * 2 + 1].toInt() and 0xFF) shl 8)).toChar()
            }
            chars.concatToString()
        }
        // UTF-16 BE BOM: FE FF
        bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte() -> {
            val data = bytes.copyOfRange(2, bytes.size)
            val chars = CharArray(data.size / 2) { i ->
                (((data[i * 2].toInt() and 0xFF) shl 8) or (data[i * 2 + 1].toInt() and 0xFF)).toChar()
            }
            chars.concatToString()
        }
        // UTF-8 BOM: EF BB BF
        bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte() ->
            bytes.copyOfRange(3, bytes.size).decodeToString()
        // Default UTF-8
        else -> bytes.decodeToString()
    }

    private fun parseLine(line: String, sep: Char): PartBatchRow? {
        val cols = line.split(sep)
        if (cols.size < 2) return null

        val partNumber  = cols[0].clean()
        val description = cols[1].clean()
        if (partNumber.isBlank() || description.isBlank()) return null
        if (partNumber.any { it.code < 32 }) return null   // reject control chars

        return PartBatchRow(
            partNumber    = partNumber,
            description   = description,
            mrpPrice      = cols.getOrNull(2)?.parsePrice() ?: 0.0,
            purchasePrice = cols.getOrNull(3)?.parsePrice() ?: 0.0,
            uom           = cols.getOrNull(6)?.clean() ?: "",
            productGroup  = cols.getOrNull(7)?.clean() ?: "",
            hsnCode       = cols.getOrNull(8)?.clean() ?: "",
            taxSlab       = cols.getOrNull(9)?.clean() ?: ""
        )
    }

    private fun String.clean() = trim().removePrefix("\"").removeSuffix("\"").trim()
    private fun String.parsePrice() = clean().replace("Rs.", "").replace(",", "").trim().toDoubleOrNull() ?: 0.0
}
