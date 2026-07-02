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

        val token = sessionManager.getToken()

        val request = chain.request()
            .newBuilder()
            .apply {
                if (!token.isNullOrEmpty()) {
                    addHeader(
                        "Authorization",
                        "Bearer $token"
                    )
                }
            }
            .build()

        return chain.proceed(request)
    }
}