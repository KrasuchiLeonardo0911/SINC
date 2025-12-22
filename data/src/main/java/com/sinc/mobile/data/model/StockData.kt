package com.sinc.mobile.data.model

import com.google.gson.annotations.SerializedName

/**
 * Estas son clases de datos (POKOs) que representan la estructura del JSON
 * devuelto por la API de stock. No son entidades de Room.
 */

data class StockApiResponse(
    val data: StockData,
    val message: String
)

data class StockData(
    @SerializedName("unidades_productivas")
    val unidadesProductivas: List<UnidadProductivaStock>,
    @SerializedName("stock_total_general")
    val stockTotalGeneral: Int
)

data class UnidadProductivaStock(
    val id: Int,
    val nombre: String,
    @SerializedName("stock_total")
    val stockTotal: Int,
    val especies: List<EspecieStock>
)

data class EspecieStock(
    val nombre: String,
    @SerializedName("stock_total")
    val stockTotal: Int,
    val desglose: List<DesgloseStock>
)

data class DesgloseStock(
    val categoria: String,
    val raza: String,
    val cantidad: Int
)
