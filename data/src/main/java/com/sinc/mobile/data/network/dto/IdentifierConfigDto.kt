package com.sinc.mobile.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A generic wrapper for API responses that follow the { "data": [...] } structure.
 */
@Serializable
data class ApiResponse<T>(
    val data: T
)

@Serializable
data class IdentifierConfigDto(
    @SerialName("type") val type: String,
    @SerialName("label") val label: String,
    @SerialName("hint") val hint: String? = null,
    @SerialName("regex") val regex: String
)
