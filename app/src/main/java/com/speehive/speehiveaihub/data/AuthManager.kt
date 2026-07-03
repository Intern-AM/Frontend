package com.speehive.speehiveaihub.data

import com.speehive.speehiveaihub.network.AuthError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthManager(private val sessionManager: SessionManager) {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Authenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun onAuthError(error: AuthError) {
        _authState.value = AuthState.Unauthenticated(error)
    }

    fun logout() {
        sessionManager.clearSession()
        _authState.value = AuthState.Unauthenticated(AuthError.NoToken)
    }
}

sealed class AuthState {
    data object Authenticated : AuthState()
    data class Unauthenticated(val error: AuthError? = null) : AuthState()
}
