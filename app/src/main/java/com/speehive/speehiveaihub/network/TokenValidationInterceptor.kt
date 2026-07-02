package com.speehive.speehiveaihub.network

import com.speehive.speehiveaihub.data.AuthManager
import okhttp3.Interceptor
import okhttp3.Response

class TokenValidationInterceptor(
    private val authManager: AuthManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        when (response.code) {
            401 -> {
                authManager.onAuthError(AuthError.TokenExpired)
            }
            403 -> {
                authManager.onAuthError(AuthError.Unauthorized)
            }
        }

        return response
    }
}
