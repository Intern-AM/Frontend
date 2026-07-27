
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
            val eventIdToTitleMap = mutableMapOf<String, String>()

            eventRepository.getEvents().fold(
                onSuccess = { events ->
                    events.forEach { eventIdToTitleMap[it.id] = it.title }
                    events.filter {
                        it.status.equals("Cancelled", true)
                    }.map {
                        Notification(
                            id = it.id,
                            title = it.title,
                            message = "Event has been cancelled",
                            timestamp = it.startTime,
                            type = NotificationType.EVENT_CANCELLED,
                            eventId = it.id,
                            eventName = it.title
                        )
                    }.let { eventNotifications.addAll(it) }
                },
                onFailure = { if (errorMessage == null) errorMessage = it.message ?: "Failed to load events" }
            )

            campaignRepository.getCampaigns().fold(
                onSuccess = { campaigns ->
                    campaigns.mapNotNull { campaign ->
                        buildCampaignNotification(campaign, eventIdToTitleMap)
                    }.let { campaignNotifications.addAll(it) }
                },
                onFailure = { if (errorMessage == null) errorMessage = it.message ?: "Failed to load campaigns" }
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
            val eventIdToTitleMap = mutableMapOf<String, String>()

            eventRepository.getEvents().fold(
                onSuccess = { events ->
                    events.forEach { eventIdToTitleMap[it.id] = it.title }
                    events.filter {
                        it.status.equals("Cancelled", true)
                    }.map {
                        Notification(
                            id = it.id,
                            title = it.title,
                            message = "Event has been cancelled",
                            timestamp = it.startTime,
                            type = NotificationType.EVENT_CANCELLED,
                            eventId = it.id,
                            eventName = it.title
                        )
                    }.let { eventNotifications.addAll(it) }
                },
                onFailure = { if (errorMessage == null) errorMessage = it.message ?: "Failed to load events" }
            )

            campaignRepository.getCampaigns().fold(
                onSuccess = { campaigns ->
                    campaigns.mapNotNull { campaign ->
                        buildCampaignNotification(campaign, eventIdToTitleMap)
                    }.let { campaignNotifications.addAll(it) }
                },
                onFailure = { if (errorMessage == null) errorMessage = it.message ?: "Failed to load campaigns" }
            )

            notifications = (campaignNotifications + eventNotifications)
                .sortedByDescending { it.timestamp }
        }
    }

    private suspend fun buildCampaignNotification(
        campaign: com.speehive.speehiveaihub.models.Campaign,
        eventIdToTitleMap: Map<String, String>
    ): Notification? {
        val actionTime = sessionManager.getActionTimestamp(campaign.eventId)
        val eventName = eventIdToTitleMap[campaign.eventId]
        val displayTitle = eventName ?: "Campaign #${campaign.campaignId}"

        var postings: List<com.speehive.speehiveaihub.models.PlatformPosting> = emptyList()
        campaignRepository.getPlatformPostings(campaign.eventId).onSuccess {
            postings = it
        }
        val latestPlatformPostedAt = postings.mapNotNull { it.postedAt }.maxOrNull()
        val effectiveTimestamp = latestPlatformPostedAt
            ?: campaign.postedAt
            ?: actionTime
            ?: campaign.createdAt

        return when (campaign.status.lowercase()) {
            "generated" -> Notification(
                id = campaign.campaignId.toString(),
                title = displayTitle,
                message = "Campaign awaiting approval",
                timestamp = effectiveTimestamp,
                type = NotificationType.REVIEW_REQUIRED,
                eventId = campaign.eventId,
                eventName = eventName,
                platformPostings = postings
            )
            "approved" -> Notification(
                id = campaign.campaignId.toString(),
                title = displayTitle,
                message = "Campaign approved successfully",
                timestamp = effectiveTimestamp,
                type = NotificationType.APPROVED,
                eventId = campaign.eventId,
                eventName = eventName,
                platformPostings = postings
            )
            "rejected" -> Notification(
                id = campaign.campaignId.toString(),
                title = displayTitle,
                message = "Campaign rejected by reviewer",
                timestamp = effectiveTimestamp,
                type = NotificationType.REJECTED,
                eventId = campaign.eventId,
                eventName = eventName,
                platformPostings = postings
            )
            "posted" -> Notification(
                id = campaign.campaignId.toString(),
                title = displayTitle,
                message = if (postings.isNotEmpty()) {
                    val postedCount = postings.count { it.status.equals("Posted", true) }
                    "Posted to $postedCount of ${postings.size} platform(s)"
                } else {
                    "Posted to social media"
                },
                timestamp = effectiveTimestamp,
                type = NotificationType.PUBLISHED,
                eventId = campaign.eventId,
                eventName = eventName,
                platformPostings = postings
            )
            else -> null
        }
    }
}