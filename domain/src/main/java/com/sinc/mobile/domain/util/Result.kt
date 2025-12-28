package com.sinc.mobile.domain.util

sealed class Result<T, E : Error> {
    data class Success<T, E : Error>(val data: T) : Result<T, E>()
    data class Failure<T, E : Error>(val error: E) : Result<T, E>()
}

interface Error {
    val message: String
}
