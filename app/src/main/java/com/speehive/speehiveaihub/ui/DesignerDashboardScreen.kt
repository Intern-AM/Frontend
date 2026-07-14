package com.speehive.speehiveaihub.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.speehive.speehiveaihub.models.Campaign
import com.speehive.speehiveaihub.models.Event
import com.speehive.speehiveaihub.ui.components.statusColor
import com.speehive.speehiveaihub.ui.components.ZoomableImageDialog
import com.speehive.speehiveaihub.ui.theme.*
import com.speehive.speehiveaihub.utils.formatCampaignDate
import com.speehive.speehiveaihub.utils.formatEventDate
import com.speehive.speehiveaihub.viewmodel.DesignerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignerDashboardScreen(
    viewModel: DesignerViewModel,
    onLogout: () -> Unit
) {
    var showCampaigns by remember { mutableStateOf(false) }
    var showEvents by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    val filteredCampaigns = viewModel.filteredCampaigns
    val filteredEvents = viewModel.filteredEvents

    LaunchedEffect(viewModel.isLoading) {
        if (isRefreshing && !viewModel.isLoading) {
            isRefreshing = false
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.loadDataSilently()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("DESIGNER", style = MaterialTheme.typography.labelSmall)
                        Text("Dashboard", style = MaterialTheme.typography.displayLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground),
                actions = {
                    TextButton(onClick = onLogout) {
                        Text(
                            "Logout",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.loadData()
            },
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 20.dp,
                    bottom = 40.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            if (viewModel.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PulseGreen)
                    }
                }
            }

            if (viewModel.errorMessage != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = PulseRedLight
                        ),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Text(
                            text = viewModel.errorMessage ?: "",
                            color = TextPrimary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            if (viewModel.uploadSuccess != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = PulseGreenLight
                        ),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Text(
                            text = viewModel.uploadSuccess ?: "",
                            color = TextPrimary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Campaigns",
                        value = filteredCampaigns.size.toString(),
                        color = PulseBlue,
                        backgroundColor = PulseBlueLight
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Events",
                        value = filteredEvents.size.toString(),
                        color = PulseGreen,
                        backgroundColor = PulseGreenLight
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Quick Access",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            item {
                DesignerExpandableSection(
                    icon = Icons.Default.Campaign,
                    title = "View Campaigns",
                    subtitle = "${filteredCampaigns.size} campaigns ready for poster upload",
                    color = PulseBlue,
                    isExpanded = showCampaigns,
                    onToggle = { showCampaigns = !showCampaigns },
                    content = {
                        if (filteredCampaigns.isEmpty()) {
                            Text(
                                text = "No generated campaigns available",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            filteredCampaigns.forEach { campaign ->
                                DesignerCampaignCard(
                                    campaign = campaign,
                                    isUploading = viewModel.uploadingId == campaign.eventId,
                                    onUpload = { uri ->
                                        viewModel.uploadCampaignImage(campaign.eventId, uri)
                                    },
                                    onEdit = { campaignPost, hashtags ->
                                        viewModel.editCampaign(campaign.eventId, campaignPost, hashtags)
                                    }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                )
            }

            item {
                DesignerExpandableSection(
                    icon = Icons.Default.Event,
                    title = "View Events",
                    subtitle = "${filteredEvents.size} events needing poster upload",
                    color = PulseGreen,
                    isExpanded = showEvents,
                    onToggle = { showEvents = !showEvents },
                    content = {
                        if (filteredEvents.isEmpty()) {
                            Text(
                                text = "No events needing poster upload",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            filteredEvents.forEach { event ->
                                DesignerEventCard(
                                    event = event,
                                    isUploading = viewModel.uploadingId == event.id,
                                    onUpload = { uri ->
                                        viewModel.uploadEventImage(event.id, uri)
                                    }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                )
            }
        }
        }
    }
}

@Composable
fun DesignerExpandableSection(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        ),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = TextSecondary
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 20.dp
                    )
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun DesignerCampaignCard(
    campaign: Campaign,
    isUploading: Boolean,
    onUpload: (Uri) -> Unit,
    onEdit: (String, String) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { onUpload(it) } }

    val campaignStatusColor = statusColor(campaign.status)
    var isEditing by remember { mutableStateOf(false) }
    var editCampaignPost by remember { mutableStateOf(campaign.campaignPost) }
    var editHashtags by remember { mutableStateOf(campaign.hashtags) }
    var showFullScreenImage by remember { mutableStateOf(false) }

    val isLocked = campaign.status.lowercase().let {
        it == "approved" || it == "rejected" || it == "posted"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        ),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = if (isEditing) "Edit Campaign" else campaign.campaignPost.take(80) +
                            if (campaign.campaignPost.length > 80) "..." else "",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = campaignStatusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = campaign.status.uppercase(),
                        color = campaignStatusColor,
                        maxLines = 1,
                        modifier = Modifier.padding(
                            horizontal = 6.dp,
                            vertical = 2.dp
                        )
                    )
                }
            }

            if (isEditing) {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = editCampaignPost,
                    onValueChange = { editCampaignPost = it },
                    label = { Text("Campaign Post") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PulseBlue,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = PulseBlue
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = editHashtags,
                    onValueChange = { editHashtags = it },
                    label = { Text("Hashtags") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PulseBlue,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = PulseBlue
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            isEditing = false
                            editCampaignPost = campaign.campaignPost
                            editHashtags = campaign.hashtags
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", style = MaterialTheme.typography.titleSmall)
                    }

                    Button(
                        onClick = {
                            onEdit(editCampaignPost, editHashtags)
                            isEditing = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PulseBlue,
                            contentColor = AppBackground
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save", style = MaterialTheme.typography.titleSmall)
                    }
                }
            } else {
                if (campaign.hashtags.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = campaign.hashtags,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = formatCampaignDate(campaign.createdAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted
                )

                if (!campaign.imageUrl.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    AsyncImage(
                        model = campaign.imageUrl,
                        contentDescription = "Campaign image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showFullScreenImage = true },
                        contentScale = ContentScale.FillWidth
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isLocked) {
                        OutlinedButton(
                            onClick = { isEditing = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, PulseBlue),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = PulseBlue
                            )
                        ) {
                            Text("Edit", style = MaterialTheme.typography.titleSmall)
                        }
                    }

                    Button(
                        onClick = { launcher.launch(arrayOf("image/png", "image/jpeg")) },
                        enabled = !isUploading && !isLocked,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLocked) TextMuted else PulseBlue,
                            contentColor = AppBackground
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = AppBackground,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(
                                Icons.Default.Upload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (campaign.imageUrl.isNullOrBlank()) "Upload Poster" else "Replace Poster",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }
        }
    }

    if (showFullScreenImage) {
        ZoomableImageDialog(imageUrl = campaign.imageUrl) {
            showFullScreenImage = false
        }
    }
}

@Composable
fun DesignerEventCard(
    event: Event,
    isUploading: Boolean,
    onUpload: (Uri) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { onUpload(it) } }

    val eventStatusColor = statusColor(event.status)
    var showFullScreenImage by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        ),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = formatEventDate(event.startTime),
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted
            )

            if (!event.designerImageUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                AsyncImage(
                    model = event.designerImageUrl,
                    contentDescription = "Designer poster",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showFullScreenImage = true },
                    contentScale = ContentScale.FillWidth
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { launcher.launch(arrayOf("image/png", "image/jpeg")) },
                enabled = !isUploading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PulseBlue,
                    contentColor = AppBackground
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = AppBackground,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Icon(
                        Icons.Default.Upload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (event.designerImageUrl.isNullOrBlank()) "Upload Poster" else "Replace Poster",
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }

    if (showFullScreenImage) {
        ZoomableImageDialog(imageUrl = event.designerImageUrl) {
            showFullScreenImage = false
        }
    }
}
