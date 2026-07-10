package com.speehive.speehiveaihub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speehive.speehiveaihub.models.AdminUser
import com.speehive.speehiveaihub.models.AuditLog
import com.speehive.speehiveaihub.models.SocialMediaCredential
import com.speehive.speehiveaihub.repository.AdminRepository
import com.speehive.speehiveaihub.repository.AuditRepository
import com.speehive.speehiveaihub.repository.SocialMediaCredentialRepository
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.Duration

class AdminViewModel(
    private val repository: AdminRepository,
    private val auditRepository: AuditRepository,
    private val credentialRepository: SocialMediaCredentialRepository
) : ViewModel() {

    var users by mutableStateOf<List<AdminUser>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var auditLogs by mutableStateOf<List<AuditLog>>(emptyList())
        private set

    var credentials by mutableStateOf<List<SocialMediaCredential>>(emptyList())
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isProcessing by mutableStateOf(false)
        private set

    init {
        loadUsers()
        loadAuditLogs()
        loadCredentials()
    }

    fun refresh() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            repository.getUsers().fold(
                onSuccess = { users = it },
                onFailure = { if (errorMessage == null) errorMessage = it.message ?: "Failed to load users" }
            )

            auditRepository.getAuditLogs().fold(
                onSuccess = { auditLogs = it },
                onFailure = { if (errorMessage == null) errorMessage = it.message ?: "Failed to load audit logs" }
            )

            credentialRepository.getCredentials().fold(
                onSuccess = { responseList ->
                    val finalList = responseList.toMutableList()
                    val providers = responseList.map { it.provider.lowercase() }.toSet()
                    if (!providers.contains("instagram")) {
                        finalList.add(SocialMediaCredential(provider = "Instagram", isActive = true, expiresAt = null, updatedAt = null, updatedBy = null, maskedToken = ""))
                    }
                    if (!providers.contains("linkedin")) {
                        finalList.add(SocialMediaCredential(provider = "LinkedIn", isActive = true, expiresAt = null, updatedAt = null, updatedBy = null, maskedToken = ""))
                    }
                    credentials = finalList
                },
                onFailure = {
                    credentials = listOf(
                        SocialMediaCredential(provider = "Instagram", isActive = true, expiresAt = null, updatedAt = null, updatedBy = null, maskedToken = ""),
                        SocialMediaCredential(provider = "LinkedIn", isActive = true, expiresAt = null, updatedAt = null, updatedBy = null, maskedToken = "")
                    )
                }
            )

            isLoading = false
        }
    }

    fun refreshSilently() {
        viewModelScope.launch {
            repository.getUsers().fold(
                onSuccess = { users = it },
                onFailure = { if (errorMessage == null) errorMessage = it.message ?: "Failed to load users" }
            )
            auditRepository.getAuditLogs().fold(
                onSuccess = { auditLogs = it },
                onFailure = { if (errorMessage == null) errorMessage = it.message ?: "Failed to load audit logs" }
            )
            credentialRepository.getCredentials().fold(
                onSuccess = { responseList ->
                    val finalList = responseList.toMutableList()
                    val providers = responseList.map { it.provider.lowercase() }.toSet()
                    if (!providers.contains("instagram")) {
                        finalList.add(SocialMediaCredential(provider = "Instagram", isActive = true, expiresAt = null, updatedAt = null, updatedBy = null, maskedToken = ""))
                    }
                    if (!providers.contains("linkedin")) {
                        finalList.add(SocialMediaCredential(provider = "LinkedIn", isActive = true, expiresAt = null, updatedAt = null, updatedBy = null, maskedToken = ""))
                    }
                    credentials = finalList
                },
                onFailure = {
                    credentials = listOf(
                        SocialMediaCredential(provider = "Instagram", isActive = true, expiresAt = null, updatedAt = null, updatedBy = null, maskedToken = ""),
                        SocialMediaCredential(provider = "LinkedIn", isActive = true, expiresAt = null, updatedAt = null, updatedBy = null, maskedToken = "")
                    )
                }
            )
        }
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

    fun loadCredentials() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            credentialRepository.getCredentials().fold(
                onSuccess = { responseList ->
                    val finalList = responseList.toMutableList()
                    val providers = responseList.map { it.provider.lowercase() }.toSet()
                    
                    if (!providers.contains("instagram")) {
                        finalList.add(
                            SocialMediaCredential(
                                provider = "Instagram",
                                isActive = true,
                                expiresAt = null,
                                updatedAt = null,
                                updatedBy = null,
                                maskedToken = ""
                            )
                        )
                    }
                    
                    if (!providers.contains("linkedin")) {
                        finalList.add(
                            SocialMediaCredential(
                                provider = "LinkedIn",
                                isActive = true,
                                expiresAt = null,
                                updatedAt = null,
                                updatedBy = null,
                                maskedToken = ""
                            )
                        )
                    }
                    
                    credentials = finalList
                },
                onFailure = { err ->
                    errorMessage = err.message ?: "Failed to load credentials"
                    credentials = listOf(
                        SocialMediaCredential(
                            provider = "Instagram",
                            isActive = true,
                            expiresAt = null,
                            updatedAt = null,
                            updatedBy = null,
                            maskedToken = ""
                        ),
                        SocialMediaCredential(
                            provider = "LinkedIn",
                            isActive = true,
                            expiresAt = null,
                            updatedAt = null,
                            updatedBy = null,
                            maskedToken = ""
                        )
                    )
                }
            )
            isLoading = false
        }
    }

    fun loadCredentialsSilently() {
        viewModelScope.launch {
            credentialRepository.getCredentials().fold(
                onSuccess = { responseList ->
                    val finalList = responseList.toMutableList()
                    val providers = responseList.map { it.provider.lowercase() }.toSet()
                    if (!providers.contains("instagram")) {
                        finalList.add(SocialMediaCredential(provider = "Instagram", isActive = true, expiresAt = null, updatedAt = null, updatedBy = null, maskedToken = ""))
                    }
                    if (!providers.contains("linkedin")) {
                        finalList.add(SocialMediaCredential(provider = "LinkedIn", isActive = true, expiresAt = null, updatedAt = null, updatedBy = null, maskedToken = ""))
                    }
                    credentials = finalList
                },
                onFailure = {
                    credentials = listOf(
                        SocialMediaCredential(provider = "Instagram", isActive = true, expiresAt = null, updatedAt = null, updatedBy = null, maskedToken = ""),
                        SocialMediaCredential(provider = "LinkedIn", isActive = true, expiresAt = null, updatedAt = null, updatedBy = null, maskedToken = "")
                    )
                }
            )
        }
    }

    fun updateCredential(
        provider: String,
        accessToken: String,
        expiresAt: String?,
        isActive: Boolean
    ) {
        viewModelScope.launch {
            isProcessing = true
            errorMessage = null
            credentialRepository.updateCredential(
                provider = provider,
                accessToken = accessToken,
                expiresAt = expiresAt,
                isActive = isActive
            ).fold(
                onSuccess = {
                    successMessage = "$provider API credentials updated successfully"
                    loadCredentials()
                },
                onFailure = {
                    successMessage = null
                    errorMessage = it.message ?: "Failed to update $provider credentials"
                }
            )
            isProcessing = false
        }
    }

    fun getExpiringCredentials(): List<SocialMediaCredential> {
        return credentials.filter { cred ->
            if (cred.isActive && !cred.expiresAt.isNullOrBlank()) {
                try {
                    val expiry = OffsetDateTime.parse(cred.expiresAt)
                    val now = OffsetDateTime.now()
                    val daysRemaining = Duration.between(now, expiry).toDays()
                    daysRemaining in 0..7
                } catch (e: Exception) {
                    false
                }
            } else {
                false
            }
        }
    }

    fun createUser(
        name: String,
        email: String,
        password: String,
        role: String
    ) {
        viewModelScope.launch {
            isProcessing = true
            errorMessage = null
            repository.createUser(name = name, email = email, password = password, role = role).fold(
                onSuccess = {
                    successMessage = "New ${role.lowercase()} created successfully"
                    loadUsers()
                },
                onFailure = { errorMessage = it.message ?: "Failed to create user" }
            )
            isProcessing = false
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
            isProcessing = true
            errorMessage = null
            repository.activateUser(id).fold(
                onSuccess = { loadUsers() },
                onFailure = { errorMessage = it.message ?: "Failed to activate user" }
            )
            isProcessing = false
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

    fun refreshAuditLogs() {
        viewModelScope.launch {
            isLoading = true
            auditRepository.getAuditLogs().fold(
                onSuccess = { auditLogs = it },
                onFailure = { if (errorMessage == null) errorMessage = it.message ?: "Failed to load audit logs" }
            )
            isLoading = false
        }
    }

    fun refreshAuditLogsSilently() = loadAuditLogs()

    fun deactivateUser(id: String) {
        viewModelScope.launch {
            isProcessing = true
            errorMessage = null
            repository.deactivateUser(id).fold(
                onSuccess = { loadUsers() },
                onFailure = { errorMessage = it.message ?: "Failed to deactivate user" }
            )
            isProcessing = false
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