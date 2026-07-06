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

class DashboardViewModel(
    private val campaignRepository: CampaignRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    var campaigns by mutableStateOf<List<Campaign>>(emptyList())

    var events by mutableStateOf<List<Event>>(emptyList())

    var isLoading by mutableStateOf(false)

    var errorMessage by mutableStateOf<String?>(null)

    val pendingCount: Int
        get() = campaigns.count {
            it.status.equals(
                "Generated",
                ignoreCase = true
            )
        }

    val approvedCount: Int
        get() = campaigns.count {
            it.status.equals(
                "Approved",
                ignoreCase = true
            )
        }

    val activeCount: Int
        get() = campaigns.count {
            it.status.equals(
                "Posted",
                ignoreCase = true
            )
        }
    val reviewQueue: List<Campaign>
        get() = campaigns.filter {
            it.status.equals(
                "Generated",
                ignoreCase = true
            )
        }
    init {
        loadCampaigns()
        loadEvents()
    }

    private fun loadCampaigns() {
        viewModelScope.launch {

            isLoading = true
            errorMessage = null

            campaignRepository.getCampaigns().fold(
                onSuccess = { campaigns = it },
                onFailure = { errorMessage = it.message ?: "Failed to load campaigns" }
            )

            isLoading = false
        }
    }

    private fun loadEvents() {
        viewModelScope.launch {
            eventRepository.getEvents().fold(
                onSuccess = {
                    events = it
                        .distinctBy { it.id }
                        .filter {
                            !it.status.equals("Cancelled", ignoreCase = true)
                        }
                        .sortedBy { it.startTime }
                },
                onFailure = { if (errorMessage == null) errorMessage = it.message ?: "Failed to load events" }
            )
        }
    }
}