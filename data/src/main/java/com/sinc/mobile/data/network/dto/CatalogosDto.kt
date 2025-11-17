package com.sinc.mobile.data.network.dto

import com.google.gson.annotations.SerializedName

data class CatalogosDto(
    val especies: List<EspecieDto>?,
    val razas: List<RazaDto>?,
    val categorias: List<CategoriaDto>?,
    @SerializedName("motivos_movimiento") val motivosMovimiento: List<MotivoMovimientoDto>?,
    val municipios: List<MunicipioDto>?,
    @SerializedName("condiciones_tenencia") val condicionesTenencia: List<CondicionTenenciaDto>?,
    @SerializedName("fuentes_agua") val fuentesAgua: List<FuenteAguaDto>?,
    @SerializedName("tipos_suelo") val tiposSuelo: List<TipoSueloDto>?,
    @SerializedName("tipos_pasto") val tiposPasto: List<TipoPastoDto>?
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

data class MunicipioDto(
    val id: Int,
    val nombre: String
)

data class CondicionTenenciaDto(
    val id: Int,
    val nombre: String
)

data class FuenteAguaDto(
    val id: Int,
    val nombre: String
)

data class TipoSueloDto(
    val id: Int,
    val nombre: String
)

data class TipoPastoDto(
    val id: Int,
    val nombre: String
)