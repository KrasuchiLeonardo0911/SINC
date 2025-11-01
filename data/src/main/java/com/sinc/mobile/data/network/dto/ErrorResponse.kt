package com.sinc.mobile.data.network.dto

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("message") val message: String
)
