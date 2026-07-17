package com.speehive.speehiveaihub.network

import com.google.gson.annotations.SerializedName

data class UpdateScheduleRequest(
    val schdtimeLinkedIn: String?,
    val schdtimeInstagram: String?,
    val schdtimeTeams: String?,
    @SerializedName("schdtimeWhatsApp")
    val schdtimeWhatsapp: String?
)
