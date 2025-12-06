package com.sinc.mobile.data.repository

import android.util.Log
import com.google.gson.Gson
import com.sinc.mobile.data.network.api.TicketApiService
import com.sinc.mobile.data.network.dto.CreateTicketRequest
import com.sinc.mobile.data.network.dto.ErrorResponse
import com.sinc.mobile.domain.model.GenericError
import com.sinc.mobile.domain.model.ticket.CreateTicketData
import com.sinc.mobile.domain.repository.TicketRepository
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class TicketRepositoryImpl @Inject constructor(
    private val apiService: TicketApiService,
    private val gson: Gson
) : TicketRepository {

    override suspend fun createTicket(ticketData: CreateTicketData): Result<Unit, Error> {
        return try {
            val request = CreateTicketRequest(
                mensaje = ticketData.mensaje,
                tipo = ticketData.tipo
            )
            Log.d("TicketRepo", "Request: $request")
            val response = apiService.createTicket(request)

            if (response.isSuccessful) {
                Log.d("TicketRepo", "Response Success: ${response.code()}")
                Result.Success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("TicketRepo", "Response Error: ${response.code()} - $errorBody")
                val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                Result.Failure(GenericError(errorResponse.message ?: "Error desconocido"))
            }
        } catch (e: HttpException) {
            Log.e("TicketRepo", "HttpException: ${e.message()}", e)
            Result.Failure(GenericError("Error de red: ${e.message()}"))
        } catch (e: IOException) {
            Log.e("TicketRepo", "IOException: Verifique la URL de la API y la conexión de red.", e)
            Result.Failure(GenericError("Error de conexión. Por favor, revisa tu conexión a internet."))
        } catch (e: Exception) {
            Log.e("TicketRepo", "Exception: ${e.message}", e)
            Result.Failure(GenericError("Ocurrió un error inesperado: ${e.message}"))
        }
    }
}
