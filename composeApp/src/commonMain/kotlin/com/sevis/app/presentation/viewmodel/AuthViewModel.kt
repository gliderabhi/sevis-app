package com.sevis.app.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.sevis.app.data.model.SignupRequest
import com.sevis.app.data.repository.AuthRepository
import com.sevis.app.presentation.BaseViewModel
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : BaseViewModel<Unit>() {

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            setLoading()
            repository.login(email, password)
                .onSuccess {
                    setData(Unit)
                    onSuccess()
                }
                .onFailure { setError(it.message ?: "Login failed") }
        }
    }

    fun signup(
        name: String,
        email: String,
        phone: String,
        password: String,
        role: String,
        accountType: String,
        companyName: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            setLoading()
            repository.signup(SignupRequest(name, email, phone, password, role, accountType, companyName))
                .onSuccess {
                    setData(Unit)
                    onSuccess()
                }
                .onFailure { setError(it.message ?: "Signup failed") }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            onComplete()
        }
    }

    fun clearError() {
        setData(Unit)
    }
}
