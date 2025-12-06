package com.sinc.mobile.data.network

import com.sinc.mobile.data.network.dto.ApiResponse
import com.sinc.mobile.data.network.dto.IdentifierConfigDto
import retrofit2.http.GET

interface IdentifierApiService {
    @GET("api/movil/identificadores-locales")
    suspend fun getIdentifierConfigs(): ApiResponse<List<IdentifierConfigDto>>
}
