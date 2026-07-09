package com.speehive.speehiveaihub.repository

import com.speehive.speehiveaihub.data.AuthManager
import com.speehive.speehiveaihub.data.SessionManager
import com.speehive.speehiveaihub.models.AuditLog
import com.speehive.speehiveaihub.network.RetrofitClient
import com.speehive.speehiveaihub.network.safeApiCall

class ApiAuditRepository(
    sessionManager: SessionManager,
    authManager: AuthManager
) : AuditRepository {

    private val api =
        RetrofitClient.create(sessionManager, authManager)

    override suspend fun getAuditLogs(): Result<List<AuditLog>> = safeApiCall {
        api.getAuditLogs().map {
            AuditLog(
                id = it.id,
                userId = it.userId,
                action = it.action,
                details = it.details,
                createdAt = it.createdAt
            )
        }
    }
}