package com.speehive.speehiveaihub.models

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val type: NotificationType,
    val eventId: String? = null,
    val eventName: String? = null,
    val platformPostings: List<PlatformPosting> = emptyList()
)
enum class NotificationType {
    REVIEW_REQUIRED,
    APPROVED,
    REJECTED,
    PUBLISHED,
    EVENT_CANCELLED
}
