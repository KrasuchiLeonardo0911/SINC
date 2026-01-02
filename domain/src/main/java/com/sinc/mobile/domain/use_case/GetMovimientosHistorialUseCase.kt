package com.sinc.mobile.domain.use_case

import com.sinc.mobile.domain.model.MovimientoHistorial
import com.sinc.mobile.domain.repository.MovimientoHistorialRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMovimientosHistorialUseCase @Inject constructor(
    private val repository: MovimientoHistorialRepository
) {
    operator fun invoke(): Flow<List<MovimientoHistorial>> {
        return repository.getMovimientos()
    }
}
