package com.speehive.speehiveaihub.data

import android.content.Context
import com.speehive.speehiveaihub.utils.istZone
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

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

    fun saveActionTimestamp(eventId: String) {
        val now = OffsetDateTime.now(istZone).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        sharedPreferences.edit()
            .putString("action_${eventId}_time", now)
            .apply()
        evictOldTimestamps()
    }

    private fun evictOldTimestamps(limit: Int = 50) {
        val actionKeys = sharedPreferences.all.keys
            .filter { it.startsWith("action_") }
            .sortedBy { sharedPreferences.getString(it, null) }
        if (actionKeys.size > limit) {
            val toRemove = actionKeys.take(actionKeys.size - limit)
            val editor = sharedPreferences.edit()
            toRemove.forEach { editor.remove(it) }
            editor.apply()
        }
    }

    fun getActionTimestamp(eventId: String): String? {
        return sharedPreferences.getString("action_${eventId}_time", null)
    }

    fun clearSession() {
        sharedPreferences.edit()
            .remove("jwt_token")
            .remove("user_name")
            .remove("role")
            .remove("seen_notification_ids")
            .apply()
    }

    fun getSeenNotificationIds(): Set<String> {
        return sharedPreferences.getStringSet("seen_notification_ids", emptySet()) ?: emptySet()
    }

    fun saveSeenNotificationIds(ids: Set<String>) {
        sharedPreferences.edit()
            .putStringSet("seen_notification_ids", ids)
            .apply()
    }


}
