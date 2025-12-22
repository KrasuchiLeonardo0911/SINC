package com.sinc.mobile.data.model

import com.google.gson.annotations.SerializedName

data class StockResponseDto(
    @SerializedName("data") val data: StockDataDto,
    @SerializedName("message") val message: String
)
