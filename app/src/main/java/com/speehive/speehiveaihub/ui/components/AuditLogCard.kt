package com.speehive.speehiveaihub.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.speehive.speehiveaihub.models.AuditLog
import com.speehive.speehiveaihub.ui.theme.*
import com.speehive.speehiveaihub.utils.formatAuditDate

@Composable
fun AuditLogCard(log: AuditLog) {

    val actionColor =
        when (log.action) {
            "CREATE_USER" -> PulseBlue
            "ACTIVATE_USER" -> PulseGreen
            "DEACTIVATE_USER" -> PulseRed
            "APPROVE_POST" -> PulseGreen
            "REJECT_POST" -> PulseRed
            "CANCEL_EVENT" -> PulseRed
            "GOOGLE_CALENDAR_UPDATED" -> PulseBlue
            "CREATE_CAMPAIGN" -> PulseBlue
            "APPROVE_EVENT" -> PulseGreen
            "REJECT_EVENT" -> PulseRed
            "POST_CAMPAIGN" -> PulseGreen
            else -> TextPrimary
        }
    val actionTitle =
        when(log.action) {
            "CREATE_USER" -> "Created User"
            "ACTIVATE_USER" -> "Activated User"
            "DEACTIVATE_USER" -> "Deactivated User"
            "APPROVE_POST" -> "Approved Campaign"
            "REJECT_POST" -> "Rejected Campaign"
            "CANCEL_EVENT" -> "Cancelled Event"
            "GOOGLE_CALENDAR_UPDATED" -> "Calendar Updated"
            "CREATE_CAMPAIGN" -> "Created Campaign"
            "APPROVE_EVENT" -> "Approved Event"
            "REJECT_EVENT" -> "Rejected Event"
            "POST_CAMPAIGN" -> "Posted Campaign"
            else -> log.action.replace("_", " ").lowercase()
                .replaceFirstChar { it.uppercase() }
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        ),
        border = BorderStroke(
            1.dp,
            CardBorder
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = actionTitle,
                style = MaterialTheme.typography.titleMedium,
                color = actionColor
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = log.details,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = formatAuditDate(log.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
