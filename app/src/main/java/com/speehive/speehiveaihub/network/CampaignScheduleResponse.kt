package com.speehive.speehiveaihub.network

import com.google.gson.annotations.SerializedName

data class CampaignScheduleResponse(
    val eventId: String,
    val campaignStatus: String,
    @SerializedName(value = "schdtimeLinkedIn", alternate = ["SchdtimeLinkedIn", "schdtimeLinkedin", "SchdTimeLinkedIn", "schdTimeLinkedIn", "SchdTimeLinkedin", "schdTimeLinkedin"])
    val schdtimeLinkedIn: String?,
    @SerializedName(value = "schdtimeInstagram", alternate = ["SchdtimeInstagram", "schdtimeinstagram", "SchdTimeInstagram", "schdTimeInstagram"])
    val schdtimeInstagram: String?,
    @SerializedName(value = "schdtimeTeams", alternate = ["SchdtimeTeams", "schdtimeteams", "SchdTimeTeams", "schdTimeTeams"])
    val schdtimeTeams: String?,
    @SerializedName(value = "schdtimeWhatsapp", alternate = ["schdtimeWhatsApp", "SchdtimeWhatsApp", "SchdtimeWhatsapp", "SchdTimeWhatsapp", "schdTimeWhatsapp", "SchdTimeWhatsApp", "schdTimeWhatsApp"])
    val schdtimeWhatsapp: String?,
    val platforms: List<PlatformScheduleResponse>
)


data class PlatformScheduleResponse(
    val platform: String,
    val status: String,
    val postedAt: String?,
    val errorMessage: String?
)

