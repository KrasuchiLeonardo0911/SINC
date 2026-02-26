package com.sinc.mobile.data.network.api

import com.sinc.mobile.data.network.dto.WeatherAlertResponseDto
import retrofit2.Response
import retrofit2.http.GET

interface WeatherAlertApiService {
    @GET("api/movil/alertas-meteorologicas")
    suspend fun getActiveAlerts(): Response<WeatherAlertResponseDto>
}
