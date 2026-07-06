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
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

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

    fun cancelEvent(id: String) {
        viewModelScope.launch {
            errorMessage = null
            repository.cancelEvent(id).fold(
                onSuccess = { loadEvents() },
                onFailure = { errorMessage = it.message ?: "Failed to cancel event" }
            )
        }
    }
}
