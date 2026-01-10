package com.sinc.mobile.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateTicketResponse(
    @SerialName("message") val message: String,
    @SerialName("ticket_id") val ticketId: Int
)
