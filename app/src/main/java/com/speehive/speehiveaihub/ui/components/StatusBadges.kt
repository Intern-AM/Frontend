package com.speehive.speehiveaihub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FigmaStatusBadge(status: String) {
    val color = statusColor(status)

    Box(
        modifier = Modifier
            .background(
                color.copy(alpha = 0.15f),
                RoundedCornerShape(100.dp)
            )
            .padding(
                horizontal = 14.dp,
                vertical = 5.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }

}


@Composable
fun PriorityBadge(priority: String) {
    val color = when (priority.uppercase()) {
        "HIGH" -> com.speehive.speehiveaihub.ui.theme.PulseRed
        "MEDIUM" -> com.speehive.speehiveaihub.ui.theme.PulseAmber
        else -> com.speehive.speehiveaihub.ui.theme.PulseGreen
    }
    
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(100.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = priority.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
