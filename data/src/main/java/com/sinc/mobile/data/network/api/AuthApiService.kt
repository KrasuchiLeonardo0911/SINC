package com.sinc.mobile.data.network.api

import com.sinc.mobile.data.network.dto.LoginRequest
import com.sinc.mobile.data.network.dto.UnidadProductivaDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

import com.sinc.mobile.data.network.dto.CatalogosDto

import com.sinc.mobile.data.network.dto.MovimientoRequest

interface AuthApiService {
    @Headers("Accept: application/json")
    @POST("api/movil/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<ResponseBody>

    @Headers("Accept: application/json")
    @GET("api/movil/unidades-productivas")
    suspend fun getUnidadesProductivas(): Response<List<UnidadProductivaDto>>

    @Headers("Accept: application/json")
    @GET("api/movil/catalogos")
    suspend fun getCatalogos(): Response<CatalogosDto>

    @Headers("Accept: application/json")
    @POST("api/movil/cuaderno/movimientos")
    suspend fun saveMovimientos(@Body request: MovimientoRequest): Response<ResponseBody>
}
