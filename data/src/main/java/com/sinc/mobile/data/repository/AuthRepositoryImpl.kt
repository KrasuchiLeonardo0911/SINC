package com.sinc.mobile.data.repository

import com.sinc.mobile.data.network.api.AuthApiService
import com.sinc.mobile.data.network.dto.ErrorResponse
import com.sinc.mobile.data.network.dto.LoginRequest
import com.sinc.mobile.data.network.dto.ValidationErrorResponse
import com.sinc.mobile.data.session.SessionManager
import com.sinc.mobile.domain.model.AuthResult
import com.sinc.mobile.domain.model.ChangePasswordData
import com.sinc.mobile.domain.model.RequestPasswordResetData
import com.sinc.mobile.domain.model.ResetPasswordWithCodeData
import com.sinc.mobile.domain.repository.AuthRepository
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import kotlinx.serialization.serializer

class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val json: Json,
    private val sessionManager: SessionManager
) : AuthRepository {

    @Suppress("ConstantConditionIf")
    override suspend fun login(email: String, password: String): AuthResult {
        try {
            val deviceName = android.os.Build.MODEL
            val request = LoginRequest(email, password, deviceName)
            val response = apiService.login(request)

            if (response.isSuccessful) {
                val loginResponse = response.body()
                if (loginResponse?.token != null) {
                    android.util.Log.d("TOKEN_DEBUG", "Token recibido y guardado: ${loginResponse.token}")
                    sessionManager.saveAuthToken(loginResponse.token)
                    return AuthResult.Success(loginResponse.token)
                } else {
                    return AuthResult.UnknownError("La respuesta del servidor no contiene un token.")
                }
            }
            else {
                return handleAuthError(response)
            }
        } catch (e: IOException) {
            return AuthResult.NetworkError
        } catch (e: Exception) {
            return AuthResult.UnknownError(e.message ?: "Ocurrió un error inesperado.")
        }
    }

    private fun handleAuthError(response: retrofit2.Response<*>): AuthResult {
        val errorBodyString = try {
            response.errorBody()?.string()
        } catch (e: IOException) {
            return AuthResult.NetworkError
        }

        if (errorBodyString.isNullOrBlank()) {
            return if (response.code() == 401) {
                AuthResult.InvalidCredentials
            } else {
                AuthResult.UnknownError("Error desconocido con cuerpo vacío: ${response.code()}")
            }
        }

        // Intento 1: Parsear como el error más complejo (ValidationErrorResponse)
        try {
            val validationError = json.decodeFromString(ValidationErrorResponse.serializer(), errorBodyString)
            val firstErrorMessage = validationError.errors.values.firstOrNull()?.firstOrNull()
            return AuthResult.UnknownError(firstErrorMessage ?: validationError.message)
        } catch (e: Exception) {
            // Si falla, no hacemos nada y pasamos al siguiente intento
            android.util.Log.w("AuthErrorParser", "No se pudo parsear como ValidationErrorResponse: ${e.message}")
        }

        // Intento 2: Parsear como el error simple (ErrorResponse)
        try {
            val errorResponse = json.decodeFromString(ErrorResponse.serializer(), errorBodyString)
            // Si el mensaje es "Credenciales inválidas", devolvemos el tipo específico
            if (errorResponse.message.contains("Credenciales inv", ignoreCase = true)) {
                return AuthResult.InvalidCredentials
            }
            return AuthResult.UnknownError(errorResponse.message)
        } catch (e: Exception) {
            android.util.Log.e("AuthErrorParser", "No se pudo parsear como ErrorResponse: ${e.message}")
        }

        // Fallback final
        return if (response.code() == 401) {
            AuthResult.InvalidCredentials
        } else {
            AuthResult.UnknownError("No se pudo interpretar la respuesta del servidor.")
        }
    }

    private fun handleResultUnitResponse(response: retrofit2.Response<*>): Result<Unit> {
        val errorBody = response.errorBody()?.string()
        val errorMessage = if (errorBody != null) {
            try {
                val validationError = json.decodeFromString(ValidationErrorResponse.serializer(), errorBody)
                validationError.errors.values.firstOrNull()?.firstOrNull() ?: validationError.message
            } catch (e: Exception) {
                try {
                    val errorResponse = json.decodeFromString(ErrorResponse.serializer(), errorBody)
                    errorResponse.message
                } catch (e2: Exception) {
                    "Error al procesar la respuesta del servidor."
                }
            }
        } else {
            "Error desconocido."
        }
        return Result.failure(Exception(errorMessage))
    }


    override suspend fun logout(): Result<Unit> {
        return try {
            sessionManager.clearAuthToken()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changePassword(passwordData: ChangePasswordData): Result<Unit> {
        try {
            val response = apiService.changePassword(
                currentPassword = passwordData.currentPassword,
                password = passwordData.newPassword,
                passwordConfirmation = passwordData.newPasswordConfirmation
            )
            return if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                handleResultUnitResponse(response)
            }
        } catch (e: IOException) {
            return Result.failure(Exception("Error de red. Por favor, comprueba tu conexión."))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun requestPasswordReset(data: RequestPasswordResetData): Result<Unit> {
        try {
            val response = apiService.requestPasswordReset(data.email)
            return if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                handleResultUnitResponse(response)
            }
        } catch (e: IOException) {
            return Result.failure(Exception("Error de red. Por favor, comprueba tu conexión."))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun resetPasswordWithCode(data: ResetPasswordWithCodeData): Result<Unit> {
        try {
            val response = apiService.resetPasswordWithCode(
                email = data.email,
                code = data.code,
                password = data.password,
                passwordConfirmation = data.passwordConfirmation
            )
            return if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                handleResultUnitResponse(response)
            }
        } catch (e: IOException) {
            return Result.failure(Exception("Error de red. Por favor, comprueba tu conexión."))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}