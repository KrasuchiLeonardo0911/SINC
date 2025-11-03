package com.sinc.mobile.data.network.dto

import com.google.gson.annotations.SerializedName

data class MovimientosBatchRequest(
    @SerializedName("upId") val upId: Int,
    @SerializedName("movimientos") val movimientos: List<MovimientoRequest>
)
