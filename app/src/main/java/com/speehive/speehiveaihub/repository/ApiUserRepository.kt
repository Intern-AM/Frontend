package com.speehive.speehiveaihub.repository

import com.speehive.speehiveaihub.data.AuthManager
import com.speehive.speehiveaihub.data.SessionManager
import com.speehive.speehiveaihub.models.User
import com.speehive.speehiveaihub.network.AuthError
import com.speehive.speehiveaihub.network.LoginRequest
import com.speehive.speehiveaihub.network.RetrofitClient
import com.speehive.speehiveaihub.network.safeApiCall

class ApiUserRepository(
    private val sessionManager: SessionManager,
    private val authManager: AuthManager
) : UserRepository {

    override suspend fun login(
        email: String,
        password: String
    ): Result<User?> = safeApiCall {
        val api = RetrofitClient.create(sessionManager, authManager)

        val response = api.login(
            LoginRequest(
                email = email,
                password = password
            )
        )

        sessionManager.saveToken(response.token)
        sessionManager.saveUserName(response.name)
        sessionManager.saveRole(response.role)

        User(
            id = response.userId,
            name = response.name,
            email = email,
            role = response.role
        )
    }.recoverCatching { error ->
        if (error is AuthError.TokenExpired) {
            throw AuthError.InvalidCredentials
        } else {
            throw error
        }
    }
}