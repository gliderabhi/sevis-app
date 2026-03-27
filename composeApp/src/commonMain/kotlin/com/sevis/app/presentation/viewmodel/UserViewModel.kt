package com.sevis.app.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.sevis.app.data.model.User
import com.sevis.app.data.repository.UserRepository
import com.sevis.app.presentation.BaseViewModel
import kotlinx.coroutines.launch

class UserViewModel(
    private val repository: UserRepository = UserRepository()
) : BaseViewModel<List<User>>() {

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            setLoading()
            repository.getAll()
                .onSuccess { setData(it) }
                .onFailure { setError(it.message ?: "Failed to load users") }
        }
    }

    fun deleteUser(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
                .onSuccess { loadUsers() }
                .onFailure { setError(it.message ?: "Failed to delete user") }
        }
    }
}
