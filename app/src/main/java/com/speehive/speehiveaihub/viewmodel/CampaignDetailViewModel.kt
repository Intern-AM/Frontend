package com.speehive.speehiveaihub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speehive.speehiveaihub.models.Campaign
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
            campaign?.let {
                campaignRepository.approveCampaign(eventId = it.eventId).fold(
                    onSuccess = { onSuccess() },
                    onFailure = { /* AuthManager handles 401/403 */ }
                )
            }
        }
    }

    fun rejectCampaign(comments: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            campaign?.let {
                campaignRepository.rejectCampaign(
                    eventId = it.eventId,
                    comments = comments
                ).fold(
                    onSuccess = { onSuccess() },
                    onFailure = { /* AuthManager handles 401/403 */ }
                )
            }
        }
    }

    fun updateStatus(newStatus: String) {
        campaign = campaign?.copy(status = newStatus)
    }
}
