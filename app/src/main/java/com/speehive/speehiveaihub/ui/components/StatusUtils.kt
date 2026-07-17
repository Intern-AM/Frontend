package com.speehive.speehiveaihub.ui.components

import androidx.compose.ui.graphics.Color
import com.speehive.speehiveaihub.ui.theme.*

fun statusColor(status: String): Color = when (status.lowercase()) {
    "approved", "active", "started" -> PulseGreen
    "pending", "pending approval", "draft", "not configured", "posting", "not scheduled" -> PulseAmber
    "rejected", "cancelled", "inactive", "failed" -> PulseRed

    "generated", "posted" -> PulseBlue
    "scheduled" -> PulsePurple
    else -> TextMuted
}
