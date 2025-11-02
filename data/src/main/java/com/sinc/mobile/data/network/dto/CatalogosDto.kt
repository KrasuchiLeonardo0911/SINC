package com.sinc.mobile.data.network.dto

import com.google.gson.annotations.SerializedName

data class CatalogosDto(
    val especies: List<EspecieDto>,
    val razas: List<RazaDto>,
    val categorias: List<CategoriaDto>,
    @SerializedName("motivos_movimiento") val motivosMovimiento: List<MotivoMovimientoDto>
)

data class EspecieDto(
    val id: Int,
    val nombre: String
)

data class RazaDto(
    val id: Int,
    val nombre: String,
    @SerializedName("especie_id") val especieId: Int
)

data class CategoriaDto(
    val id: Int,
    val nombre: String,
    @SerializedName("especie_id") val especieId: Int
)

data class MotivoMovimientoDto(
    val id: Int,
    val nombre: String,
    val tipo: String
)
