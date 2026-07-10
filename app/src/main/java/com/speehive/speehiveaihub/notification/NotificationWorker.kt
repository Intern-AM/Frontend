package com.speehive.speehiveaihub.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.speehive.speehiveaihub.data.AuthManager
import com.speehive.speehiveaihub.data.SessionManager
import com.speehive.speehiveaihub.network.RetrofitClient
import com.speehive.speehiveaihub.network.SpeehiveApiService
import java.time.OffsetDateTime
import java.time.Duration

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val sessionManager = SessionManager(applicationContext)
        if (!sessionManager.isLoggedIn()) return Result.success()

        val authManager = AuthManager(sessionManager)
        val api: SpeehiveApiService = RetrofitClient.create(sessionManager, authManager)

        return try {
            val campaigns = api.getCampaigns()

            val seenIds = sessionManager.getSeenNotificationIds()
            val currentIds = mutableSetOf<String>()

            for (campaign in campaigns) {
                val id = campaign.campaignId.toString()
                val shouldNotify = when (campaign.status.lowercase()) {
                    "generated" -> id !in seenIds
                    "posted" -> id !in seenIds
                    else -> false
                }

                if (shouldNotify) {
                    val title = if (campaign.status.lowercase() == "generated") {
                        "Review Required"
                    } else {
                        "Campaign Published"
                    }
                    val message = if (campaign.status.lowercase() == "generated") {
                        "Campaign awaiting approval"
                    } else {
                        "Posted to LinkedIn"
                    }
                    NotificationHelper.showNotification(
                        applicationContext,
                        id.hashCode(),
                        title,
                        message
                    )
                }

                if (campaign.status.lowercase() == "generated" ||
                    campaign.status.lowercase() == "posted"
                ) {
                    currentIds.add(id)
                }
            }

            sessionManager.saveSeenNotificationIds(currentIds)

            // Check for expiring social media credentials if user is Admin
            if (sessionManager.getRole().equals("admin", ignoreCase = true)) {
                try {
                    val credentials = api.getSocialMediaCredentials()
                    for (cred in credentials) {
                        if (cred.isActive && !cred.expiresAt.isNullOrBlank()) {
                            val expiry = OffsetDateTime.parse(cred.expiresAt)
                            val now = OffsetDateTime.now()
                            val daysRemaining = Duration.between(now, expiry).toDays()
                            if (daysRemaining in 0..7) {
                                // Check if we already notified today to avoid spamming
                                val lastNotified = sessionManager.getActionTimestamp("expiry_${cred.provider}")
                                var shouldNotifyToday = true
                                if (lastNotified != null) {
                                    try {
                                        val lastDate = OffsetDateTime.parse(lastNotified).toLocalDate()
                                        val todayDate = OffsetDateTime.now().toLocalDate()
                                        if (lastDate == todayDate) {
                                            shouldNotifyToday = false
                                        }
                                    } catch (e: Exception) {
                                        // Ignore parse error and allow notifying
                                    }
                                }

                                if (shouldNotifyToday) {
                                    NotificationHelper.showNotification(
                                        context = applicationContext,
                                        id = cred.provider.hashCode(),
                                        title = "${cred.provider} Token Expiring",
                                        message = "Token expires in $daysRemaining days. Tap to update."
                                    )
                                    sessionManager.saveActionTimestamp("expiry_${cred.provider}")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Suppress API/mapping errors here so they don't break the main campaign polling
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
