package com.speehive.speehiveaihub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speehive.speehiveaihub.models.AdminUser
import com.speehive.speehiveaihub.repository.AdminRepository
import kotlinx.coroutines.launch
import com.speehive.speehiveaihub.models.AuditLog
import com.speehive.speehiveaihub.repository.AuditRepository

class AdminViewModel(
    private val repository: AdminRepository,
    private val auditRepository: AuditRepository
) : ViewModel() {

    var users by mutableStateOf<List<AdminUser>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var auditLogs by mutableStateOf<List<AuditLog>>(emptyList())
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadUsers()
        loadAuditLogs()
    }

    fun loadUsers() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            repository.getUsers().fold(
                onSuccess = { users = it },
                onFailure = { errorMessage = it.message ?: "Failed to load users" }
            )
            isLoading = false
        }
    }

    fun createUser(
        name: String,
        email: String,
        password: String,
        role: String
    ) {
        viewModelScope.launch {
            errorMessage = null
            repository.createUser(name = name, email = email, password = password, role = role).fold(
                onSuccess = {
                    successMessage = "New ${role.lowercase()} created successfully"
                    loadUsers()
                },
                onFailure = { errorMessage = it.message ?: "Failed to create user" }
            )
        }
    }

    fun clearSuccessMessage() {
        successMessage = null
    }

    fun clearError() {
        errorMessage = null
    }

    fun activateUser(id: String) {
        viewModelScope.launch {
            errorMessage = null
            repository.activateUser(id).fold(
                onSuccess = { loadUsers() },
                onFailure = { errorMessage = it.message ?: "Failed to activate user" }
            )
        }
    }

    fun loadAuditLogs() {
        viewModelScope.launch {
            auditRepository.getAuditLogs().fold(
                onSuccess = { auditLogs = it },
                onFailure = { if (errorMessage == null) errorMessage = it.message ?: "Failed to load audit logs" }
            )
        }
    }

    fun deactivateUser(id: String) {
        viewModelScope.launch {
            errorMessage = null
            repository.deactivateUser(id).fold(
                onSuccess = { loadUsers() },
                onFailure = { errorMessage = it.message ?: "Failed to deactivate user" }
            )
        }
    }

    val totalUsers: Int
        get() = users.count { !it.role.equals("Admin", ignoreCase = true) }

    val activeUsers: Int
        get() = users.count { it.isActive && !it.role.equals("Admin", ignoreCase = true) }

    val inactiveUsers: Int
        get() = users.count { !it.isActive && !it.role.equals("Admin", ignoreCase = true) }

    val designerUsers: Int
        get() = users.count { it.role.equals("Designer", ignoreCase = true) }

    val adminUsers: Int
        get() = users.count {
            it.role.equals("Admin", ignoreCase = true)
        }
}