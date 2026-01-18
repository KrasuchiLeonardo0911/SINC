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
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class LogisticsUiState(
    val isLoading: Boolean = false,
    val logisticsInfo: LogisticsInfo? = null,
    val daysRemaining: Long? = null,
    val error: String? = null
)

@HiltViewModel
class LogisticsViewModel @Inject constructor(
    private val getLogisticsInfoUseCase: GetLogisticsInfoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogisticsUiState())
    val uiState: StateFlow<LogisticsUiState> = _uiState.asStateFlow()

    init {
        loadLogisticsInfo()
    }

    private fun loadLogisticsInfo() {
        _uiState.update { it.copy(isLoading = true) }
        
        getLogisticsInfoUseCase().onEach { result ->
            when (result) {
                is Result.Success -> {
                    val info = result.data
                    val daysRemaining = info.proximaVisita?.let { 
                        ChronoUnit.DAYS.between(LocalDate.now(), it) 
                    }
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            logisticsInfo = info,
                            daysRemaining = daysRemaining
                        )
                    }
                }
                is Result.Failure -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = result.error.message
                        ) 
                    }
                }
            }
        }.launchIn(viewModelScope)
    }
}
