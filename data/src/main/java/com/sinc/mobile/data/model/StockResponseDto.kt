package com.sinc.mobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StockResponseDto(
    @SerialName("data") val data: StockDataDto,
    @SerialName("message") val message: String
)
