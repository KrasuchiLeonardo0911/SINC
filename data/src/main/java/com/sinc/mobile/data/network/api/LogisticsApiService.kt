package com.sinc.mobile.data.network.api

import com.sinc.mobile.data.network.dto.LogisticsInfoDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers

interface LogisticsApiService {
    @Headers("Accept: application/json")
    @GET("api/movil/logistica/proxima-visita")
    suspend fun getLogisticsInfo(): Response<LogisticsInfoDto>
}
