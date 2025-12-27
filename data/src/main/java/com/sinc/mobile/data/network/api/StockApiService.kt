package com.sinc.mobile.data.network.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers

interface StockApiService {
    @Headers("Accept: application/json")
    @GET("/api/movil/stock")
    suspend fun getStock(): Response<ResponseBody>
}
