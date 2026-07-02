package com.speehive.speehiveaihub.repository

import com.speehive.speehiveaihub.models.AuditLog

interface AuditRepository {

    suspend fun getAuditLogs(): Result<List<AuditLog>>
}