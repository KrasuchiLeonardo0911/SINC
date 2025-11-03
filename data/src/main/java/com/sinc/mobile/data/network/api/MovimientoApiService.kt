package com.sinc.mobile.data.network.api

import com.sinc.mobile.data.network.dto.MovimientosBatchRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface MovimientoApiService {
    @Headers("Accept: application/json")
    @POST("movil/cuaderno/movimientos")
    suspend fun saveMovimientos(@Body request: MovimientosBatchRequest): Response<ResponseBody>
}
