package com.speehive.speehiveaihub.network

import com.google.gson.annotations.SerializedName

data class UpdatePlatformScheduleRequest(
    @SerializedName("scheduledTime")
    val scheduledTime: String?
)
