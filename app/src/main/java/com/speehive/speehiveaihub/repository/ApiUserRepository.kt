package com.speehive.speehiveaihub.repository

import com.speehive.speehiveaihub.data.AuthManager
import com.speehive.speehiveaihub.data.SessionManager
import com.speehive.speehiveaihub.models.User
import com.speehive.speehiveaihub.network.AuthError
import com.speehive.speehiveaihub.network.LoginRequest
import com.speehive.speehiveaihub.network.RetrofitClient
import retrofit2.HttpException

class ApiUserRepository(
    private val sessionManager: SessionManager,
    private val authManager: AuthManager
) : UserRepository {

    override suspend fun login(
        email: String,
        password: String
    ): Result<User?> {
        return try {
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

            Result.success(
                User(
                    id = response.userId,
                    name = response.name,
                    email = email,
                    role = response.role
                )
            )
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            when (e.code()) {
                401 -> Result.failure(AuthError.InvalidCredentials)
                403 -> Result.failure(AuthError.Unauthorized)
                else -> Result.failure(AuthError.fromCode(e.code(), errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}