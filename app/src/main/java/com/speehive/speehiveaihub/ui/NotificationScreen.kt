package com.speehive.speehiveaihub.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.speehive.speehiveaihub.models.Notification
import com.speehive.speehiveaihub.models.NotificationType
import com.speehive.speehiveaihub.ui.theme.*
import com.speehive.speehiveaihub.viewmodel.NotificationViewModel
import com.speehive.speehiveaihub.ui.components.BottomNavBar
import com.speehive.speehiveaihub.ui.components.BottomNavItem
import com.speehive.speehiveaihub.utils.formatAuditDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel,
    onNavigateHome: () -> Unit,
    onNavigateEvents: () -> Unit,
    onNavigateCampaigns: () -> Unit
) {
    Scaffold(bottomBar = {
        BottomNavBar(
            selected = BottomNavItem.NOTIFICATIONS,

            onHomeClick = onNavigateHome,

            onEventsClick = onNavigateEvents,

            onCampaignsClick = onNavigateCampaigns,

            onNotificationsClick = {}
        )
    },
        containerColor = PureBlack,
        topBar = {
            TopAppBar(
                title = { Column {

                    Text(
                        "ACTIVITY",
                        style = MaterialTheme.typography.labelSmall
                    )

                    Text(
                        "Notifications",
                        style = MaterialTheme.typography.displayLarge
                    )
                } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PureBlack)
            )
        }
    ) { paddingValues ->
        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TextPrimary, strokeWidth = 1.dp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(viewModel.notifications) { notification ->
                    NotificationCard(notification)
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notification: Notification) {
    val accentColor = when (notification.type) {

    NotificationType.REVIEW_REQUIRED ->
        PulseAmber

    NotificationType.APPROVED ->
        PulseGreen

    NotificationType.REJECTED ->
        PulseRed

    NotificationType.PUBLISHED ->
        PulseBlue

    NotificationType.EVENT_CANCELLED ->
        PulsePurple
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
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = notification.title, 
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = formatAuditDate(
                        notification.timestamp
                    ),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Surface(
                color = accentColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = notification.type.name
                        .replace("_", " "),
                    color = accentColor,
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 4.dp
                    )
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )
        }
    }
}
