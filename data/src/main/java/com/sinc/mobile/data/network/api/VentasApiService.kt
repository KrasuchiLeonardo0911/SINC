package com.sinc.mobile.data.network.api

import com.sinc.mobile.data.network.dto.request.CreateDeclaracionVentaRequest
import com.sinc.mobile.data.network.dto.response.DeclaracionVentaDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface VentasApiService {
    @Headers("Accept: application/json")
    @GET("api/movil/declaraciones-venta")
    suspend fun getDeclaracionesVenta(): Response<List<DeclaracionVentaDto>>

    @Headers("Accept: application/json")
    @POST("api/movil/declaraciones-venta")
    suspend fun createDeclaracionVenta(@Body request: CreateDeclaracionVentaRequest): Response<ResponseBody>

    @Headers("Accept: application/json")
    @DELETE("api/movil/declaraciones-venta/{id}")
    suspend fun cancelDeclaracionVenta(@Path("id") id: Int): Response<ResponseBody>

    @Headers("Accept: application/json")
    @GET("api/productor/stock-disponible-venta")
    suspend fun getStockDisponibleVenta(
        @Query("unidad_productiva_id") upId: Int,
        @Query("especie_id") especieId: Int,
        @Query("raza_id") razaId: Int,
        @Query("categoria_animal_id") categoriaId: Int
    ): Response<StockDisponibleResponse>
}

@kotlinx.serialization.Serializable
data class StockDisponibleResponse(
    @kotlinx.serialization.SerialName("stock_disponible") val stockDisponible: Int
)