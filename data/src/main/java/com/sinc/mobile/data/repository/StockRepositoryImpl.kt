package com.sinc.mobile.data.repository

import com.google.gson.Gson
import com.sinc.mobile.data.local.dao.StockDao
import com.sinc.mobile.data.mapper.toDomain
import com.sinc.mobile.data.mapper.toEntity
import com.sinc.mobile.data.network.api.StockApiService
import com.sinc.mobile.data.network.dto.ErrorResponse
import com.sinc.mobile.domain.model.GenericError
import com.sinc.mobile.domain.model.Stock
import com.sinc.mobile.domain.repository.StockRepository
import com.sinc.mobile.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class StockRepositoryImpl @Inject constructor(
    private val apiService: StockApiService,
    private val stockDao: StockDao,
    private val gson: Gson
) : StockRepository {

    override fun getStock(): Flow<Stock> {
        return stockDao.getStock().map { it?.toDomain() ?: Stock(emptyList(), 0) }
    }

    override suspend fun syncStock(): Result<Unit, GenericError> {
        return try {
            val response = apiService.getStock()
            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    val stockResponseDto = gson.fromJson(responseBody.charStream(), com.sinc.mobile.data.model.StockResponseDto::class.java)
                    val stockEntity = stockResponseDto.data.toEntity()
                    stockDao.insertStock(stockEntity)
                    Result.Success(Unit)
                } ?: Result.Failure(GenericError("Respuesta vacía del servidor."))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                Result.Failure(GenericError(errorResponse.message ?: "Error desconocido en el servidor"))
            }
        } catch (e: HttpException) {
            Result.Failure(GenericError("Error de red: ${e.message()}"))
        } catch (e: IOException) {
            Result.Failure(GenericError("Error de conexión: Asegúrate de tener acceso a internet."))
        } catch (e: Exception) {
            e.printStackTrace() // Log the full stack trace
            Result.Failure(GenericError("Ocurrió un error inesperado al sincronizar el stock. ${e.message}"))
        }
    }
}
