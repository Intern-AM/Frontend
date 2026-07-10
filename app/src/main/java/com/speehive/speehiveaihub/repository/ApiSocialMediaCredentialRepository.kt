package com.speehive.speehiveaihub.repository

import com.speehive.speehiveaihub.data.AuthManager
import com.speehive.speehiveaihub.data.SessionManager
import com.speehive.speehiveaihub.models.SocialMediaCredential
import com.speehive.speehiveaihub.network.RetrofitClient
import com.speehive.speehiveaihub.network.UpdateSocialMediaCredentialRequest
import com.speehive.speehiveaihub.network.safeApiCall
import com.speehive.speehiveaihub.network.toResult

class ApiSocialMediaCredentialRepository(
    sessionManager: SessionManager,
    authManager: AuthManager
) : SocialMediaCredentialRepository {

    private val api = RetrofitClient.create(sessionManager, authManager)

    override suspend fun getCredentials(): Result<List<SocialMediaCredential>> = safeApiCall {
        api.getSocialMediaCredentials().map {
            SocialMediaCredential(
                provider = it.provider,
                isActive = it.isActive,
                expiresAt = it.expiresAt,
                updatedAt = it.updatedAt,
                updatedBy = it.updatedBy,
                maskedToken = it.maskedToken
            )
        }
    }

    override suspend fun updateCredential(
        provider: String,
        accessToken: String,
        expiresAt: String?,
        isActive: Boolean
    ): Result<Unit> = safeApiCall {
        api.updateSocialMediaCredential(
            provider = provider,
            request = UpdateSocialMediaCredentialRequest(
                accessToken = accessToken,
                expiresAt = expiresAt,
                isActive = isActive
            )
        ).toResult()
    }
}
