package com.speehive.speehiveaihub.network

import com.speehive.speehiveaihub.models.Campaign

fun CampaignResponse.toCampaign(): Campaign {
    return Campaign(
        campaignId = campaignId,
        eventId = eventId,
        campaignPost = campaignPost,
        hashtags = hashtags,
        cta = cta,
        imagePrompt = imagePrompt,
        imageUrl = RetrofitClient.getFormattedImageUrl(imageUrl),
        status = status,
        createdAt = createdAt,
        linkedInPostId = linkedInPostId,
        postedAt = postedAt
    )
}