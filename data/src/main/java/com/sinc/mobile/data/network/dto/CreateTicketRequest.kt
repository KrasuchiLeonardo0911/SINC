package com.sinc.mobile.data.network.dto

import com.google.gson.annotations.SerializedName

data class CreateTicketRequest(
    @SerializedName("mensaje") val mensaje: String,
    @SerializedName("tipo") val tipo: String
)
