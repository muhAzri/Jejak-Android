package com.muhazri.jejak.core.network.models

import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorDto(
    val message: String? = null,
    val error: String? = null,
)
