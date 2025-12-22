package com.sinc.mobile.data.network.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface StockApiService {
    @GET("movil/stock")
    suspend fun getStock(): Response<ResponseBody>
}
