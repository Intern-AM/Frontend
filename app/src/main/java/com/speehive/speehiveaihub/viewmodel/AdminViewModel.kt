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

    init {
        loadUsers()
        loadAuditLogs()
    }

    fun loadUsers() {
        viewModelScope.launch {
            isLoading = true
            repository.getUsers().fold(
                onSuccess = { users = it },
                onFailure = { /* AuthManager handles 401/403, UI redirects */ }
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
            repository.createUser(name = name, email = email, password = password, role = role).fold(
                onSuccess = {
                    successMessage = "New ${role.lowercase()} created successfully"
                    loadUsers()
                },
                onFailure = { /* AuthManager handles 401/403 */ }
            )
        }
    }

    fun clearSuccessMessage() {
        successMessage = null
    }

    fun activateUser(id: String) {
        viewModelScope.launch {
            repository.activateUser(id).fold(
                onSuccess = { loadUsers() },
                onFailure = { /* AuthManager handles 401/403 */ }
            )
        }
    }

    fun loadAuditLogs() {
        viewModelScope.launch {
            auditRepository.getAuditLogs().fold(
                onSuccess = { auditLogs = it },
                onFailure = { /* AuthManager handles 401/403 */ }
            )
        }
    }

    fun deactivateUser(id: String) {
        viewModelScope.launch {
            repository.deactivateUser(id).fold(
                onSuccess = { loadUsers() },
                onFailure = { /* AuthManager handles 401/403 */ }
            )
        }
    }

    fun deleteUser(id: String) {
        viewModelScope.launch {
            repository.deleteUser(id).fold(
                onSuccess = { loadUsers() },
                onFailure = { /* AuthManager handles 401/403 */ }
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