package com.sinc.mobile.domain.model

sealed class AuthResult {
    data class Success(val token: String) : AuthResult()
    object InvalidCredentials : AuthResult()
    object NetworkError : AuthResult()
    data class UnknownError(val message: String) : AuthResult()
}
