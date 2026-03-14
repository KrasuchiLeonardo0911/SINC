package com.sinc.mobile.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    @SerialName("token") val token: String,
    @SerialName("server_time") val serverTime: String? = null // NUEVO: Sincronización global
)
