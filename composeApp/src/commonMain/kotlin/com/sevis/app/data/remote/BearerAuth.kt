package com.sevis.app.data.remote

import com.sevis.app.data.auth.TokenManager
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders

internal fun HttpRequestBuilder.bearerAuth() {
    TokenManager.token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
}
