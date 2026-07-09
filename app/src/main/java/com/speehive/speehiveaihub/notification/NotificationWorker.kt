package com.speehive.speehiveaihub.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.speehive.speehiveaihub.data.AuthManager
import com.speehive.speehiveaihub.data.SessionManager
import com.speehive.speehiveaihub.network.RetrofitClient
import com.speehive.speehiveaihub.network.SpeehiveApiService

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
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
