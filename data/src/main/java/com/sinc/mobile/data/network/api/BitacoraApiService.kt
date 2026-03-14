package com.sinc.mobile.data.network.api

import com.sinc.mobile.data.network.dto.BitacoraDto
import com.sinc.mobile.data.network.dto.CreateBitacoraRequest
import com.sinc.mobile.data.network.dto.UpdateBitacoraRequest
import retrofit2.Response
import retrofit2.http.*

interface BitacoraApiService {
    @GET("api/movil/bitacoras-diarias")
    suspend fun getBitacoras(): Response<List<BitacoraDto>>

    @POST("api/movil/bitacoras-diarias")
    suspend fun createBitacora(@Body request: CreateBitacoraRequest): Response<BitacoraDto>

    @GET("api/movil/bitacoras-diarias/{id}")
    suspend fun getBitacoraDetail(@Path("id") id: Int): Response<BitacoraDto>

    @PUT("api/movil/bitacoras-diarias/{id}")
    suspend fun updateBitacora(@Path("id") id: Int, @Body request: UpdateBitacoraRequest): Response<BitacoraDto>

    @DELETE("api/movil/bitacoras-diarias/{id}")
    suspend fun deleteBitacora(@Path("id") id: Int): Response<Unit>
}
