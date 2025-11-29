package com.sinc.mobile.data.model.geojson

import com.google.gson.annotations.SerializedName

data class FeatureCollectionDto(
    val type: String,
    val features: List<FeatureDto>
)

data class FeatureDto(
    val type: String,
    val geometry: GeometryDto,
    val properties: PropertiesDto
)

data class GeometryDto(
    val type: String,
    val coordinates: List<List<List<Double>>> // For Polygon type
)

data class PropertiesDto(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("centroide_lat") val centroideLat: Double,
    @SerializedName("centroide_lon") val centroideLon: Double
)
