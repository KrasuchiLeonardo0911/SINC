package com.sinc.mobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Estas son clases de datos (POKOs) que representan la estructura del JSON
 * devuelto por la API de stock. No son entidades de Room.
 */

@Serializable
data class StockApiResponse(
    val data: StockData,
    val message: String
)

@Serializable
data class StockData(
    @SerialName("unidades_productivas")
    val unidadesProductivas: List<UnidadProductivaStock>,
    @SerialName("stock_total_general")
    val stockTotalGeneral: Int
)

@Serializable
data class UnidadProductivaStock(
    val id: Int,
    val nombre: String,
    @SerialName("stock_total")
    val stockTotal: Int,
    val especies: List<EspecieStock>
)

@Serializable
data class EspecieStock(
    val nombre: String,
    @SerialName("stock_total")
    val stockTotal: Int,
    val desglose: List<DesgloseStock>
)

@Serializable
data class DesgloseStock(
    val categoria: String,
    val raza: String,
    val cantidad: Int
)
