package com.speehive.speehiveaihub.utils

import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val istZone = ZoneId.of("Asia/Kolkata")

fun formatDate(dateString: String): String {
    return try {
        val date = OffsetDateTime.parse(dateString).atZoneSameInstant(istZone)
        date.format(
            DateTimeFormatter.ofPattern("dd MMM yyyy • hh:mm a")
        )
    } catch (e: Exception) {
        dateString
    }
}

fun formatEventDate(dateString: String): String = formatDate(dateString)
fun formatAuditDate(dateString: String): String = formatDate(dateString)
fun formatCampaignDate(dateString: String): String = formatDate(dateString)

fun isEventUpcoming(endTime: String): Boolean {
    return try {
        val date = OffsetDateTime.parse(endTime)
        date.toInstant().isAfter(java.time.Instant.now())
    } catch (e: Exception) {
        true
    }
}
