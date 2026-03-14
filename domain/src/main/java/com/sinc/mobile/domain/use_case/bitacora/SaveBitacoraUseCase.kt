package com.sinc.mobile.domain.use_case.bitacora

import com.sinc.mobile.domain.model.Bitacora
import com.sinc.mobile.domain.repository.BitacoraRepository
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.domain.util.Error
import java.time.LocalDate
import javax.inject.Inject

class SaveBitacoraUseCase @Inject constructor(
    private val repository: BitacoraRepository
) {
    suspend operator fun invoke(fecha: LocalDate, contenido: String): Result<Bitacora, Error> {
        return repository.saveBitacora(fecha, contenido)
    }
}
