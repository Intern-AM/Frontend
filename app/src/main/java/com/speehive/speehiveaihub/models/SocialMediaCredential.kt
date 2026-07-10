package com.speehive.speehiveaihub.models

data class SocialMediaCredential(
    val provider: String,
    val isActive: Boolean,
    val expiresAt: String?,
    val updatedAt: String?,
    val updatedBy: String?,
    val maskedToken: String
)
