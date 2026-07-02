package com.speehive.speehiveaihub.repository

import com.speehive.speehiveaihub.models.AdminUser

interface AdminRepository {

    suspend fun getUsers(): Result<List<AdminUser>>

    suspend fun createUser(
        name: String,
        email: String,
        password: String,
        role: String
    ): Result<Unit>

    suspend fun activateUser(
        id: String
    ): Result<Unit>

    suspend fun deactivateUser(
        id: String
    ): Result<Unit>

    suspend fun deleteUser(
        id: String
    ): Result<Unit>
}