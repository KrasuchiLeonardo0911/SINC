package com.sinc.mobile.data.network.dto

import com.google.gson.annotations.SerializedName

data class ValidationErrorResponse(
    @SerializedName("message") val message: String,
    @SerializedName("errors") val errors: Map<String, List<String>>
)
