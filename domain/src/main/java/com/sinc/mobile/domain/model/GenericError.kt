package com.sinc.mobile.domain.model

import com.sinc.mobile.domain.util.Error

data class GenericError(override val message: String) : Error
