package com.speehive.speehiveaihub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speehive.speehiveaihub.models.Event
import com.speehive.speehiveaihub.repository.EventRepository
import kotlinx.coroutines.launch

class EventViewModel(private val repository: EventRepository) : ViewModel() {
    var events by mutableStateOf<List<Event>>(emptyList())
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
            repository.getEvents().fold(
                onSuccess = {
                    events = it
                        .distinctBy { it.id }
                        .sortedBy { it.startTime }
                },
                onFailure = { errorMessage = it.message ?: "Failed to load events" }
            )
            isLoading = false
        }
    }

    fun loadEventsSilently() {
        viewModelScope.launch {
            repository.getEvents().fold(
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
            repository.uploadDesignerImage(eventId, uri).fold(
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
            repository.cancelEvent(id).fold(
                onSuccess = { loadEvents() },
                onFailure = { errorMessage = it.message ?: "Failed to cancel event" }
            )
            isProcessing = false
        }
    }
}
