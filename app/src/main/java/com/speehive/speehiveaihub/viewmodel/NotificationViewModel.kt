
package com.speehive.speehiveaihub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.speehive.speehiveaihub.data.SessionManager
import com.speehive.speehiveaihub.models.Notification
import com.speehive.speehiveaihub.models.NotificationType

import com.speehive.speehiveaihub.repository.CampaignRepository
import com.speehive.speehiveaihub.repository.EventRepository

import kotlinx.coroutines.launch


class NotificationViewModel(
    private val campaignRepository: CampaignRepository,
    private val eventRepository: EventRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    var notifications by mutableStateOf<List<Notification>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadNotifications()
    }

    fun loadNotifications() {

        viewModelScope.launch {

            isLoading = true
            errorMessage = null

            val campaignNotifications = mutableListOf<Notification>()
            val eventNotifications = mutableListOf<Notification>()

            campaignRepository.getCampaigns().fold(
                onSuccess = { campaigns ->
                    campaigns.mapNotNull { campaign ->
                        when (campaign.status.lowercase()) {
                            "generated" -> Notification(
                                id = campaign.campaignId.toString(),
                                title = "Review Required",
                                message = "Campaign awaiting approval",
                                timestamp = campaign.createdAt,
                                type = NotificationType.REVIEW_REQUIRED
                            )
                            "approved" -> Notification(
                                id = campaign.campaignId.toString(),
                                title = "Campaign Approved",
                                message = "Campaign approved successfully",
                                timestamp = sessionManager.getActionTimestamp(campaign.eventId) ?: campaign.createdAt,
                                type = NotificationType.APPROVED
                            )
                            "rejected" -> Notification(
                                id = campaign.campaignId.toString(),
                                title = "Campaign Rejected",
                                message = "Campaign rejected by reviewer",
                                timestamp = sessionManager.getActionTimestamp(campaign.eventId) ?: campaign.createdAt,
                                type = NotificationType.REJECTED
                            )
                            "posted" -> Notification(
                                id = campaign.campaignId.toString(),
                                title = "Campaign Published",
                                message = "Posted to LinkedIn",
                                timestamp = campaign.createdAt,
                                type = NotificationType.PUBLISHED
                            )
                            else -> null
                        }
                    }?.let { campaignNotifications.addAll(it) }
                },
                onFailure = { if (errorMessage == null) errorMessage = it.message ?: "Failed to load campaigns" }
            )

            eventRepository.getEvents().fold(
                onSuccess = { events ->
                    events.filter {
                        it.status.equals("Cancelled", true)
                    }.map {
                        Notification(
                            id = it.id,
                            title = "Event Cancelled",
                            message = it.title,
                            timestamp = it.startTime,
                            type = NotificationType.EVENT_CANCELLED
                        )
                    }.let { eventNotifications.addAll(it) }
                },
                onFailure = { if (errorMessage == null) errorMessage = it.message ?: "Failed to load events" }
            )

            notifications = (campaignNotifications + eventNotifications)
                .sortedByDescending { it.timestamp }

            isLoading = false
        }
    }

    fun loadNotificationsSilently() {
        viewModelScope.launch {
            val campaignNotifications = mutableListOf<Notification>()
            val eventNotifications = mutableListOf<Notification>()

            campaignRepository.getCampaigns().fold(
                onSuccess = { campaigns ->
                    campaigns.mapNotNull { campaign ->
                        when (campaign.status.lowercase()) {
                            "generated" -> Notification(
                                id = campaign.campaignId.toString(),
                                title = "Review Required",
                                message = "Campaign awaiting approval",
                                timestamp = campaign.createdAt,
                                type = NotificationType.REVIEW_REQUIRED
                            )
                            "approved" -> Notification(
                                id = campaign.campaignId.toString(),
                                title = "Campaign Approved",
                                message = "Campaign approved successfully",
                                timestamp = sessionManager.getActionTimestamp(campaign.eventId) ?: campaign.createdAt,
                                type = NotificationType.APPROVED
                            )
                            "rejected" -> Notification(
                                id = campaign.campaignId.toString(),
                                title = "Campaign Rejected",
                                message = "Campaign rejected by reviewer",
                                timestamp = sessionManager.getActionTimestamp(campaign.eventId) ?: campaign.createdAt,
                                type = NotificationType.REJECTED
                            )
                            "posted" -> Notification(
                                id = campaign.campaignId.toString(),
                                title = "Campaign Published",
                                message = "Posted to LinkedIn",
                                timestamp = campaign.createdAt,
                                type = NotificationType.PUBLISHED
                            )
                            else -> null
                        }
                    }?.let { campaignNotifications.addAll(it) }
                },
                onFailure = { if (errorMessage == null) errorMessage = it.message ?: "Failed to load campaigns" }
            )

            eventRepository.getEvents().fold(
                onSuccess = { events ->
                    events.filter {
                        it.status.equals("Cancelled", true)
                    }.map {
                        Notification(
                            id = it.id,
                            title = "Event Cancelled",
                            message = it.title,
                            timestamp = it.startTime,
                            type = NotificationType.EVENT_CANCELLED
                        )
                    }.let { eventNotifications.addAll(it) }
                },
                onFailure = { if (errorMessage == null) errorMessage = it.message ?: "Failed to load events" }
            )

            notifications = (campaignNotifications + eventNotifications)
                .sortedByDescending { it.timestamp }
        }
    }
}