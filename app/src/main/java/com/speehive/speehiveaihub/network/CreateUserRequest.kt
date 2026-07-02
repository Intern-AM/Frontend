package com.speehive.speehiveaihub.network

data class CreateUserRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String
)