package com.speehive.speehiveaihub.repository

import android.content.Context
import android.net.Uri
import com.speehive.speehiveaihub.data.AuthManager
import com.speehive.speehiveaihub.data.SessionManager
import com.speehive.speehiveaihub.models.Event
import com.speehive.speehiveaihub.network.AuthError
import com.speehive.speehiveaihub.network.RetrofitClient
import com.speehive.speehiveaihub.network.toEvent
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File

class ApiEventRepository(
    sessionManager: SessionManager,
    authManager: AuthManager,
    private val context: Context
) : EventRepository {

    private val api =
        RetrofitClient.create(sessionManager, authManager)

    override suspend fun getEvents(): Result<List<Event>> {
        return try {
            val response = api.getEvents()
            Result.success(response.map { it.toEvent() })
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Result.failure(AuthError.fromCode(e.code(), errorBody))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelEvent(id: String): Result<Unit> {
        return try {
            val response = api.cancelEvent(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(AuthError.fromCode(response.code(), errorBody))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Result.failure(AuthError.fromCode(e.code(), errorBody))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadDesignerImage(
        eventId: String,
        imageUri: Uri
    ): Result<String> {
        val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        return try {
            val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"
            val allowedTypes = listOf("image/png", "image/jpeg")
            if (mimeType !in allowedTypes) {
                return Result.failure(Exception("Only PNG and JPEG images are allowed"))
            }

            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: return Result.failure(Exception("Cannot open image"))

            inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val maxSize = 10L * 1024 * 1024
            if (tempFile.length() > maxSize) {
                return Result.failure(Exception("File size must be under 10 MB"))
            }

            val requestBody = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("image", tempFile.name, requestBody)

            val response = api.uploadDesignerImage(eventId, part)

            if (response.imageUrl.isNotBlank()) {
                Result.success(response.imageUrl)
            } else {
                Result.failure(Exception("Upload succeeded but no image URL returned"))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Result.failure(AuthError.fromCode(e.code(), errorBody))
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            tempFile.delete()
        }
    }
}