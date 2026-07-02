package com.speehive.speehiveaihub.network

data class CampaignResponse(
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