package com.speehive.speehiveaihub.repository

import com.speehive.speehiveaihub.data.AuthManager
import com.speehive.speehiveaihub.data.SessionManager
import com.speehive.speehiveaihub.models.AdminUser
import com.speehive.speehiveaihub.network.AuthError
import com.speehive.speehiveaihub.network.CreateUserRequest
import com.speehive.speehiveaihub.network.RetrofitClient
import retrofit2.HttpException

class ApiAdminRepository(
    sessionManager: SessionManager,
    authManager: AuthManager
) : AdminRepository {

    private val api =
        RetrofitClient.create(sessionManager, authManager)

    override suspend fun getUsers(): Result<List<AdminUser>> {
        return try {
            val response = api.getUsers().map {
                AdminUser(
                    id = it.id,
                    name = it.name,
                    email = it.email,
                    role = it.role,
                    isActive = it.isActive,
                    createdAt = it.createdAt
                )
            }
            Result.success(response)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Result.failure(AuthError.fromCode(e.code(), errorBody))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createUser(
        name: String,
        email: String,
        password: String,
        role: String
    ): Result<Unit> {
        return try {
            val response = api.createUser(
                CreateUserRequest(
                    name = name,
                    email = email,
                    password = password,
                    role = role
                )
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(AuthError.fromCode(response.code(), errorBody))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Result.failure(AuthError.fromCode(e.code(), errorBody))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun activateUser(id: String): Result<Unit> {
        return try {
            val response = api.activateUser(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(AuthError.fromCode(response.code(), errorBody))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Result.failure(AuthError.fromCode(e.code(), errorBody))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deactivateUser(id: String): Result<Unit> {
        return try {
            val response = api.deactivateUser(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(AuthError.fromCode(response.code(), errorBody))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Result.failure(AuthError.fromCode(e.code(), errorBody))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(id: String): Result<Unit> {
        return try {
            val response = api.deleteUser(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(AuthError.fromCode(response.code(), errorBody))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Result.failure(AuthError.fromCode(e.code(), errorBody))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}