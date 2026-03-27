package com.sevis.app.data.auth

object TokenManager {
    private var _token: String? = null

    val token: String? get() = _token
    val isLoggedIn: Boolean get() = !_token.isNullOrBlank()

    fun save(token: String) {
        _token = token
    }

    fun clear() {
        _token = null
    }
}
