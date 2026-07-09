package com.speehive.speehiveaihub.repository

import com.speehive.speehiveaihub.data.AuthManager
import com.speehive.speehiveaihub.data.SessionManager
import com.speehive.speehiveaihub.models.AdminUser
import com.speehive.speehiveaihub.network.CreateUserRequest
import com.speehive.speehiveaihub.network.RetrofitClient
import com.speehive.speehiveaihub.network.safeApiCall
import com.speehive.speehiveaihub.network.toResult

class ApiAdminRepository(
    sessionManager: SessionManager,
    authManager: AuthManager
) : AdminRepository {

    private val api =
        RetrofitClient.create(sessionManager, authManager)

    override suspend fun getUsers(): Result<List<AdminUser>> = safeApiCall {
        api.getUsers().map {
            AdminUser(
                id = it.id,
                name = it.name,
                email = it.email,
                role = it.role,
                isActive = it.isActive,
                createdAt = it.createdAt
            )
        }
    }

    override suspend fun createUser(
        name: String,
        email: String,
        password: String,
        role: String
    ): Result<Unit> = safeApiCall {
        api.createUser(
            CreateUserRequest(
                name = name,
                email = email,
                password = password,
                role = role
            )
        ).toResult()
    }

    override suspend fun activateUser(id: String): Result<Unit> = safeApiCall {
        api.activateUser(id).toResult()
    }

    override suspend fun deactivateUser(id: String): Result<Unit> = safeApiCall {
        api.deactivateUser(id).toResult()
    }
}