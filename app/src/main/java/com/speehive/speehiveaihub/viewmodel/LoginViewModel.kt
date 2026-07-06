package com.speehive.speehiveaihub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speehive.speehiveaihub.network.AuthError
import com.speehive.speehiveaihub.repository.UserRepository
import kotlinx.coroutines.launch
import com.speehive.speehiveaihub.models.User

class LoginViewModel(private val repository: UserRepository) : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
        private set
    var loginError by mutableStateOf<String?>(null)
        private set
    var isLoggedIn by mutableStateOf(false)
        private set
    var currentUser by mutableStateOf<User?>(null)
        private set

    fun onLoginClick() {
        viewModelScope.launch {
            isLoading = true
            loginError = null

            if (email.isBlank() || password.isBlank()) {
                loginError = "Please enter email and password"
                isLoading = false
                return@launch
            }

            val result = repository.login(email, password)

            result.fold(
                onSuccess = { user ->
                    if (user != null) {
                        currentUser = user
                        isLoggedIn = true
                    } else {
                        loginError = "Invalid email or password"
                    }
                },
                onFailure = { error ->
                    loginError = when (error) {
                        is AuthError.InvalidCredentials -> "Invalid email or password"
                        is AuthError.TokenExpired -> "Session expired. Please try again."
                        is AuthError.Unauthorized -> "You don't have permission."
                        else -> error.message ?: "Login failed"
                    }
                }
            )
            isLoading = false
        }
    }
}
