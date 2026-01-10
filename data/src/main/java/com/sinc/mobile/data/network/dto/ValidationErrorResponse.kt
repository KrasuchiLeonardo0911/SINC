package com.sinc.mobile.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ValidationErrorResponse(
    @SerialName("message") val message: String,
    @SerialName("errors") val errors: Map<String, List<String>>
)
