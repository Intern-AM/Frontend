package com.speehive.speehiveaihub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speehive.speehiveaihub.models.Campaign
import com.speehive.speehiveaihub.network.AuthError
import com.speehive.speehiveaihub.repository.CampaignRepository
import com.speehive.speehiveaihub.repository.EventRepository
import kotlinx.coroutines.launch

class CampaignDetailViewModel(
    private val campaignRepository: CampaignRepository,
    private val eventRepository: EventRepository
) : ViewModel() {
    var campaign by mutableStateOf<Campaign?>(null)
    var isLoading by mutableStateOf(true)
    var eventTitle by mutableStateOf("Loading...")
    var errorMessage by mutableStateOf<String?>(null)
    var isProcessing by mutableStateOf(false)

    fun loadCampaign(id: String) {
        viewModelScope.launch {
            isLoading = true

            campaignRepository.getCampaignById(id).fold(
                onSuccess = { found ->
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
                onFailure = { campaign = null }
            )

            isLoading = false
        }
    }

    fun approveCampaign(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isProcessing = true
            errorMessage = null
            campaign?.let {
                campaignRepository.approveCampaign(eventId = it.eventId).fold(
                    onSuccess = { onSuccess() },
                    onFailure = { error ->
                        errorMessage = when (error) {
                            is AuthError.Unauthorized -> "You don't have permission to approve campaigns"
                            is AuthError.TokenExpired -> "Session expired. Please log in again"
                            else -> error.message ?: "Failed to approve campaign"
                        }
                    }
                )
            }
            isProcessing = false
        }
    }

    fun rejectCampaign(comments: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isProcessing = true
            errorMessage = null
            campaign?.let {
                campaignRepository.rejectCampaign(
                    eventId = it.eventId,
                    comments = comments
                ).fold(
                    onSuccess = { onSuccess() },
                    onFailure = { error ->
                        errorMessage = when (error) {
                            is AuthError.Unauthorized -> "You don't have permission to reject campaigns"
                            is AuthError.TokenExpired -> "Session expired. Please log in again"
                            else -> error.message ?: "Failed to reject campaign"
                        }
                    }
                )
            }
            isProcessing = false
        }
    }

    fun clearError() {
        errorMessage = null
    }
}
