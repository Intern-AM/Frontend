package com.speehive.speehiveaihub.repository

import com.speehive.speehiveaihub.models.SocialMediaCredential

interface SocialMediaCredentialRepository {
    suspend fun getCredentials(): Result<List<SocialMediaCredential>>
    suspend fun updateCredential(
        provider: String,
        accessToken: String,
        expiresAt: String?,
        isActive: Boolean
    ): Result<Unit>
}
