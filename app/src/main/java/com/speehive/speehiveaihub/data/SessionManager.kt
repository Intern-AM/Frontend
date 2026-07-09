package com.speehive.speehiveaihub.data

import android.content.Context
import androidx.core.content.edit
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
        sharedPreferences.edit {
            putString("jwt_token", token)
        }
    }

    fun getToken(): String? {
        return sharedPreferences.getString(
            "jwt_token",
            null
        )
    }

    fun saveUserName(name: String) {
        sharedPreferences.edit {
            putString("user_name", name)
        }
    }

    fun getUserName(): String {
        return sharedPreferences.getString(
            "user_name",
            "User"
        ) ?: "User"
    }

    fun saveRole(role: String) {
        sharedPreferences.edit {
            putString("role", role)
        }
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
        sharedPreferences.edit {
            putString("action_${eventId}_time", now)
        }
        evictOldTimestamps()
    }

    private fun evictOldTimestamps(limit: Int = 50) {
        val actionKeys = sharedPreferences.all.keys
            .filter { it.startsWith("action_") }
            .sortedBy { sharedPreferences.getString(it, null) }
        if (actionKeys.size > limit) {
            val toRemove = actionKeys.take(actionKeys.size - limit)
            sharedPreferences.edit {
                toRemove.forEach { remove(it) }
            }
        }
    }

    fun getActionTimestamp(eventId: String): String? {
        return sharedPreferences.getString("action_${eventId}_time", null)
    }

    fun clearSession() {
        sharedPreferences.edit {
            remove("jwt_token")
            remove("user_name")
            remove("role")
            remove("seen_notification_ids")
        }
    }

    fun getSeenNotificationIds(): Set<String> {
        return sharedPreferences.getStringSet("seen_notification_ids", emptySet()) ?: emptySet()
    }

    fun saveSeenNotificationIds(ids: Set<String>) {
        sharedPreferences.edit {
            putStringSet("seen_notification_ids", ids)
        }
    }
}
