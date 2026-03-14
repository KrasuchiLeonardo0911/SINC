package com.sinc.mobile.domain.repository

import com.sinc.mobile.domain.model.Bitacora
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface BitacoraRepository {
    fun getBitacoras(): Flow<List<Bitacora>>
    suspend fun syncBitacoras(): Result<Unit, Error>
    suspend fun saveBitacora(fecha: LocalDate, contenido: String): Result<Bitacora, Error>
    suspend fun updateBitacora(id: Int, contenido: String): Result<Bitacora, Error>
    suspend fun deleteBitacora(id: Int): Result<Unit, Error>
}
