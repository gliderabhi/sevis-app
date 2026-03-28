package com.sevis.app.data.auth

object TokenManager {
    private var _token: String? = null
    var userId: Long = 0L
        private set
    var role: String = ""
        private set
    var accountType: String = ""
        private set
    var userName: String = ""
        private set

    val token: String? get() = _token
    val isLoggedIn: Boolean get() = !_token.isNullOrBlank()
    val isAdmin: Boolean get() = role == "ADMIN"

    fun save(token: String, userId: Long, role: String, accountType: String, name: String) {
        _token       = token
        this.userId  = userId
        this.role    = role
        this.accountType = accountType
        this.userName = name
    }

    fun clear() {
        _token      = null
        userId      = 0L
        role        = ""
        accountType = ""
        userName    = ""
    }
}
