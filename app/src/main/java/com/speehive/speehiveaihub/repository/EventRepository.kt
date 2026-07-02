package com.speehive.speehiveaihub.repository

import android.net.Uri
import com.speehive.speehiveaihub.models.Event

interface EventRepository {

    suspend fun getEvents(): Result<List<Event>>

    suspend fun cancelEvent(id: String): Result<Unit>

    suspend fun uploadDesignerImage(eventId: String, imageUri: Uri): Result<String>
}