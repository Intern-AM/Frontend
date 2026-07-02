package com.speehive.speehiveaihub.models

data class AuditLog(
    val id: String,
    val userId: String,
    val action: String,
    val details: String,
    val createdAt: String
)