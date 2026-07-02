package com.speehive.speehiveaihub.network

import com.speehive.speehiveaihub.data.AuthManager
import com.speehive.speehiveaihub.data.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL =
        "http://172.16.50.91:5019/"

    fun create(
        sessionManager: SessionManager,
        authManager: AuthManager
    ): SpeehiveApiService {

        val client = OkHttpClient.Builder()
            .addInterceptor(
                AuthInterceptor(sessionManager)
            )
            .addInterceptor(
                TokenValidationInterceptor(authManager)
            )
            .build()

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