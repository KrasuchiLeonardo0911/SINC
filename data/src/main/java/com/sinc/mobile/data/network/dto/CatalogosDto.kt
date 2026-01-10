package com.sinc.mobile.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CatalogosDto(
    val especies: List<EspecieDto>?,
    val razas: List<RazaDto>?,
    val categorias: List<CategoriaDto>?,
    @SerialName("motivos_movimiento") val motivosMovimiento: List<MotivoMovimientoDto>?,
    val municipios: List<MunicipioDto>?,
    @SerialName("condiciones_tenencia") val condicionesTenencia: List<CondicionTenenciaDto>?,
    @SerialName("fuentes_agua") val fuentesAgua: List<FuenteAguaDto>?,
    @SerialName("tipos_suelo") val tiposSuelo: List<TipoSueloDto>?,
    @SerialName("tipos_pasto") val tiposPasto: List<TipoPastoDto>?
)

@Serializable
data class EspecieDto(
    val id: Int,
    val nombre: String
)

@Serializable
data class RazaDto(
    val id: Int,
    val nombre: String,
    @SerialName("especie_id") val especieId: Int
)

@Serializable
data class CategoriaDto(
    val id: Int,
    val nombre: String,
    @SerialName("especie_id") val especieId: Int
)

@Serializable
data class MotivoMovimientoDto(
    val id: Int,
    val nombre: String,
    val tipo: String
)

@Serializable
data class MunicipioDto(
    val id: Int,
    val nombre: String,
    val latitud: Double?,
    val longitud: Double?,
    @SerialName("geojson_boundary") val geojsonBoundary: String?
)

@Serializable
data class CondicionTenenciaDto(
    val id: Int,
    val nombre: String
)

@Serializable
data class FuenteAguaDto(
    val id: Int,
    val nombre: String
)

@Serializable
data class TipoSueloDto(
    val id: Int,
    val nombre: String
)

@Serializable
data class TipoPastoDto(
    val id: Int,
    val nombre: String
)