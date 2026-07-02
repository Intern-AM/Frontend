package com.speehive.speehiveaihub.repository

import com.speehive.speehiveaihub.data.AuthManager
import com.speehive.speehiveaihub.data.SessionManager
import com.speehive.speehiveaihub.models.AuditLog
import com.speehive.speehiveaihub.network.AuthError
import com.speehive.speehiveaihub.network.RetrofitClient
import retrofit2.HttpException

class ApiAuditRepository(
    sessionManager: SessionManager,
    authManager: AuthManager
) : AuditRepository {

    private val api =
        RetrofitClient.create(sessionManager, authManager)

    override suspend fun getAuditLogs(): Result<List<AuditLog>> {
        return try {
            val response = api.getAuditLogs().map {
                AuditLog(
                    id = it.id,
                    userId = it.userId,
                    action = it.action,
                    details = it.details,
                    createdAt = it.createdAt
                )
            }
            Result.success(response)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Result.failure(AuthError.fromCode(e.code(), errorBody))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}