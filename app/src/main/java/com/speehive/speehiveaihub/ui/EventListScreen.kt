package com.speehive.speehiveaihub.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import com.speehive.speehiveaihub.models.Event
import com.speehive.speehiveaihub.ui.components.statusColor
import com.speehive.speehiveaihub.ui.theme.*
import com.speehive.speehiveaihub.viewmodel.EventViewModel
import com.speehive.speehiveaihub.ui.components.BottomNavBar
import com.speehive.speehiveaihub.ui.components.BottomNavItem
import com.speehive.speehiveaihub.utils.formatEventDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    viewModel: EventViewModel,
    onNavigateHome: () -> Unit,
    onNavigateCampaigns: () -> Unit,
    onNavigateNotifications: () -> Unit
) {
    var isRefreshing by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selected = BottomNavItem.EVENTS,

                onHomeClick = onNavigateHome,

                onEventsClick = {},

                onCampaignsClick = onNavigateCampaigns,

                onNotificationsClick = onNavigateNotifications
            )
        },
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("MONITORING", style = MaterialTheme.typography.labelSmall)
                        Text("Events", style = MaterialTheme.typography.displayLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.loadEvents()
                isRefreshing = false
            },
            state = rememberPullToRefreshState()
        ) {
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {

            viewModel.errorMessage?.let { message ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PulseRedLight
                    ),
                    border = BorderStroke(1.dp, CardBorder)
                ) {

                    Text(
                        text = message,
                        color = TextPrimary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (viewModel.isLoading && viewModel.events.isEmpty()) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = PulseGreen
                    )
                }

            } else {

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 8.dp,
                        bottom = 80.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    items(
                        items = viewModel.events.distinctBy { it.id },
                        key = { it.id }
                    ) { event ->

                        FullEventCard(
                            event = event,
                            onReject = {
                                viewModel.cancelEvent(
                                    event.id
                                )
                            },
                            isProcessing = viewModel.isProcessing
                        )
                }
            }
            }
        }
    }
}
}
@Composable
fun FullEventCard(
    event: Event,
    onReject: () -> Unit,
    isProcessing: Boolean = false
){

    val statusColor = statusColor(event.status)

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
            modifier = Modifier.padding(20.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {

                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = PulseBlue.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = event.eventType,
                            color = PulseBlue,
                            maxLines = 1,
                            modifier = Modifier.padding(
                                horizontal = 6.dp,
                                vertical = 2.dp
                            )
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = event.description,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    Icons.Default.Event,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )

                Spacer(
                    modifier = Modifier.width(6.dp)
                )

                Text(
                    text = formatEventDate(event.startTime),
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Spacer(
                modifier = Modifier.height(16.dp)
            )


            if (event.status.equals("Pending", true)) {

                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PulseRed,
                        disabledContainerColor = PulseRed.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isProcessing
                ) {
                    Text(
                        text = "Reject Event",
                        color = AppBackground
                    )

                }
                Spacer(
                    modifier = Modifier.height(20.dp)
                )
            }

            Row {
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = event.status.uppercase(),
                        color = statusColor,
                        maxLines = 1,
                        modifier = Modifier.padding(
                            horizontal = 6.dp,
                            vertical = 2.dp
                        )
                    )
                }
            }
        }
    }
}
