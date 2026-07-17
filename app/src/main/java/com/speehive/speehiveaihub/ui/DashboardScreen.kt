package com.speehive.speehiveaihub.ui
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.speehive.speehiveaihub.models.Campaign
import com.speehive.speehiveaihub.ui.components.*
import com.speehive.speehiveaihub.ui.theme.*
import com.speehive.speehiveaihub.viewmodel.DashboardViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.speehive.speehiveaihub.utils.istZone
import com.speehive.speehiveaihub.utils.formatCampaignDate
import com.speehive.speehiveaihub.utils.formatEventDate
import com.speehive.speehiveaihub.utils.isEventUpcoming

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    userName: String,
    onNavigateToEvents: () -> Unit,
    onNavigateToCampaigns: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToCampaignDetail: (String) -> Unit,
    onLogout: () -> Unit,
    isAdmin: Boolean = false,
    onNavigateToAdmin: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var showMenu by remember {
    mutableStateOf(false)
}

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshSilently()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        containerColor = AppBackground,
        bottomBar = {
            BottomNavBar(
                selected = BottomNavItem.HOME,

                onHomeClick = {},

                onEventsClick = onNavigateToEvents,

                onCampaignsClick = onNavigateToCampaigns,

                onNotificationsClick = onNavigateToNotifications
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = viewModel.isLoading,
            onRefresh = { viewModel.refresh() },
            state = rememberPullToRefreshState()
        ) {
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 20.dp,
                bottom = 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Dashboard Header (Chart Area Placeholder)
            item {

                val currentDate = LocalDate.now(istZone)

                val formattedDate = currentDate.format(
                    DateTimeFormatter.ofPattern("EEE • MMM dd, yyyy")
                )
                val firstName = userName.substringBefore(" ")

                val greeting = when (LocalTime.now(istZone).hour) {
                    in 0..11 -> "Good Morning"
                    in 12..16 -> "Good Afternoon"
                    else -> "Good Evening"
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
                        modifier = Modifier.padding(24.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {

                            Column {

                                Text(
                                    text = formattedDate.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = greeting,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = TextSecondary
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = firstName,
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = TextPrimary
                                )
                            }

                            Box {

                                Surface(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clickable {
                                            showMenu = true
                                        },
                                    shape = RoundedCornerShape(20.dp),
                                    color = PulseBlue
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val initials = userName
                                            .split(" ")
                                            .mapNotNull { it.firstOrNull()?.toString() }
                                            .take(2)
                                            .joinToString("")
                                            .uppercase()
                                        Text(
                                            text = initials,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = AppBackground
                                        )
                                    }
                                }
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = {
                                            showMenu = false
                                        },
                                        containerColor = CardSurface,
                                        tonalElevation = 0.dp,
                                        shadowElevation = 0.dp,
                                        shape = RoundedCornerShape(12.dp),
                                    ){
                                        if (isAdmin && onNavigateToAdmin != null) {
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = "Admin Dashboard",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = TextPrimary
                                                    )
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.Dashboard,
                                                        contentDescription = null,
                                                        tint = PulseBlue
                                                    )
                                                },
                                                onClick = {
                                                    showMenu = false
                                                    Toast.makeText(context, "Switched back to Admin Dashboard", Toast.LENGTH_SHORT).show()
                                                    onNavigateToAdmin()
                                                }
                                            )
                                        }
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "Logout",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = TextPrimary
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                                    contentDescription = null,
                                                    tint = PulseRed
                                                )
                                            },
                                            onClick = {
                                                showMenu = false
                                                Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                                                onLogout()
                                            }
                                        )
                                    }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            StatCard(
                                modifier = Modifier.weight(1f),
                                title = "Pending",
                                value = viewModel.pendingCount.toString(),
                                color = PulseAmber,
                                backgroundColor = PulseAmberLight
                            )

                            StatCard(
                                modifier = Modifier.weight(1f),
                                title = "Approved",
                                value = viewModel.approvedCount.toString(),
                                color = PulseGreen,
                                backgroundColor = PulseGreenLight
                            )

                            StatCard(
                                modifier = Modifier.weight(1f),
                                title = "Active",
                                value = viewModel.activeCount.toString(),
                                color = PulseBlue,
                                backgroundColor = PulseBlueLight
                            )
                        }
                    }
                }
            }

            // Campaign Queue Section
            item {
                SectionHeader("Campaign Queue", onNavigateToCampaigns)
            }

            items(
                items = viewModel.campaigns
                    .filter {
                        it.status.equals("Generated", ignoreCase = true) ||
                        it.status.equals("Approved", ignoreCase = true)
                    }
                    .take(4),
                key = { it.campaignId }
            ) { campaign ->

                val eventTitle = viewModel.events
                    .find { it.id == campaign.eventId }
                    ?.title
                    ?: "Unknown Event"

                DashboardCampaignCard(
                    campaign = campaign,
                    title = eventTitle,
                    onClick = onNavigateToCampaignDetail
                )
            }

            // Upcoming Events Section
            item {
                SectionHeader("Upcoming Events", onNavigateToEvents)
            }
            
            // Note: In a real app we'd have an event list in the VM,
            // for now showing placeholders that match Figma
            items(
                items = viewModel.events.filter { isEventUpcoming(it.endTime) }.take(4),
                key = { it.id }
            ) { event ->

                EventDashboardCard(
                    name = event.title,
                    date = formatEventDate(event.startTime),
                    location = event.location,
                    eventType = event.eventType
                )
            }
        }
        }
    }
}

@Composable
fun SectionHeader(title: String, onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Row(
            modifier = Modifier.clickable { onSeeAll() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "All", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(12.dp))
        }
    }
}

@Composable
fun DashboardCampaignCard(
    campaign: Campaign,
    title: String,
    onClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick(campaign.campaignId.toString())
            },
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

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = campaign.campaignPost.take(80) +
                        if (campaign.campaignPost.length > 80) "..." else "",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                FigmaStatusBadge(
                    campaign.status
                )

                Text(
                    text = formatCampaignDate(
                        campaign.createdAt
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }

}

@Composable
fun EventDashboardCard(
    name: String,
    date: String,
    location: String,
    eventType: String
) {

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
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(12.dp)
                )

                Spacer(
                    modifier = Modifier.width(4.dp)
                )

                Text(
                    text = date,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Surface(
                    color = PulseBlue.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {

                    Text(
                        text = eventType.uppercase(),
                        color = PulseBlue,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(
                            horizontal = 7.dp,
                            vertical = 3.dp
                        )
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )


        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    color: Color,
    backgroundColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = color
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
        }
    }
}
