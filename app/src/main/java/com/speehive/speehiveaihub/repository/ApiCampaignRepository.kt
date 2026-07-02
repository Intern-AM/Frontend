package com.speehive.speehiveaihub.repository

import android.content.Context
import android.net.Uri
import com.speehive.speehiveaihub.data.AuthManager
import com.speehive.speehiveaihub.data.SessionManager
import com.speehive.speehiveaihub.models.Campaign
import com.speehive.speehiveaihub.network.ApprovalRequest
import com.speehive.speehiveaihub.network.AuthError
import com.speehive.speehiveaihub.network.EditCampaignRequest
import com.speehive.speehiveaihub.network.RetrofitClient
import com.speehive.speehiveaihub.network.toCampaign
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File

class ApiCampaignRepository(
    sessionManager: SessionManager,
    authManager: AuthManager,
    private val context: Context
) : CampaignRepository {
    private val api =
        RetrofitClient.create(sessionManager, authManager)

    override suspend fun getCampaigns(): Result<List<Campaign>> {
        return try {
            val response = api.getCampaigns()
            Result.success(response.map { it.toCampaign() })
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Result.failure(AuthError.fromCode(e.code(), errorBody))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun approveCampaign(
        eventId: String,
        comments: String
    ): Result<Unit> {
        return try {
            val response = api.approveCampaign(
                ApprovalRequest(
                    eventId = eventId,
                    comments = comments
                )
            )
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

    override suspend fun rejectCampaign(
        eventId: String,
        comments: String
    ): Result<Unit> {
        return try {
            val response = api.rejectCampaign(
                ApprovalRequest(
                    eventId = eventId,
                    comments = comments
                )
            )
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

    override suspend fun getCampaignById(id: String): Result<Campaign?> {
        return try {
            val campaigns = api.getCampaigns().map { it.toCampaign() }
            Result.success(campaigns.find { it.campaignId.toString() == id })
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Result.failure(AuthError.fromCode(e.code(), errorBody))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadCampaignImage(
        eventId: String,
        imageUri: Uri
    ): Result<String> {
        return try {
            val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"
            val allowedTypes = listOf("image/png", "image/jpeg")
            if (mimeType !in allowedTypes) {
                return Result.failure(Exception("Only PNG and JPEG images are allowed"))
            }

            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: return Result.failure(Exception("Cannot open image"))

            val tempFile = File(context.cacheDir, "campaign_upload_${System.currentTimeMillis()}.jpg")
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()

            val maxSize = 10L * 1024 * 1024
            if (tempFile.length() > maxSize) {
                tempFile.delete()
                return Result.failure(Exception("File size must be under 10 MB"))
            }

            val requestBody = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("image", tempFile.name, requestBody)

            val response = api.uploadCampaignImage(eventId, part)

            tempFile.delete()

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
        }
    }

    override suspend fun editCampaign(
        eventId: String,
        campaignPost: String,
        hashtags: String
    ): Result<Unit> {
        return try {
            val response = api.editCampaign(
                eventId,
                EditCampaignRequest(
                    campaignPost = campaignPost,
                    hashtags = hashtags
                )
            )
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
}