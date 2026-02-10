package com.sinc.mobile.app.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sinc.mobile.domain.model.AppControl
import com.sinc.mobile.domain.model.Features
import com.sinc.mobile.domain.use_case.init.InitializeAppUseCase
import com.sinc.mobile.domain.use_case.profile.GetUserProfileUseCase
import com.sinc.mobile.domain.util.Result
import com.sinc.mobile.data.session.SessionManager
import com.sinc.mobile.domain.use_case.auth.SendFcmTokenUseCase
import com.sinc.mobile.domain.use_case.notification.GetUnreadNotificationCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class MainViewModel @Inject constructor(
    private val initializeAppUseCase: InitializeAppUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val sendFcmTokenUseCase: SendFcmTokenUseCase,
    private val getUnreadNotificationCountUseCase: GetUnreadNotificationCountUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        initializeApp()
        observeUnreadNotifications()
    }

    private fun observeUnreadNotifications() {
        getUnreadNotificationCountUseCase()
            .onEach { count ->
                _uiState.update { it.copy(unreadNotificationCount = count) }
            }
            .launchIn(viewModelScope)
    }

    private fun initializeApp() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Force minimum splash duration of 1 second for better UX
            val minDelay = async { delay(1000) }

            val result = initializeAppUseCase()

            // Wait for the minimum delay to finish
            minDelay.await()

            when (result) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            appControl = result.data.appControl,
                            features = result.data.features,
                            isInitialized = true
                        )
                    }
                    // After successful initialization, fetch the user profile
                    fetchUserFirstName()
                }
                is Result.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Error al conectar con el servidor",
                            isInitialized = true
                        )
                    }
                }
            }
        }
    }

    private fun fetchUserFirstName() {
        viewModelScope.launch {
            when (val profileResult = getUserProfileUseCase()) {
                is Result.Success -> {
                    val firstName = profileResult.data.name.split(" ").firstOrNull() ?: profileResult.data.name
                    _uiState.update {
                        it.copy(userName = firstName)
                    }
                }
                is Result.Failure -> {
                    // We can log this error, but we probably don't want to show it to the user
                    // as it's not critical for the main screen functionality.
                    // For now, we'll just use a fallback name.
                    _uiState.update {
                        it.copy(userName = "Productor")
                    }
                }
            }
        }
    }


    fun resetNavigationToCreateUnidadProductiva() {
        _uiState.update { it.copy(shouldNavigateToCreateUnidadProductiva = false) }
    }

    fun sendFcmToken(token: String) {
        // Only send the token if the user is logged in
        if (sessionManager.getAuthToken() != null) {
            viewModelScope.launch {
                when(val result = sendFcmTokenUseCase(token)) {
                    is Result.Success -> {
                        Log.d("FCM_TOKEN_SEND", "FCM Token sent successfully.")
                    }
                    is Result.Failure -> {
                        Log.e("FCM_TOKEN_SEND", "Failed to send FCM Token: ${result.error.message}")
                    }
                }
            }
        } else {
            Log.w("FCM_TOKEN_SEND", "User not logged in. FCM Token will not be sent.")
        }
    }
}

data class MainUiState(
    val isLoading: Boolean = true,
    val isInitialized: Boolean = false,
    val error: String? = null,
    val shouldNavigateToCreateUnidadProductiva: Boolean = false,
    val appControl: AppControl? = null,
    val features: Features? = null,
    val userName: String? = null,
    val unreadNotificationCount: Int = 0
)