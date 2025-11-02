package com.sinc.mobile.data.network.dto

import com.google.gson.annotations.SerializedName

data class MovimientoRequest(
    @SerializedName("upId") val upId: Int,
    val movimientos: List<MovimientoItemRequest>
)

data class MovimientoItemRequest(
    @SerializedName("especie_id") val especieId: Int,
    @SerializedName("categoria_id") val categoriaId: Int,
    @SerializedName("raza_id") val razaId: Int,
    val cantidad: Int,
    @SerializedName("motivo_movimiento_id") val motivoMovimientoId: Int,
    @SerializedName("destino_traslado") val destinoTraslado: String? = null
)
