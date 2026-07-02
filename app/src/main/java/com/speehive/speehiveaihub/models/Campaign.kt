package com.speehive.speehiveaihub.models

data class Campaign(
    val campaignId: Int,
    val eventId: String,
    val campaignPost: String,
    val hashtags: String,
    val cta: String,
    val imagePrompt: String,
    val imageUrl: String?,
    val status: String,
    val createdAt: String,
    val linkedInPostId: String?,
    val postedAt: String?
)