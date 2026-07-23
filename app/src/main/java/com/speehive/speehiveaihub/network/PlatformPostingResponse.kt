package com.speehive.speehiveaihub.network

data class PlatformPostingResponse(
    val platform: String,
    val status: String,
    val createdAt: String,
    val postedAt: String?,
    val errorMessage: String?
)
