package com.sinc.mobile.data.network.api

import com.sinc.mobile.data.model.MovimientoHistorialDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface HistorialMovimientosApiService {
    @Headers("Accept: application/json")
    @GET("api/movil/cuaderno/movimientos")
    suspend fun getHistorialMovimientos(
        @Query("updated_after") updatedAfter: String?
    ): Response<List<MovimientoHistorialDto>>
}