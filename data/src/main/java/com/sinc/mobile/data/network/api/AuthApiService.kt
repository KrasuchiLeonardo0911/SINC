package com.sinc.mobile.data.network.api

import com.sinc.mobile.data.network.dto.LoginRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApiService {
    @Headers("Accept: application/json")
    @POST("api/movil/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<ResponseBody>
}
