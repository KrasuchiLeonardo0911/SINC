package com.sinc.mobile.domain.repository

import com.sinc.mobile.domain.model.GenericError
import com.sinc.mobile.domain.model.MovimientoHistorial
import com.sinc.mobile.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface MovimientoHistorialRepository {
    fun getMovimientos(): Flow<List<MovimientoHistorial>>
    suspend fun syncMovimientos(lastSyncTimestamp: String?): Result<Unit, GenericError>
    
    suspend fun getLastSyncTimestamp(): String?
    suspend fun saveLastSyncTimestamp(timestamp: String)
}