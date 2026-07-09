package com.speehive.speehiveaihub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speehive.speehiveaihub.data.SessionManager
import com.speehive.speehiveaihub.models.Campaign
import com.speehive.speehiveaihub.network.AuthError
import com.speehive.speehiveaihub.repository.CampaignRepository
import com.speehive.speehiveaihub.repository.EventRepository
import kotlinx.coroutines.launch

class CampaignDetailViewModel(
    private val campaignRepository: CampaignRepository,
    private val eventRepository: EventRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    var campaign by mutableStateOf<Campaign?>(null)
        private set
    var isLoading by mutableStateOf(true)
        private set
    var eventTitle by mutableStateOf("Loading...")
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var isProcessing by mutableStateOf(false)
        private set

    fun loadCampaign(id: String) {
        viewModelScope.launch {
            isLoading = true

            campaignRepository.getCampaigns().fold(
                onSuccess = { campaigns ->
                    val found = campaigns.find { it.campaignId.toString() == id }
                    campaign = found
                    found?.let { currentCampaign ->
                        eventRepository.getEvents().fold(
                            onSuccess = {
                                eventTitle = it.find { it.id == currentCampaign.eventId }?.title ?: "Unknown Event"
                            },
                            onFailure = { eventTitle = "Unknown Event" }
                        )
                    }
                },
                onFailure = {
                    campaign = null
                    errorMessage = "Campaign not found"
                }
            )

            isLoading = false
        }
    }

    fun approveCampaign(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isProcessing = true
            errorMessage = null
            campaign?.let { currentCampaign ->
                campaignRepository.approveCampaign(eventId = currentCampaign.eventId).fold(
                    onSuccess = {
                        sessionManager.saveActionTimestamp(currentCampaign.eventId)
                        onSuccess()
                    },
                    onFailure = { error ->
                        errorMessage = when (error) {
                            is AuthError.Unauthorized -> "You don't have permission to approve campaigns"
                            is AuthError.TokenExpired -> "Session expired. Please log in again"
                            else -> error.message ?: "Failed to approve campaign"
                        }
                    }
                )
            } ?: run {
                errorMessage = "Campaign not found"
            }
            isProcessing = false
        }
    }

    fun rejectCampaign(comments: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isProcessing = true
            errorMessage = null
            campaign?.let { currentCampaign ->
                campaignRepository.rejectCampaign(
                    eventId = currentCampaign.eventId,
                    comments = comments
                ).fold(
                    onSuccess = {
                        sessionManager.saveActionTimestamp(currentCampaign.eventId)
                        onSuccess()
                    },
                    onFailure = { error ->
                        errorMessage = when (error) {
                            is AuthError.Unauthorized -> "You don't have permission to reject campaigns"
                            is AuthError.TokenExpired -> "Session expired. Please log in again"
                            else -> error.message ?: "Failed to reject campaign"
                        }
                    }
                )
            } ?: run {
                errorMessage = "Campaign not found"
            }
            isProcessing = false
        }
    }

    fun clearError() {
        errorMessage = null
    }
}
