package com.speehive.speehiveaihub.network

import com.speehive.speehiveaihub.data.AuthManager
import com.speehive.speehiveaihub.data.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL =
        "http://172.16.50.91:5019/"

    @Volatile
    private var cachedClient: OkHttpClient? = null
    @Volatile
    private var cachedSessionManager: SessionManager? = null

    fun create(
        sessionManager: SessionManager,
        authManager: AuthManager
    ): SpeehiveApiService {

        val client = synchronized(this) {
            if (cachedSessionManager === sessionManager && cachedClient != null) {
                cachedClient!!
            } else {
                OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(
                        AuthInterceptor(sessionManager)
                    )
                    .addInterceptor(
                        TokenValidationInterceptor(authManager)
                    )
                    .build()
                    .also {
                        cachedClient = it
                        cachedSessionManager = sessionManager
                    }
            }
        }

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(SpeehiveApiService::class.java)
    }
}