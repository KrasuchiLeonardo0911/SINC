package com.sinc.mobile.app.features.cuaderno

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.app.ui.components.BannerType
import com.sinc.mobile.domain.model.Bitacora
import com.sinc.mobile.domain.use_case.bitacora.*
import com.sinc.mobile.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class BitacoraViewModel @Inject constructor(
    private val getBitacorasUseCase: GetBitacorasUseCase,
    private val syncBitacorasUseCase: SyncBitacorasUseCase,
    private val saveBitacoraUseCase: SaveBitacoraUseCase,
    private val updateBitacoraUseCase: UpdateBitacoraUseCase,
    private val deleteBitacoraUseCase: DeleteBitacoraUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BitacoraState())
    val state = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<BitacoraEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    sealed class BitacoraEvent {
        data class ShowBanner(val message: String, val type: BannerType) : BitacoraEvent()
        object Success : BitacoraEvent()
    }

    init {
        loadBitacoras()
        refreshBitacoras()
    }

    private fun loadBitacoras() {
        getBitacorasUseCase().onEach { bitacoras ->
            _state.update { it.copy(
                bitacoras = bitacoras,
                filteredBitacoras = filterBitacorasByMonth(bitacoras, it.selectedDate),
                isInitialLoad = false
            ) }
        }.launchIn(viewModelScope)
    }

    fun nextMonth() {
        _state.update { 
            val newDate = it.selectedDate.plusMonths(1)
            it.copy(
                selectedDate = newDate,
                filteredBitacoras = filterBitacorasByMonth(it.bitacoras, newDate)
            )
        }
    }

    fun previousMonth() {
        _state.update { 
            val newDate = it.selectedDate.minusMonths(1)
            it.copy(
                selectedDate = newDate,
                filteredBitacoras = filterBitacorasByMonth(it.bitacoras, newDate)
            )
        }
    }

    fun setView(view: CuadernoView) {
        _state.update { it.copy(currentView = view) }
        if (view == CuadernoView.LISTADO) {
            resetTempData()
        }
    }

    fun onDateSelected(date: LocalDate) {
        _state.update { it.copy(tempDate = date, currentView = CuadernoView.PASO_DESCRIPCION) }
    }

    fun onContentChanged(content: String) {
        _state.update { it.copy(tempContent = content) }
    }

    private fun resetTempData() {
        _state.update { it.copy(tempDate = LocalDate.now(), tempContent = "") }
    }

    private fun filterBitacorasByMonth(list: List<Bitacora>, date: LocalDate): List<Bitacora> {
        return list.filter { 
            it.fecha.year == date.year && it.fecha.month == date.month
        }
    }

    fun refreshBitacoras() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = syncBitacorasUseCase()
            if (result is Result.Failure) {
                _state.update { it.copy(error = result.error.message) }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun saveBitacora(fecha: LocalDate, contenido: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            
            // Delay artificial para que se vea la pantalla de carga
            kotlinx.coroutines.delay(500)
            
            val result = saveBitacoraUseCase(fecha, contenido)
            when (result) {
                is Result.Success -> {
                    _eventFlow.emit(BitacoraEvent.Success)
                    _eventFlow.emit(BitacoraEvent.ShowBanner("Registro guardado con éxito", BannerType.SUCCESS))
                }
                is Result.Failure -> {
                    _eventFlow.emit(BitacoraEvent.ShowBanner(result.error.message, BannerType.ERROR))
                }
            }
            _state.update { it.copy(isSaving = false) }
        }
    }

    fun updateBitacora(id: Int, contenido: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val result = updateBitacoraUseCase(id, contenido)
            when (result) {
                is Result.Success -> {
                    _eventFlow.emit(BitacoraEvent.ShowBanner("Registro actualizado", BannerType.SUCCESS))
                }
                is Result.Failure -> {
                    _eventFlow.emit(BitacoraEvent.ShowBanner(result.error.message, BannerType.ERROR))
                }
            }
            _state.update { it.copy(isSaving = false) }
        }
    }

    fun deleteBitacora(id: Int) {
        viewModelScope.launch {
            val result = deleteBitacoraUseCase(id)
            if (result is Result.Failure) {
                _eventFlow.emit(BitacoraEvent.ShowBanner(result.error.message, BannerType.ERROR))
            } else {
                _eventFlow.emit(BitacoraEvent.ShowBanner("Registro eliminado", BannerType.SUCCESS))
            }
        }
    }
}
