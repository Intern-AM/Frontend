package com.speehive.speehiveaihub.models

data class AdminUser(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val isActive: Boolean,
    val createdAt: String
)