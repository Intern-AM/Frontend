package com.speehive.speehiveaihub.models

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val type: NotificationType
)
enum class NotificationType {
    REVIEW_REQUIRED,
    APPROVED,
    REJECTED,
    PUBLISHED,
    EVENT_CANCELLED
}
