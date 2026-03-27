package com.sevis.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long = 0,
    val name: String = "",
    val email: String = "",
    val phone: String = ""
)
