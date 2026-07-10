package com.speehive.speehiveaihub.network

data class UpdateSocialMediaCredentialRequest(
    val accessToken: String,
    val expiresAt: String?,
    val isActive: Boolean
)
