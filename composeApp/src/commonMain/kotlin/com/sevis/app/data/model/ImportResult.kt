package com.sevis.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ImportResult(
    val inserted: Int = 0,
    val updated: Int = 0,
    val skipped: Int = 0,
    val message: String = ""
)
