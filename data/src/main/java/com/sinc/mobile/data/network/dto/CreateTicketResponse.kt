package com.sinc.mobile.data.network.dto

import com.google.gson.annotations.SerializedName

data class CreateTicketResponse(
    @SerializedName("message") val message: String,
    @SerializedName("ticket_id") val ticketId: Int
)
