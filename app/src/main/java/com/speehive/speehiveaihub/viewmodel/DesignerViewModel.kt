package com.speehive.speehiveaihub.viewmodel

import android.net.Uri
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

class DesignerViewModel(
    private val eventRepository: EventRepository,
    private val campaignRepository: CampaignRepository
) : ViewModel() {

    var events by mutableStateOf<List<Event>>(emptyList())
        private set
    var campaigns by mutableStateOf<List<Campaign>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var uploadSuccess by mutableStateOf<String?>(null)
        private set
    var uploadingId by mutableStateOf<String?>(null)
        private set

    val filteredEvents: List<Event>
        get() {
            val campaignEventIds = campaigns.map { it.eventId }.toSet()
            return events.filter {
                !it.status.trim().equals("Started", ignoreCase = true) &&
                !it.status.trim().equals("Cancelled", ignoreCase = true) &&
                it.id !in campaignEventIds
            }
        }

    val filteredCampaigns: List<Campaign>
        get() = campaigns.filter {
            it.status.trim().equals("Generated", ignoreCase = true)
        }

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            isLoading = true
            error = null
            eventRepository.getEvents().fold(
                onSuccess = { events = it },
                onFailure = { error = it.message ?: "Failed to load events" }
            )
            campaignRepository.getCampaigns().fold(
                onSuccess = { campaigns = it },
                onFailure = { if (error == null) error = it.message ?: "Failed to load campaigns" }
            )
            isLoading = false
        }
    }

    fun uploadEventImage(eventId: String, uri: Uri) {
        viewModelScope.launch {
            uploadingId = eventId
            error = null
            uploadSuccess = null
            eventRepository.uploadDesignerImage(eventId, uri).fold(
                onSuccess = {
                    uploadSuccess = "Event poster uploaded"
                    loadData()
                },
                onFailure = { error = it.message ?: "Upload failed" }
            )
            uploadingId = null
        }
    }

    fun uploadCampaignImage(eventId: String, uri: Uri) {
        viewModelScope.launch {
            uploadingId = eventId
            error = null
            uploadSuccess = null
            campaignRepository.uploadCampaignImage(eventId, uri).fold(
                onSuccess = {
                    uploadSuccess = "Campaign poster uploaded"
                    loadData()
                },
                onFailure = { error = it.message ?: "Upload failed" }
            )
            uploadingId = null
        }
    }

    fun editCampaign(eventId: String, campaignPost: String, hashtags: String) {
        viewModelScope.launch {
            uploadingId = eventId
            error = null
            uploadSuccess = null
            campaignRepository.editCampaign(eventId, campaignPost, hashtags).fold(
                onSuccess = {
                    uploadSuccess = "Campaign updated successfully"
                    loadData()
                },
                onFailure = { error = it.message ?: "Update failed" }
            )
            uploadingId = null
        }
    }
}
