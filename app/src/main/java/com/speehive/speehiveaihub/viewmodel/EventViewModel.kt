package com.speehive.speehiveaihub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speehive.speehiveaihub.models.Campaign
import com.speehive.speehiveaihub.models.Event
import com.speehive.speehiveaihub.repository.CampaignRepository
import com.speehive.speehiveaihub.repository.EventRepository
import kotlinx.coroutines.launch

class EventViewModel(
    private val eventRepository: EventRepository,
    private val campaignRepository: CampaignRepository
) : ViewModel() {
    var events by mutableStateOf<List<Event>>(emptyList())
        private set
    var campaigns by mutableStateOf<List<Campaign>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var isProcessing by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            
            campaignRepository.getCampaigns().fold(
                onSuccess = { campaigns = it },
                onFailure = { errorMessage = it.message ?: "Failed to load campaigns" }
            )

            eventRepository.getEvents().fold(
                onSuccess = {
                    events = it
                        .distinctBy { it.id }
                        .sortedBy { it.startTime }
                },
                onFailure = { if (errorMessage == null) errorMessage = it.message ?: "Failed to load events" }
            )
            isLoading = false
        }
    }

    fun loadEventsSilently() {
        viewModelScope.launch {
            campaignRepository.getCampaigns().fold(
                onSuccess = { campaigns = it },
                onFailure = { if (errorMessage == null) errorMessage = it.message ?: "Failed to load campaigns" }
            )

            eventRepository.getEvents().fold(
                onSuccess = {
                    events = it
                        .distinctBy { it.id }
                        .sortedBy { it.startTime }
                },
                onFailure = { if (errorMessage == null) errorMessage = it.message ?: "Failed to load events" }
            )
        }
    }

    fun uploadEventImage(eventId: String, uri: android.net.Uri) {
        viewModelScope.launch {
            isProcessing = true
            errorMessage = null
            eventRepository.uploadDesignerImage(eventId, uri).fold(
                onSuccess = { loadEventsSilently() },
                onFailure = { errorMessage = it.message ?: "Failed to upload poster" }
            )
            isProcessing = false
        }
    }

    fun cancelEvent(id: String) {
        viewModelScope.launch {
            isProcessing = true
            errorMessage = null
            eventRepository.cancelEvent(id).fold(
                onSuccess = { loadEvents() },
                onFailure = { errorMessage = it.message ?: "Failed to cancel event" }
            )
            isProcessing = false
        }
    }

    fun restoreEvent(id: String) {
        viewModelScope.launch {
            isProcessing = true
            errorMessage = null
            eventRepository.restoreEvent(id).fold(
                onSuccess = { loadEvents() },
                onFailure = { errorMessage = it.message ?: "Failed to restore event" }
            )
            isProcessing = false
        }
    }
}
