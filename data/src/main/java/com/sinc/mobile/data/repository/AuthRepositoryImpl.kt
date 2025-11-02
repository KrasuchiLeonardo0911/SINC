package com.sinc.mobile.data.repository

import com.google.gson.Gson
import com.sinc.mobile.data.network.api.AuthApiService
import com.sinc.mobile.data.network.dto.ErrorResponse
import com.sinc.mobile.data.network.dto.LoginRequest
import com.sinc.mobile.data.network.dto.LoginResponse
import com.sinc.mobile.data.network.dto.ValidationErrorResponse
import com.sinc.mobile.domain.model.AuthResult
import com.sinc.mobile.domain.repository.AuthRepository
import java.io.IOException
import javax.inject.Inject

import com.sinc.mobile.data.session.SessionManager

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
                return handleError(response)
            }
        } catch (e: IOException) {
            return AuthResult.NetworkError
        } catch (e: Exception) {
            return AuthResult.UnknownError(e.message ?: "Ocurrió un error inesperado.")
        }
    }

    private fun handleError(response: retrofit2.Response<*>): AuthResult {
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
}
