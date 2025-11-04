package com.sinc.mobile.data.network

import com.sinc.mobile.data.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = sessionManager.getAuthToken()
        val request = chain.request().newBuilder()
        // token?.let {
        //     request.addHeader("Authorization", "Bearer $it")
        // }
        return chain.proceed(request.build())
    }
}
