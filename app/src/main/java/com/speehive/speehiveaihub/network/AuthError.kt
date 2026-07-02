package com.speehive.speehiveaihub.network

sealed class AuthError(val errorMessage: String, val statusCode: Int) : Exception(errorMessage) {
    data object TokenExpired : AuthError("Session expired. Please log in again.", 401)
    data object Unauthorized : AuthError("You don't have permission to perform this action.", 403)
    data object NoToken : AuthError("No authentication token found.", 401)
    data object InvalidCredentials : AuthError("Invalid email or password.", 401)
    data class Unknown(val code: Int, val detail: String? = null) :
        AuthError(detail ?: "Request failed ($code)", code)

    companion object {
        fun fromCode(code: Int, detail: String? = null): AuthError = when (code) {
            401 -> TokenExpired
            403 -> Unauthorized
            else -> Unknown(code, detail)
        }
    }
}
