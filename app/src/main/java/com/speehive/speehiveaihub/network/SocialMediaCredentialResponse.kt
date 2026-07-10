package com.speehive.speehiveaihub.network

data class SocialMediaCredentialResponse(
    val provider: String,
    val isActive: Boolean,
    val expiresAt: String?,
    val updatedAt: String?,
    val updatedBy: String?,
    val maskedToken: String
)
