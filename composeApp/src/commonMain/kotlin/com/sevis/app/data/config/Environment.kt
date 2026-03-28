package com.sevis.app.data.config

object Environment {

    enum class Env(val label: String, val baseUrl: String) {
        LOCAL("Local",  "http://localhost:8080"),
        PROD ("Prod",   "http://32.194.147.195:8761")
    }

    var current: Env = Env.LOCAL
        private set

    val baseUrl: String get() = current.baseUrl

    fun set(env: Env) { current = env }
}
