package com.speehive.speehiveaihub.network

import com.speehive.speehiveaihub.data.AuthManager
import com.speehive.speehiveaihub.data.SessionManager
import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL =
        "https://debian.tailbd6bc8.ts.net/"

    private val cloudflareDns: Dns by lazy {
        val bootstrapClient = OkHttpClient.Builder().build()
        DnsOverHttps.Builder()
            .client(bootstrapClient)
            .url("https://1.1.1.1/dns-query".toHttpUrl())
            .bootstrapDnsHosts(
                InetAddress.getByName("1.1.1.1"),
                InetAddress.getByName("1.0.0.1")
            )
            .build()
    }

    private val hybridDns = object : Dns {
        override fun lookup(hostname: String): List<InetAddress> {
            return try {
                Dns.SYSTEM.lookup(hostname)
            } catch (e: UnknownHostException) {
                try {
                    cloudflareDns.lookup(hostname)
                } catch (_: Exception) {
                    throw e
                }
            }
        }
    }

    fun getFormattedImageUrl(url: String?): String? {
        if (url.isNullOrBlank()) return null
        return when {
            url.startsWith("http://") || url.startsWith("https://") -> {
                val regex = Regex("^https?://[^/]+")
                val hostMatched = regex.find(url)?.value ?: return url
                if (hostMatched.contains("172.16.70.37") ||
                    hostMatched.contains("localhost") ||
                    hostMatched.contains("127.0.0.1") ||
                    hostMatched.contains("10.0.2.2")
                ) {
                    val path = url.substring(hostMatched.length)
                    val cleanPath = if (path.startsWith("/")) path.substring(1) else path
                    "$BASE_URL$cleanPath"
                } else {
                    url
                }
            }
            else -> {
                val cleanUrl = if (url.startsWith("/")) url.substring(1) else url
                "$BASE_URL$cleanUrl"
            }
        }
    }

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
                val loggingInterceptor = okhttp3.logging.HttpLoggingInterceptor().apply {
                    level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
                }
                OkHttpClient.Builder()
                    .dns(hybridDns)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(
                        AuthInterceptor(sessionManager)
                    )
                    .addInterceptor(
                        TokenValidationInterceptor(authManager)
                    )
                    .addInterceptor(loggingInterceptor)
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