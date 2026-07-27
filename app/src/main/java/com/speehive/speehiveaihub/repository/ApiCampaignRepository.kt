package com.speehive.speehiveaihub.repository

import android.content.Context
import android.net.Uri
import com.speehive.speehiveaihub.data.AuthManager
import com.speehive.speehiveaihub.data.SessionManager
import com.speehive.speehiveaihub.models.Campaign
import com.speehive.speehiveaihub.models.PlatformPosting
import com.speehive.speehiveaihub.network.ApprovalRequest
import com.speehive.speehiveaihub.network.EditCampaignRequest
import com.speehive.speehiveaihub.network.RetrofitClient
import com.speehive.speehiveaihub.network.safeApiCall
import com.speehive.speehiveaihub.network.toCampaign
import com.speehive.speehiveaihub.network.toResult
import com.speehive.speehiveaihub.utils.toMultipartBodyPart

import com.speehive.speehiveaihub.network.CampaignScheduleResponse
import com.speehive.speehiveaihub.network.UpdateScheduleRequest

class ApiCampaignRepository(
    sessionManager: SessionManager,
    authManager: AuthManager,
    private val context: Context
) : CampaignRepository {
    private val api =
        RetrofitClient.create(sessionManager, authManager)

    override suspend fun getCampaigns(): Result<List<Campaign>> = safeApiCall {
        api.getCampaigns().map { it.toCampaign() }
    }

    override suspend fun approveCampaign(
        eventId: String,
        comments: String
    ): Result<Unit> = api.approveCampaign(
        ApprovalRequest(
            eventId = eventId,
            comments = comments
        )
    ).toResult().map { }

    override suspend fun rejectCampaign(
        eventId: String,
        comments: String
    ): Result<Unit> = api.rejectCampaign(
        ApprovalRequest(
            eventId = eventId,
            comments = comments
        )
    ).toResult().map { }

    override suspend fun uploadCampaignImage(
        eventId: String,
        imageUri: Uri
    ): Result<String> {
        return imageUri.toMultipartBodyPart(context, "image", "campaign_upload_") { part ->
            safeApiCall {
                val response = api.uploadCampaignImage(eventId, part)
                if (response.imageUrl.isNotBlank()) {
                    RetrofitClient.getFormattedImageUrl(response.imageUrl) ?: ""
                } else {
                    throw Exception("Upload succeeded but no image URL returned")
                }
            }
        }
    }

    override suspend fun editCampaign(
        eventId: String,
        campaignPost: String,
        hashtags: String
    ): Result<Unit> = api.editCampaign(
        eventId,
        EditCampaignRequest(
            campaignPost = campaignPost,
            hashtags = hashtags
        )
    ).toResult()

    override suspend fun getCampaignSchedule(eventId: String): Result<CampaignScheduleResponse> = safeApiCall {
        api.getCampaignSchedule(eventId)
    }

    override suspend fun updatePlatformSchedule(
        eventId: String,
        platform: String,
        scheduledTime: String?
    ): Result<Unit> = api.updatePlatformSchedule(
        eventId = eventId,
        platform = platform,
        request = com.speehive.speehiveaihub.network.UpdatePlatformScheduleRequest(scheduledTime)
    ).toResult()

    override suspend fun getPlatformPostings(eventId: String): Result<List<PlatformPosting>> = safeApiCall {
        val response = api.getPlatformPostings(eventId)
        if (response.code() == 404 || response.body() == null) {
            emptyList()
        } else if (response.isSuccessful) {
            response.body()!!.map { dto ->
                PlatformPosting(
                    platform = dto.platform,
                    status = dto.status,
                    createdAt = dto.createdAt,
                    postedAt = dto.postedAt,
                    errorMessage = dto.errorMessage
                )
            }
        } else {
            emptyList()
        }
    }
}

