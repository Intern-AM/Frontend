package com.speehive.speehiveaihub.network

import com.google.gson.annotations.SerializedName

data class CampaignScheduleResponse(
    val eventId: String,
    val campaignStatus: String,
    val schdtimeLinkedIn: String?,
    val schdtimeInstagram: String?,
    val schdtimeTeams: String?,
    @SerializedName(value = "schdtimeWhatsApp", alternate = ["schdtimeWhatsapp"])
    val schdtimeWhatsapp: String?,
    val platforms: List<PlatformScheduleResponse>
)

data class PlatformScheduleResponse(
    val platform: String,
    val status: String,
    val postedAt: String?,
    val errorMessage: String?
)
