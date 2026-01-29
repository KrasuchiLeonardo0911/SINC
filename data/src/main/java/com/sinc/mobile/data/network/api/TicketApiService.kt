package com.sinc.mobile.data.network.api

import com.sinc.mobile.data.network.dto.AddMessageRequest
import com.sinc.mobile.data.network.dto.CreateTicketRequest
import com.sinc.mobile.data.network.dto.CreateTicketResponse
import com.sinc.mobile.data.network.dto.TicketDto
import retrofit2.http.*

interface TicketApiService {

    @GET("api/movil/tickets")
    suspend fun getTickets(): List<TicketDto>

    @POST("api/movil/tickets")
    suspend fun createTicket(@Body request: CreateTicketRequest): CreateTicketResponse

    @GET("api/movil/tickets/{id}")
    suspend fun getTicket(@Path("id") ticketId: Long): TicketDto

    @POST("api/movil/tickets/{id}/messages")
    suspend fun addMessage(
        @Path("id") ticketId: Long,
        @Body request: AddMessageRequest
    ): TicketDto

    @POST("api/movil/tickets/{id}/resolve")
    suspend fun resolveTicket(@Path("id") ticketId: Long): TicketDto
}
