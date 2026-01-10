package com.sinc.mobile.data.model.geojson

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeatureCollectionDto(
    val type: String,
    val features: List<FeatureDto>
)

@Serializable
data class FeatureDto(
    val type: String,
    val geometry: GeometryDto,
    val properties: PropertiesDto
)

@Serializable
data class GeometryDto(
    val type: String,
    val coordinates: List<List<List<Double>>> // For Polygon type
)

@Serializable
data class PropertiesDto(
    @SerialName("nombre") val nombre: String,
    @SerialName("centroide_lat") val centroideLat: Double,
    @SerialName("centroide_lon") val centroideLon: Double
)
