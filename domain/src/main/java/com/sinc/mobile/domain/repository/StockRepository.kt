package com.sinc.mobile.domain.repository

import com.sinc.mobile.domain.model.Stock
import com.sinc.mobile.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    fun getStock(): Flow<Stock>
    suspend fun syncStock(): Result<Unit, com.sinc.mobile.domain.model.GenericError>
}
