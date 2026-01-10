package com.sinc.mobile.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestPasswordResetRequest(
    @SerialName("email")
    val email: String
)
