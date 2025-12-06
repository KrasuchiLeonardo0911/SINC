package com.sinc.mobile.data.network.dto

import com.google.gson.annotations.SerializedName

/**
 * A generic wrapper for API responses that follow the { "data": [...] } structure.
 */
data class ApiResponse<T>(
    val data: T
)

data class IdentifierConfigDto(
    @SerializedName("type") val type: String,
    @SerializedName("label") val label: String,
    @SerializedName("hint") val hint: String?,
    @SerializedName("regex") val regex: String
)
