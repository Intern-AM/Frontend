package com.speehive.speehiveaihub.repository

import android.net.Uri
import com.speehive.speehiveaihub.models.Campaign

interface CampaignRepository {

    suspend fun getCampaigns(): Result<List<Campaign>>

    suspend fun getCampaignById(id: String): Result<Campaign?>

    suspend fun approveCampaign(
        eventId: String,
        comments: String = ""
    ): Result<Unit>

    suspend fun rejectCampaign(
        eventId: String,
        comments: String
    ): Result<Unit>

    suspend fun uploadCampaignImage(eventId: String, imageUri: Uri): Result<String>

    suspend fun editCampaign(
        eventId: String,
        campaignPost: String,
        hashtags: String
    ): Result<Unit>
}