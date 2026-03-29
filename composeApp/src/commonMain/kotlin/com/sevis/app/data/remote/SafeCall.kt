package com.sevis.app.data.remote

import com.sevis.app.data.model.ApiError
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText

suspend inline fun <reified T> safeCall(response: HttpResponse): T {
    return if (response.status.value in 200..299) {
        response.body()
    } else {
        val error = runCatching { response.body<ApiError>() }.getOrNull()
        val message = error?.error ?: response.bodyAsText()
        throw Exception("API ${response.status.value}: $message")
    }
}