package com.speehive.speehiveaihub.network

import com.speehive.speehiveaihub.data.AuthManager
import okhttp3.Interceptor
import okhttp3.Response

class TokenValidationInterceptor(
    private val authManager: AuthManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        val isLoginRequest = request.url.encodedPath.contains("api/Auth/login")

        if (!isLoginRequest && response.code == 401) {
            authManager.onAuthError(AuthError.TokenExpired)
        }

        return response
    }
}
