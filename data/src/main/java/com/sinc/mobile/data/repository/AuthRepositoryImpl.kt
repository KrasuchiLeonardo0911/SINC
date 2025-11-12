package com.sinc.mobile.data.repository

import com.google.gson.Gson
import com.sinc.mobile.data.network.api.AuthApiService
import com.sinc.mobile.data.network.dto.ChangePasswordRequest
import com.sinc.mobile.data.network.dto.ErrorResponse
import com.sinc.mobile.data.network.dto.LoginRequest
import com.sinc.mobile.data.network.dto.LoginResponse
import com.sinc.mobile.data.network.dto.RequestPasswordResetRequest
import com.sinc.mobile.data.network.dto.ResetPasswordWithCodeRequest
import com.sinc.mobile.data.network.dto.ValidationErrorResponse
import com.sinc.mobile.data.session.SessionManager
import com.sinc.mobile.domain.model.AuthResult
import com.sinc.mobile.domain.model.ChangePasswordData
import com.sinc.mobile.domain.model.RequestPasswordResetData
import com.sinc.mobile.domain.model.ResetPasswordWithCodeData
import com.sinc.mobile.domain.repository.AuthRepository
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val gson: Gson,
    private val sessionManager: SessionManager
) : AuthRepository {

    @Suppress("ConstantConditionIf")
    override suspend fun login(email: String, password: String): AuthResult {
        try {
            val deviceName = android.os.Build.MODEL
            val request = LoginRequest(email, password, deviceName)
            val response = apiService.login(request)

            if (response.isSuccessful) {
                response.body()?.string()?.let { body ->
                    @Suppress("ConstantConditionIf")
                    if (body.isNotEmpty()) {
                        try {
                            val loginResponse = gson.fromJson(body, LoginResponse::class.java)
                            if (loginResponse.token != null) {
                                android.util.Log.d("TOKEN_DEBUG", "Token recibido y guardado: ${loginResponse.token}")
                                sessionManager.saveAuthToken(loginResponse.token)
                                return AuthResult.Success(loginResponse.token)
                            } else {
                                return AuthResult.UnknownError("La respuesta del servidor no contiene un token.")
                            }
                        } catch (e: Exception) {
                            return AuthResult.UnknownError("Error al parsear la respuesta del servidor.")
                        }
                    } else {
                        return AuthResult.UnknownError("La respuesta del servidor está vacía.")
                    }
                } ?: return AuthResult.UnknownError("La respuesta del servidor es nula.")
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
        val errorBodyString: String?
        try {
            errorBodyString = response.errorBody()?.string()
        } catch (e: IOException) {
            return AuthResult.NetworkError
        }

        if (errorBodyString == null) {
            return AuthResult.UnknownError("Error desconocido con cuerpo vacío: ${response.code()}")
        }

        return when (response.code()) {
            401 -> AuthResult.InvalidCredentials
            422 -> {
                try {
                    val validationError = gson.fromJson(errorBodyString, ValidationErrorResponse::class.java)
                    val firstErrorMessage = validationError.errors.values.firstOrNull()?.firstOrNull()
                    AuthResult.UnknownError(firstErrorMessage ?: validationError.message)
                } catch (e: Exception) {
                    return AuthResult.UnknownError("Error al parsear la respuesta de validación.")
                }
            }
            else -> {
                try {
                    val errorResponse = gson.fromJson(errorBodyString, ErrorResponse::class.java)
                    AuthResult.UnknownError(errorResponse.message)
                } catch (e: Exception) {
                    return AuthResult.UnknownError("Error al parsear la respuesta de error: ${response.code()}")
                }
            }
        }
    }

    private fun handleResultUnitResponse(response: retrofit2.Response<*>): Result<Unit> {
        val errorBody = response.errorBody()?.string()
        val errorMessage = if (errorBody != null) {
            try {
                val validationError = gson.fromJson(errorBody, ValidationErrorResponse::class.java)
                validationError.errors.values.firstOrNull()?.firstOrNull() ?: validationError.message
            } catch (e: Exception) {
                try {
                    val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
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
            val request = ChangePasswordRequest(
                currentPassword = passwordData.currentPassword,
                password = passwordData.newPassword,
                passwordConfirmation = passwordData.newPasswordConfirmation
            )
            val response = apiService.changePassword(request)
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
            val request = RequestPasswordResetRequest(email = data.email)
            val response = apiService.requestPasswordReset(request)
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
            val request = ResetPasswordWithCodeRequest(
                email = data.email,
                code = data.code,
                password = data.password,
                passwordConfirmation = data.passwordConfirmation
            )
            val response = apiService.resetPasswordWithCode(request)
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

