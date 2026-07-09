package com.speehive.speehiveaihub.repository

import android.content.Context
import android.net.Uri
import com.speehive.speehiveaihub.data.AuthManager
import com.speehive.speehiveaihub.data.SessionManager
import com.speehive.speehiveaihub.models.Campaign
import com.speehive.speehiveaihub.network.ApprovalRequest
import com.speehive.speehiveaihub.network.EditCampaignRequest
import com.speehive.speehiveaihub.network.RetrofitClient
import com.speehive.speehiveaihub.network.safeApiCall
import com.speehive.speehiveaihub.network.toCampaign
import com.speehive.speehiveaihub.network.toResult
import com.speehive.speehiveaihub.utils.toMultipartBodyPart

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
    ): Result<Unit> = safeApiCall {
        api.approveCampaign(
            ApprovalRequest(
                eventId = eventId,
                comments = comments
            )
        ).toResult().map { }
    }

    override suspend fun rejectCampaign(
        eventId: String,
        comments: String
    ): Result<Unit> = safeApiCall {
        api.rejectCampaign(
            ApprovalRequest(
                eventId = eventId,
                comments = comments
            )
        ).toResult().map { }
    }

    override suspend fun uploadCampaignImage(
        eventId: String,
        imageUri: Uri
    ): Result<String> {
        return imageUri.toMultipartBodyPart(context, "image", "campaign_upload_") { part ->
            safeApiCall {
                val response = api.uploadCampaignImage(eventId, part)
                if (response.imageUrl.isNotBlank()) {
                    response.imageUrl
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
    ): Result<Unit> = safeApiCall {
        api.editCampaign(
            eventId,
            EditCampaignRequest(
                campaignPost = campaignPost,
                hashtags = hashtags
            )
        ).toResult()
    }
}
