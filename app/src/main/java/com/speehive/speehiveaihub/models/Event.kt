package com.speehive.speehiveaihub.models

data class Event(
    val id: String,
    val title: String,
    val description: String,
    val startTime: String,
    val endTime: String,
    val location: String,
    val eventType: String,
    val status: String,
    val approvalDeadline: String?,
    val designerImageUrl: String? = null
)