package com.speehive.speehiveaihub.network

data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    val passwordHash: String,
    val role: String,
    val isActive: Boolean,
    val createdAt: String
)