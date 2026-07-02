package com.speehive.speehiveaihub.network

data class AuditLogResponse(
    val id: String,
    val userId: String,
    val action: String,
    val details: String,
    val createdAt: String
)