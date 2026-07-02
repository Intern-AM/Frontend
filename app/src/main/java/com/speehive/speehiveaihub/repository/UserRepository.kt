package com.speehive.speehiveaihub.repository

import com.speehive.speehiveaihub.models.User

interface UserRepository {
    suspend fun login(email: String, password: String): Result<User?>
}
