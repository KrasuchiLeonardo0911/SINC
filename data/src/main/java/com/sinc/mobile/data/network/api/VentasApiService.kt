package com.sinc.mobile.data.network.api

import com.sinc.mobile.data.network.dto.request.CreateDeclaracionVentaRequest
import com.sinc.mobile.data.network.dto.response.DeclaracionVentaDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface VentasApiService {
    @Headers("Accept: application/json")
    @GET("api/movil/declaraciones-venta")
    suspend fun getDeclaracionesVenta(): Response<List<DeclaracionVentaDto>>

    @Headers("Accept: application/json")
    @POST("api/movil/declaraciones-venta")
    suspend fun createDeclaracionVenta(@Body request: CreateDeclaracionVentaRequest): Response<ResponseBody>
}
