package com.sinc.mobile.app.features.logistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.LogisticsInfo
import com.sinc.mobile.domain.use_case.GetLogisticsInfoUseCase
import com.sinc.mobile.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import com.sinc.mobile.data.session.SessionManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class LogisticsUiState(
    val isLoading: Boolean = false,
    val logisticsInfo: LogisticsInfo? = null,
    val daysRemaining: Long? = null,
    val orderDeadline: LocalDate? = null,
    val error: String? = null
)

@HiltViewModel
class LogisticsViewModel @Inject constructor(
    private val getLogisticsInfoUseCase: GetLogisticsInfoUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogisticsUiState())
    val uiState: StateFlow<LogisticsUiState> = _uiState.asStateFlow()

    init {
        loadLogisticsInfo()
    }

    private fun loadLogisticsInfo() {
        _uiState.update { it.copy(isLoading = true) }
        
        // Cargar datos desde SessionManager (Fuente de verdad V2)
        val nextVisitString = sessionManager.getNextVisitDate()
        val deadlineString = sessionManager.getOrderDeadline()
        
        var nextVisitDate: LocalDate? = null
        var deadlineDate: LocalDate? = null
        var daysRemaining: Long? = null

        try {
            if (nextVisitString != null) {
                nextVisitDate = LocalDate.parse(nextVisitString.take(10))
                daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), nextVisitDate)
            }
            if (deadlineString != null) {
                deadlineDate = LocalDate.parse(deadlineString.take(10))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Mantener compatibilidad con el UseCase existente para frecuenciaDias
        // pero priorizar las fechas de SessionManager
        getLogisticsInfoUseCase().onEach { result ->
            when (result) {
                is Result.Success -> {
                    // Usamos la info del UseCase (frecuencia) pero mantenemos las fechas de sesión si existen
                    val info = result.data.copy(proximaVisita = nextVisitDate ?: result.data.proximaVisita)
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            logisticsInfo = info,
                            daysRemaining = daysRemaining,
                            orderDeadline = deadlineDate
                        )
                    }
                }
                is Result.Failure -> {
                    // Si falla el use case, al menos mostramos lo que tenemos de sesión
                    val fallbackInfo = if (nextVisitDate != null) LogisticsInfo(nextVisitDate, 0) else null
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            logisticsInfo = fallbackInfo,
                            daysRemaining = daysRemaining,
                            orderDeadline = deadlineDate,
                            error = if (fallbackInfo == null) result.error.message else null
                        ) 
                    }
                }
            }
        }.launchIn(viewModelScope)
    }
}
