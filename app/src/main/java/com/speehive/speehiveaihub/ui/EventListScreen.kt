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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import com.speehive.speehiveaihub.R
import com.speehive.speehiveaihub.models.Event
import com.speehive.speehiveaihub.ui.components.statusColor
import com.speehive.speehiveaihub.ui.theme.*
import com.speehive.speehiveaihub.viewmodel.EventViewModel
import com.speehive.speehiveaihub.ui.components.BottomNavBar
import com.speehive.speehiveaihub.ui.components.BottomNavItem
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Upload
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.clickable
import coil.compose.AsyncImage
import com.speehive.speehiveaihub.ui.components.ZoomableImageDialog
import com.speehive.speehiveaihub.utils.formatEventDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    viewModel: EventViewModel,
    onNavigateHome: () -> Unit,
    onNavigateCampaigns: () -> Unit,
    onNavigateNotifications: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.loadEventsSilently()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
                    CircularProgressIndicator()
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
                            onUploadImage = { uri -> viewModel.uploadEventImage(event.id, uri) },
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
    onUploadImage: ((android.net.Uri) -> Unit)? = null,
    isProcessing: Boolean = false
){
    val eventStatusColor = statusColor(event.status)
    var showZoomDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { onUploadImage?.invoke(it) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .deep3DCard(elevation = 10.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        ),
        border = BorderStroke(1.dp, CardBorder)
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

            Row {
                Surface(
                    color = eventStatusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = event.status.uppercase(),
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
}
