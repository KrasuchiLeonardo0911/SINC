package com.sinc.mobile.domain.use_case.ventas

import com.sinc.mobile.domain.model.DeclaracionVenta
import com.sinc.mobile.domain.repository.VentasRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDeclaracionesVentaUseCase @Inject constructor(
    private val repository: VentasRepository
) {
    operator fun invoke(): Flow<List<DeclaracionVenta>> {
        return repository.getDeclaraciones()
    }
}
