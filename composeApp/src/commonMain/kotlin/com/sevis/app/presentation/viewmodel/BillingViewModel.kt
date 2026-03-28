package com.sevis.app.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.sevis.app.data.model.Bill
import com.sevis.app.data.repository.BillingRepository
import com.sevis.app.presentation.BaseViewModel
import kotlinx.coroutines.launch

class BillingViewModel(
    private val repository: BillingRepository
) : BaseViewModel<List<Bill>>() {

    init {
        loadBills()
    }

    fun loadBills() {
        viewModelScope.launch {
            setLoading()
            repository.getAll()
                .onSuccess { setData(it) }
                .onFailure { setError(it.message ?: "Failed to load bills") }
        }
    }

    fun loadBillsByUser(userId: Long) {
        viewModelScope.launch {
            setLoading()
            repository.getByUserId(userId)
                .onSuccess { setData(it) }
                .onFailure { setError(it.message ?: "Failed to load bills") }
        }
    }
}
