package com.speehive.speehiveaihub.repository

import android.net.Uri
import com.speehive.speehiveaihub.models.Campaign

import com.speehive.speehiveaihub.network.CampaignScheduleResponse
import com.speehive.speehiveaihub.network.UpdateScheduleRequest

interface CampaignRepository {

    suspend fun getCampaigns(): Result<List<Campaign>>

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

    suspend fun getCampaignSchedule(eventId: String): Result<CampaignScheduleResponse>

    suspend fun updateCampaignSchedule(eventId: String, request: UpdateScheduleRequest): Result<Unit>
}