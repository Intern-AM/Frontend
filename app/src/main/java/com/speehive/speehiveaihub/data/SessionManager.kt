package com.speehive.speehiveaihub.data

import android.content.Context

class SessionManager(context: Context) {

    private val sharedPreferences =
        context.getSharedPreferences(
            "speehive_session",
            Context.MODE_PRIVATE
        )

    fun saveToken(token: String) {
        sharedPreferences.edit()
            .putString("jwt_token", token)
            .apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString(
            "jwt_token",
            null
        )
    }

    fun saveUserName(name: String) {
        sharedPreferences.edit()
            .putString("user_name", name)
            .apply()
    }

    fun getUserName(): String {
        return sharedPreferences.getString(
            "user_name",
            "User"
        ) ?: "User"
    }

    fun saveRole(role: String) {
        sharedPreferences.edit()
            .putString("role", role)
            .apply()
    }

    fun getRole(): String {
        return sharedPreferences.getString(
            "role",
            ""
        ) ?: ""
    }

    fun isLoggedIn(): Boolean {
        return !getToken().isNullOrEmpty()
    }

    fun clearSession() {
        sharedPreferences.edit()
            .remove("jwt_token")
            .remove("user_name")
            .remove("role")
            .apply()
    }


}
