package com.speehive.speehiveaihub.network

import com.speehive.speehiveaihub.data.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(
        chain: Interceptor.Chain
    ): Response {

        val request = chain.request()
        val isLoginRequest = request.url.encodedPath.contains("api/Auth/login")

        val token = sessionManager.getToken()

        val finalRequest = request
            .newBuilder()
            .apply {
                if (!isLoginRequest && !token.isNullOrEmpty()) {
                    addHeader(
                        "Authorization",
                        "Bearer $token"
                    )
                }
            }
            .build()

        return chain.proceed(finalRequest)
    }
}