package com.sinc.mobile.data.network.api

import com.sinc.mobile.data.network.dto.LoginRequest
import com.sinc.mobile.data.network.dto.LoginResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import com.sinc.mobile.data.network.dto.CatalogosDto
import com.sinc.mobile.data.network.dto.MovimientoRequest
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded

interface AuthApiService {
    @Headers("Accept: application/json")
    @POST("api/movil/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @Headers("Accept: application/json")
    @GET("api/movil/catalogos")
    suspend fun getCatalogos(): Response<CatalogosDto>

    @Headers("Accept: application/json")
    @POST("api/movil/cuaderno/movimientos")
    suspend fun saveMovimientos(@Body request: MovimientoRequest): Response<ResponseBody>

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("api/movil/password/change")
    suspend fun changePassword(
        @Field("current_password") currentPassword: String,
        @Field("password") password: String,
        @Field("password_confirmation") passwordConfirmation: String
    ): Response<ResponseBody>

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("api/movil/password/request-reset")
    suspend fun requestPasswordReset(
        @Field("email") email: String
    ): Response<ResponseBody>

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("api/movil/password/reset-with-code")
    suspend fun resetPasswordWithCode(
        @Field("email") email: String,
        @Field("code") code: String,
        @Field("password") password: String,
        @Field("password_confirmation") passwordConfirmation: String
    ): Response<ResponseBody>
}
