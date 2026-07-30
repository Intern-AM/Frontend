package com.speehive.speehiveaihub.repository

import android.net.Uri
import com.speehive.speehiveaihub.models.AdminUser
import com.speehive.speehiveaihub.models.BulkValidationResponse
import com.speehive.speehiveaihub.models.BulkImportResponse

interface AdminRepository {

    suspend fun getUsers(): Result<List<AdminUser>>

    suspend fun createUser(
        name: String,
        email: String,
        password: String,
        role: String
    ): Result<Unit>

    suspend fun activateUser(
        id: String
    ): Result<Unit>

    suspend fun deactivateUser(
        id: String
    ): Result<Unit>

    suspend fun validateBulkEvents(
        fileUri: Uri
    ): Result<BulkValidationResponse>

    suspend fun importBulkEvents(
        fileUri: Uri
    ): Result<BulkImportResponse>
}