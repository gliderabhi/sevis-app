package com.sevis.app.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UiState<T>(
    val isLoading: Boolean = false,
    val data: T? = null,
    val error: String? = null
)

abstract class BaseViewModel<T> : ViewModel() {
    protected val _uiState = MutableStateFlow(UiState<T>())
    val uiState: StateFlow<UiState<T>> = _uiState.asStateFlow()

    protected fun setLoading() {
        _uiState.value = UiState(isLoading = true)
    }

    protected fun setData(data: T) {
        _uiState.value = UiState(data = data)
    }

    protected fun setError(message: String) {
        _uiState.value = UiState(error = message)
    }
}
