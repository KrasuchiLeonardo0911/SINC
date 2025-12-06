package com.sinc.mobile.data.network.api

import com.sinc.mobile.data.network.dto.CreateTicketRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface TicketApiService {
    @Headers("Accept: application/json")
    @POST("/api/movil/tickets")
    suspend fun createTicket(@Body request: CreateTicketRequest): Response<ResponseBody>
}
