package com.speehive.speehiveaihub.repository

import android.content.Context
import android.net.Uri
import com.speehive.speehiveaihub.data.AuthManager
import com.speehive.speehiveaihub.data.SessionManager
import com.speehive.speehiveaihub.models.Event
import com.speehive.speehiveaihub.network.RetrofitClient
import com.speehive.speehiveaihub.network.safeApiCall
import com.speehive.speehiveaihub.network.toEvent
import com.speehive.speehiveaihub.network.toResult
import com.speehive.speehiveaihub.utils.toMultipartBodyPart

class ApiEventRepository(
    sessionManager: SessionManager,
    authManager: AuthManager,
    private val context: Context
) : EventRepository {

    private val api =
        RetrofitClient.create(sessionManager, authManager)

    override suspend fun getEvents(): Result<List<Event>> = safeApiCall {
        api.getEvents().map { it.toEvent() }
    }

    override suspend fun cancelEvent(id: String): Result<Unit> = safeApiCall {
        api.cancelEvent(id).toResult()
    }

    override suspend fun uploadDesignerImage(
        eventId: String,
        imageUri: Uri
    ): Result<String> {
        return imageUri.toMultipartBodyPart(context, "image", "upload_") { part ->
            safeApiCall {
                val response = api.uploadDesignerImage(eventId, part)
                if (response.imageUrl.isNotBlank()) {
                    response.imageUrl
                } else {
                    throw Exception("Upload succeeded but no image URL returned")
                }
            }
        }
    }
}