package com.sinc.mobile.data.network.dto

import com.google.gson.annotations.SerializedName

data class RequestPasswordResetRequest(
    @SerializedName("email")
    val email: String
)
