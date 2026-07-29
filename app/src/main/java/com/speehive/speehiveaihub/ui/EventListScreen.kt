package com.speehive.speehiveaihub.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import com.speehive.speehiveaihub.ui.components.ConfirmationDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import com.speehive.speehiveaihub.R
import com.speehive.speehiveaihub.models.Campaign
import com.speehive.speehiveaihub.models.Event
import com.speehive.speehiveaihub.ui.components.BottomNavBar
import com.speehive.speehiveaihub.ui.components.BottomNavItem
import com.speehive.speehiveaihub.ui.components.FigmaStatusBadge
import com.speehive.speehiveaihub.ui.components.ZoomableImageDialog
import com.speehive.speehiveaihub.ui.components.SlidingStatusFilter
import com.speehive.speehiveaihub.ui.components.statusColor
import com.speehive.speehiveaihub.ui.theme.*
import com.speehive.speehiveaihub.utils.formatCampaignDate
import com.speehive.speehiveaihub.utils.formatEventDate
import com.speehive.speehiveaihub.utils.isEventPassed
import com.speehive.speehiveaihub.utils.istZone
import com.speehive.speehiveaihub.viewmodel.EventViewModel
import java.time.OffsetDateTime

sealed class EventListItem {
    data class PendingEvent(val event: Event) : EventListItem()
    data class GeneratedCampaign(
        val campaign: Campaign,
        val eventTitle: String,
        val eventStartTime: String,
        val eventEndTime: String?
    ) : EventListItem()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    viewModel: EventViewModel,
    initialStatus: String,
    onStatusChange: (String) -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateNotifications: () -> Unit,
    onCampaignClick: (String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.loadEventsSilently()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var selectedMonth by remember { mutableStateOf(0) } // 0 = All Months
    var selectedYear by remember { mutableStateOf(java.time.LocalDate.now().year.toString()) }
    var selectedStatus by remember(initialStatus) { mutableStateOf(initialStatus) } // "All", "Pending", "Generated", "Rejected", "Completed"
    var monthDropdownExpanded by remember { mutableStateOf(false) }
    var yearDropdownExpanded by remember { mutableStateOf(false) }
    var eventToReject by remember { mutableStateOf<Event?>(null) }
    var eventToRestore by remember { mutableStateOf<Event?>(null) }

    val months = listOf(
        "All Months", "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val availableYears = remember {
        val currentYear = java.time.LocalDate.now().year
        val previousYear = currentYear - 1
        listOf(currentYear.toString(), previousYear.toString())
    }

    val listItems = remember(viewModel.events, viewModel.campaigns, selectedMonth, selectedYear, selectedStatus) {
        val eventsFilteredByDate = viewModel.events.filter { event ->
            try {
                val date = OffsetDateTime.parse(event.startTime).atZoneSameInstant(istZone)
                val matchesMonth = if (selectedMonth == 0) true else date.monthValue == selectedMonth
                val matchesYear = date.year.toString() == selectedYear
                matchesMonth && matchesYear
            } catch (e: Exception) {
                false
            }
        }

        val items = mutableListOf<EventListItem>()
        eventsFilteredByDate.forEach { event ->
            val campaign = viewModel.campaigns.find { it.eventId == event.id }
            val hasCampaign = campaign != null
            val isPassed = event.endTime?.let { isEventPassed(it) } ?: false

            when (selectedStatus) {
                "All" -> {
                    if (hasCampaign) {
                        items.add(EventListItem.GeneratedCampaign(campaign!!, event.title, event.startTime, event.endTime))
                    } else {
                        items.add(EventListItem.PendingEvent(event))
                    }
                }
                "Pending" -> {
                    if (!hasCampaign && !event.status.equals("Cancelled", ignoreCase = true)) {
                        items.add(EventListItem.PendingEvent(event))
                    }
                }
                "Generated" -> {
                    if (hasCampaign && !campaign!!.status.equals("Rejected", ignoreCase = true) && !isPassed) {
                        items.add(EventListItem.GeneratedCampaign(campaign, event.title, event.startTime, event.endTime))
                    }
                }
                "Rejected" -> {
                    if (event.status.equals("Cancelled", ignoreCase = true)) {
                        items.add(EventListItem.PendingEvent(event))
                    }
                    if (hasCampaign && campaign!!.status.equals("Rejected", ignoreCase = true)) {
                        items.add(EventListItem.GeneratedCampaign(campaign, event.title, event.startTime, event.endTime))
                    }
                }
                "Completed" -> {
                    if (hasCampaign && (campaign!!.status.equals("Posted", ignoreCase = true) || isPassed)) {
                        items.add(EventListItem.GeneratedCampaign(campaign, event.title, event.startTime, event.endTime))
                    }
                }
            }
        }

        items.sortedBy { item ->
            when (item) {
                is EventListItem.PendingEvent -> item.event.startTime
                is EventListItem.GeneratedCampaign -> item.eventStartTime
            }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selected = BottomNavItem.EVENTS,
                onHomeClick = onNavigateHome,
                onEventsClick = {},
                onNotificationsClick = onNavigateNotifications
            )
        },
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(stringResource(R.string.monitoring_header), style = MaterialTheme.typography.labelSmall)
                        Text(stringResource(R.string.nav_events), style = MaterialTheme.typography.displayLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = viewModel.isLoading,
            onRefresh = { viewModel.loadEvents() },
            state = rememberPullToRefreshState()
        ) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                // Filters Header Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Month & Year Dropdowns Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Month Dropdown (weight 1.2f)
                            Box(modifier = Modifier.weight(1.2f)) {
                                OutlinedButton(
                                    onClick = { monthDropdownExpanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, CardBorder),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = months[selectedMonth],
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Icon(
                                            imageVector = if (monthDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                            contentDescription = "Dropdown Indicator",
                                            tint = TextSecondary
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = monthDropdownExpanded,
                                    onDismissRequest = { monthDropdownExpanded = false },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(CardSurface)
                                ) {
                                    months.forEachIndexed { index, name ->
                                        DropdownMenuItem(
                                            text = { Text(name, style = MaterialTheme.typography.bodyMedium, color = TextPrimary) },
                                            onClick = {
                                                selectedMonth = index
                                                monthDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Year Dropdown (weight 1f)
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { yearDropdownExpanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, CardBorder),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = selectedYear,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Icon(
                                            imageVector = if (yearDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                            contentDescription = "Dropdown Indicator",
                                            tint = TextSecondary
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = yearDropdownExpanded,
                                    onDismissRequest = { yearDropdownExpanded = false },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(CardSurface)
                                ) {
                                    availableYears.forEach { yearName ->
                                        DropdownMenuItem(
                                            text = { Text(yearName, style = MaterialTheme.typography.bodyMedium, color = TextPrimary) },
                                            onClick = {
                                                selectedYear = yearName
                                                yearDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        SlidingStatusFilter(
                            selectedStatus = selectedStatus,
                            onStatusSelected = {
                                selectedStatus = it
                                onStatusChange(it)
                            }
                        )
                    }
                }

                viewModel.errorMessage?.let { message ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = PulseRedLight),
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
                        CircularProgressIndicator()
                    }
                } else if (listItems.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No events or campaigns found",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyLarge
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
                            items = listItems,
                            key = { item ->
                                when (item) {
                                    is EventListItem.PendingEvent -> "event_${item.event.id}"
                                    is EventListItem.GeneratedCampaign -> "campaign_${item.campaign.campaignId}"
                                }
                            }
                        ) { item ->
                            when (item) {
                                is EventListItem.PendingEvent -> {
                                    val campaignExists = viewModel.campaigns.any { it.eventId == item.event.id }
                                    val canRestore = item.event.status.equals("Cancelled", ignoreCase = true) && !campaignExists
                                    FullEventCard(
                                        event = item.event,
                                        onReject = { eventToReject = item.event },
                                        onUploadImage = { uri -> viewModel.uploadEventImage(item.event.id, uri) },
                                        onRestore = if (canRestore) { { eventToRestore = item.event } } else null,
                                        selectedStatus = selectedStatus,
                                        isProcessing = viewModel.isProcessing
                                    )
                                }
                                is EventListItem.GeneratedCampaign -> {
                                    val isPassed = remember(item.eventEndTime) { isEventPassed(item.eventEndTime) }
                                    CampaignListCard(
                                        campaign = item.campaign,
                                        title = item.eventTitle,
                                        eventStartTime = item.eventStartTime,
                                        isPassed = isPassed,
                                        selectedStatus = selectedStatus,
                                        onClick = { onCampaignClick(item.campaign.campaignId.toString()) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (eventToReject != null) {
        ConfirmationDialog(
            title = "Reject Event",
            message = "Are you sure you want to reject \"${eventToReject?.title}\"? This will cancel the event.",
            confirmLabel = "Reject",
            confirmButtonColor = PulseRed,
            onConfirm = {
                eventToReject?.let { viewModel.cancelEvent(it.id) }
                eventToReject = null
            },
            onDismiss = { eventToReject = null }
        )
    }

    if (eventToRestore != null) {
        ConfirmationDialog(
            title = "Restore Event",
            message = "Are you sure you want to restore \"${eventToRestore?.title}\"? This will make the event pending again.",
            confirmLabel = "Restore",
            confirmButtonColor = PulseGreen,
            onConfirm = {
                eventToRestore?.let { viewModel.restoreEvent(it.id) }
                eventToRestore = null
            },
            onDismiss = { eventToRestore = null }
        )
    }
}

@Composable
fun CampaignListCard(
    campaign: Campaign,
    title: String,
    eventStartTime: String,
    isPassed: Boolean,
    selectedStatus: String = "All",
    onClick: () -> Unit
) {
    val displayStatus = if (isPassed && !campaign.status.equals("Posted", ignoreCase = true)) "ARCHIVED" else campaign.status
    val shadowColor = if (selectedStatus == "All") {
        statusColor(displayStatus)
    } else {
        Color(0x380F172A)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .deep3DCard(elevation = 10.dp, spotColor = shadowColor)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )

                FigmaStatusBadge(displayStatus)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = campaign.campaignPost.take(120) +
                        if (campaign.campaignPost.length > 120) "..." else "",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = formatEventDate(eventStartTime),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun FullEventCard(
    event: Event,
    onReject: () -> Unit,
    onUploadImage: ((android.net.Uri) -> Unit)? = null,
    onRestore: (() -> Unit)? = null,
    selectedStatus: String = "All",
    isProcessing: Boolean = false
) {
    val displayStatus = event.status
    val eventStatusColor = statusColor(displayStatus)
    var showZoomDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { onUploadImage?.invoke(it) }
    }

    val shadowColor = if (selectedStatus == "All") {
        eventStatusColor
    } else {
        Color(0x380F172A)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .deep3DCard(elevation = 10.dp, spotColor = shadowColor),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
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
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = event.description,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )

            if (!event.designerImageUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = event.designerImageUrl,
                    contentDescription = "Event Poster",
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showZoomDialog = true },
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatEventDate(event.startTime),
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                if (onUploadImage != null && event.status.equals("Pending", ignoreCase = true)) {
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch(arrayOf("image/*")) },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PulseBlue),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = "Upload Poster",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (!event.designerImageUrl.isNullOrBlank()) "Replace Poster" else "Upload Poster",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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

            if (onRestore != null) {
                Button(
                    onClick = onRestore,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PulseGreen,
                        disabledContainerColor = PulseGreen.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isProcessing
                ) {
                    Text(
                        text = "Restore Event",
                        color = AppBackground
                    )
                }
                Spacer(
                    modifier = Modifier.height(20.dp)
                )
            }

            Row {
                Surface(
                    color = eventStatusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = displayStatus.uppercase(),
                        color = eventStatusColor,
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

    if (showZoomDialog) {
        ZoomableImageDialog(imageUrl = event.designerImageUrl) {
            showZoomDialog = false
        }
    }
}

