package com.sinc.mobile.data.network.api

import com.sinc.mobile.data.network.dto.request.CreateUnidadProductivaRequest
import com.sinc.mobile.data.network.dto.response.UnidadProductivaDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface UnidadProductivaApiService {

    @Headers("Accept: application/json")
    @GET("api/movil/unidades-productivas")
    suspend fun getUnidadesProductivas(): Response<List<UnidadProductivaDto>>

    @Headers("Accept: application/json")
    @POST("api/movil/unidades-productivas")
    suspend fun createUnidadProductiva(@Body request: CreateUnidadProductivaRequest): Response<UnidadProductivaDto>
}
