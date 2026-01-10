package com.sinc.mobile.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateTicketRequest(
    @SerialName("mensaje") val mensaje: String,
    @SerialName("tipo") val tipo: String
)
