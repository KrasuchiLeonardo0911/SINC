package com.sinc.mobile.data.network

import com.sinc.mobile.data.session.SessionManager
import com.sinc.mobile.domain.navigation.NavigationCommand
import com.sinc.mobile.domain.navigation.NavigationManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorInterceptor @Inject constructor(
    private val sessionManager: SessionManager,
    private val navigationManager: NavigationManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code == 401) {
            // Clear session and trigger navigation to login
            sessionManager.clearAuthToken()
            runBlocking {
                navigationManager.navigate(NavigationCommand.NavigateToLogin)
            }
        }

        return response
    }
}
