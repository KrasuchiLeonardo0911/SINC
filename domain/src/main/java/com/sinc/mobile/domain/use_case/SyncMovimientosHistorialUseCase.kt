package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.model.GenericError
import com.sinc.mobile.domain.repository.MovimientoHistorialRepository
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import javax.inject.Inject

class SyncMovimientosHistorialUseCase @Inject constructor(
    private val repository: MovimientoHistorialRepository
) {
    suspend operator fun invoke(): Result<Unit, GenericError> {
        val lastSync = repository.getLastSyncTimestamp()
        val result = repository.syncMovimientos(lastSync)
        if (result is Result.Success) {
            repository.saveLastSyncTimestamp(getCurrentTimestamp())
        }
        return result
    }

    private fun getCurrentTimestamp(): String {
        return java.time.format.DateTimeFormatter.ISO_INSTANT.format(java.time.Instant.now())
    }
}
