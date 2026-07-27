package com.speehive.speehiveaihub.models

data class PlatformPosting(
    val platform: String,
    val status: String,
    val createdAt: String,
    val postedAt: String?,
    val errorMessage: String?
)
