package com.speehive.speehiveaihub.network

data class LoginResponse(
    val message: String,
    val token: String,
    val userId: String,
    val name: String,
    val role: String
)