package com.sinc.mobile.data.network.api.agenda

import com.sinc.mobile.data.network.dto.agenda.AgendaItemDto
import com.sinc.mobile.data.network.dto.agenda.AgendaResponseDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AgendaApiService {
    @Headers("Accept: application/json")
    @GET("api/movil/agenda")
    suspend fun getAgendaItems(): Response<List<AgendaItemDto>>

    @Headers("Accept: application/json")
    @POST("api/movil/agenda")
    suspend fun saveAgendaItem(
        @Body item: AgendaItemDto
    ): Response<AgendaItemDto>

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @PUT("api/movil/agenda/{id}")
    suspend fun updateAgendaStatus(
        @Path("id") id: Long,
        @Field("completada") isCompleted: Boolean
    ): Response<ResponseBody>

    @Headers("Accept: application/json")
    @DELETE("api/movil/agenda/{id}")
    suspend fun deleteAgendaItem(
        @Path("id") id: Long
    ): Response<ResponseBody>
}
