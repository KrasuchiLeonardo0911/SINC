package com.sinc.mobile.data.model

import com.google.gson.annotations.SerializedName

data class MovimientoHistorialDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("fecha_registro")
    val fechaRegistro: String,
    @SerializedName("cantidad")
    val cantidad: Int,
    @SerializedName("especie")
    val especie: String,
    @SerializedName("categoria")
    val categoria: String,
    @SerializedName("raza")
    val raza: String,
    @SerializedName("motivo")
    val motivo: String,
    @SerializedName("tipo_movimiento")
    val tipoMovimiento: String,
    @SerializedName("unidad_productiva")
    val unidadProductiva: String,
    @SerializedName("destino_traslado")
    val destinoTraslado: String?
)
