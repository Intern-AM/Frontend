package com.speehive.speehiveaihub.ui

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.speehive.speehiveaihub.ui.components.FigmaStatusBadge
import com.speehive.speehiveaihub.ui.components.ZoomableImageDialog
import com.speehive.speehiveaihub.ui.theme.*
import com.speehive.speehiveaihub.viewmodel.CampaignDetailViewModel
import com.speehive.speehiveaihub.utils.formatCampaignDate
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import java.time.Instant
import java.time.ZoneId
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.LocalTime
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Clear
import com.speehive.speehiveaihub.network.UpdateScheduleRequest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.speehive.speehiveaihub.ui.components.statusColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignDetailScreen(
    campaignId: String,
    viewModel: CampaignDetailViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    LaunchedEffect(campaignId) {
        viewModel.loadCampaign(campaignId)
    }

    val campaign = viewModel.campaign
    val isLoading = viewModel.isLoading
    var showFullScreenImage by remember { mutableStateOf(false) }
    var showImagePromptDropdown by remember { mutableStateOf(false) }
    val isProcessing = viewModel.isProcessing
    val errorMessage = viewModel.errorMessage
    var showEditScheduleDialog by remember { mutableStateOf(false) }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            containerColor = CardSurface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Error",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearError() },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PulseBlue
                    )
                ) {
                    Text(
                        text = "Dismiss",
                        color = AppBackground
                    )
                }
            }
        )
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.uploadCampaignImage(it) }
    }

    var isEditingPost by remember { mutableStateOf(false) }
    var editCampaignPost by remember(campaign?.campaignPost) { mutableStateOf(campaign?.campaignPost ?: "") }
    var editHashtags by remember(campaign?.hashtags) { mutableStateOf(campaign?.hashtags ?: "") }

    Scaffold(
        containerColor = AppBackground,

        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("CAMPAIGN", style = MaterialTheme.typography.labelSmall)
                        Text("Review", style = MaterialTheme.typography.displayLarge)
                    }
                },

                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBackground
                )
            )
        },

        bottomBar = {

            if (
                campaign != null &&
                campaign.status.equals(
                    "Generated",
                    ignoreCase = true
                )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    OutlinedButton(
                        onClick = {

                            viewModel.rejectCampaign(
                                comments = "Rejected by reviewer"
                            ) {
                                Toast.makeText(context, "Campaign rejected successfully", Toast.LENGTH_SHORT).show()
                                onBack()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        enabled = !isProcessing,
                        shape = RoundedCornerShape(20.dp),

                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PulseRed,
                            disabledContentColor = PulseRed.copy(alpha = 0.5f)
                        )
                    ) {

                        Icon(
                            Icons.Default.Close,
                            contentDescription = null
                        )

                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )

                        Text(
                            text = "Reject",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    Button(
                        onClick = {

                            viewModel.approveCampaign {
                                Toast.makeText(context, "Campaign approved successfully", Toast.LENGTH_SHORT).show()
                            }
                        },

                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        enabled = !isProcessing,
                        shape = RoundedCornerShape(20.dp),

                        colors = ButtonDefaults.buttonColors(
                            containerColor = PulseGreen,
                            contentColor = AppBackground,
                            disabledContainerColor = PulseGreen.copy(alpha = 0.5f)
                        )
                    ) {

                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null
                        )

                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )

                        Text(
                            text = "Approve",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            when {

                isLoading -> {

                    CircularProgressIndicator(
                        modifier =
                            Modifier.align(
                                Alignment.Center
                            )
                    )
                }

                campaign == null -> {

                    Text(
                        text = "Campaign not found",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier =
                            Modifier.align(
                                Alignment.Center
                            )
                    )
                }

                else -> {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(
                                rememberScrollState()
                            )
                            .padding(20.dp),

                        verticalArrangement =
                            Arrangement.spacedBy(20.dp)
                    ) {

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = CardSurface
                            ),
                            border = BorderStroke(1.dp, CardBorder)
                        ) {

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {

                                Text(
                                    text = viewModel.eventTitle,
                                    style =
                                        MaterialTheme.typography
                                            .headlineSmall
                                )

                                Spacer(
                                    modifier = Modifier.height(12.dp)
                                )

                                FigmaStatusBadge(
                                    campaign.status
                                )

                                Spacer(
                                    modifier = Modifier.height(12.dp)
                                )

                                Text(
                                    text = formatCampaignDate(
                                        campaign.createdAt
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Publishing Schedules Card
                        val scheduleState = viewModel.scheduleState
                        val isLoadingSchedule = viewModel.isLoadingSchedule
                        val isUpdatingSchedule = viewModel.isUpdatingSchedule

                        val postedPlatforms = scheduleState?.platforms
                            ?.filter { it.status.equals("Posted", ignoreCase = true) }
                            ?.map { it.platform.lowercase() }
                            ?.toSet() ?: emptySet()

                        val unpostedPlatforms = scheduleState?.platforms
                            ?.filter { !it.status.equals("Posted", ignoreCase = true) }
                            ?: emptyList()

                        val isCompletelyPosted = campaign.status.equals("Posted", ignoreCase = true) ||
                                (scheduleState != null && scheduleState.platforms.isNotEmpty() && scheduleState.platforms.all { it.status.equals("Posted", ignoreCase = true) })

                        if (campaign.status.equals("Approved", ignoreCase = true)) {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = CardSurface
                                ),
                                border = BorderStroke(1.dp, CardBorder)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Publishing Schedules",
                                            style = MaterialTheme.typography.titleMedium
                                        )

                                        val canModifySchedule = campaign.status.equals("Approved", ignoreCase = true) && !isCompletelyPosted

                                        if (canModifySchedule && !isUpdatingSchedule) {
                                            IconButton(onClick = { showEditScheduleDialog = true }) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit Schedules",
                                                    tint = PulseBlue
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    if (isLoadingSchedule) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                        }
                                    } else if (scheduleState != null && unpostedPlatforms.isNotEmpty()) {
                                        unpostedPlatforms.forEachIndexed { index, platformInfo ->
                                            val platformTime = when {
                                                platformInfo.platform.contains("LinkedIn", ignoreCase = true) -> scheduleState.schdtimeLinkedIn
                                                platformInfo.platform.contains("Instagram", ignoreCase = true) -> scheduleState.schdtimeInstagram
                                                platformInfo.platform.contains("Teams", ignoreCase = true) -> scheduleState.schdtimeTeams
                                                platformInfo.platform.contains("Whatsapp", ignoreCase = true) || platformInfo.platform.contains("WhatsApp", ignoreCase = true) -> scheduleState.schdtimeWhatsapp
                                                else -> null
                                            }

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = when {
                                                            platformInfo.platform.equals("LinkedIn", ignoreCase = true) -> "LinkedIn"
                                                            platformInfo.platform.equals("Instagram", ignoreCase = true) -> "Instagram"
                                                            platformInfo.platform.equals("Teams", ignoreCase = true) -> "MS Teams Group"
                                                            platformInfo.platform.equals("Whatsapp", ignoreCase = true) || platformInfo.platform.equals("WhatsApp", ignoreCase = true) -> "WhatsApp Channel"
                                                            else -> platformInfo.platform
                                                        },
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        color = TextPrimary
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = if (!platformTime.isNullOrBlank()) {
                                                            formatCampaignDate(platformTime)
                                                        } else {
                                                            "Not Scheduled"
                                                        },
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = if (!platformTime.isNullOrBlank()) TextSecondary else TextMuted
                                                    )
                                                    if (!platformInfo.errorMessage.isNullOrBlank()) {
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = platformInfo.errorMessage,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = PulseRed
                                                        )
                                                    }
                                                }

                                                val platformStatus = if (!platformTime.isNullOrBlank()) {
                                                    platformInfo.status
                                                } else {
                                                    "Not Scheduled"
                                                }
                                                val pColor = statusColor(platformStatus)
                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            pColor.copy(alpha = 0.15f),
                                                            RoundedCornerShape(100.dp)
                                                        )
                                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = platformStatus.uppercase(),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = pColor
                                                    )
                                                }
                                            }
                                            if (index < unpostedPlatforms.lastIndex) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(1.dp)
                                                        .background(CardBorder.copy(alpha = 0.5f))
                                                )
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = if (isCompletelyPosted) "All scheduled posts have been published." else "No schedules configured. Tap edit to set up.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }

                        if (showEditScheduleDialog) {
                            EditScheduleDialog(
                                initialLinkedIn = scheduleState?.schdtimeLinkedIn,
                                initialInstagram = scheduleState?.schdtimeInstagram,
                                initialTeams = scheduleState?.schdtimeTeams,
                                initialWhatsapp = scheduleState?.schdtimeWhatsapp,
                                postedPlatforms = postedPlatforms,
                                onDismiss = { showEditScheduleDialog = false },
                                onSave = { request ->
                                    showEditScheduleDialog = false
                                    viewModel.updateSchedule(request) {
                                        Toast.makeText(context, "Schedules updated successfully", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }

                        // AI Creative Image Prompt Card (Always visible with Copy Prompt button)
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = CardSurface
                            ),
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showImagePromptDropdown = !showImagePromptDropdown },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(PulseBlue.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lightbulb,
                                                contentDescription = null,
                                                tint = PulseBlue,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "AI CREATIVE IMAGE PROMPT",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = PulseBlue
                                            )
                                            Text(
                                                text = if (showImagePromptDropdown) "Tap to collapse" else "Tap to view full prompt details",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Button(
                                            onClick = {
                                                val promptToCopy = campaign.imagePrompt
                                                if (!promptToCopy.isNullOrBlank()) {
                                                    clipboardManager.setText(AnnotatedString(promptToCopy))
                                                    Toast.makeText(context, "Image prompt copied to clipboard!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "No image prompt available for this campaign", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = PulseBlue.copy(alpha = 0.15f),
                                                contentColor = PulseBlue
                                            ),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy Image Prompt",
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Copy",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }

                                        IconButton(onClick = { showImagePromptDropdown = !showImagePromptDropdown }) {
                                            Icon(
                                                imageVector = if (showImagePromptDropdown) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                                contentDescription = null,
                                                tint = TextSecondary
                                            )
                                        }
                                    }
                                }

                                if (showImagePromptDropdown) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(AppBackground, RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = if (!campaign.imagePrompt.isNullOrBlank()) campaign.imagePrompt else "No image prompt available for this campaign.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }

                        // Campaign Image Card with Upload / Replace Button
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = CardSurface
                            ),
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Campaign Poster",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                if (!campaign.imageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = campaign.imageUrl,
                                        contentDescription = "Campaign Poster",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight()
                                            .clip(RoundedCornerShape(20.dp))
                                            .clickable { showFullScreenImage = true },
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .background(AppBackground, RoundedCornerShape(16.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (campaign.status.equals("Posted", ignoreCase = true))
                                                "No poster uploaded for this campaign."
                                            else
                                                "No poster uploaded yet. Tap Upload Poster to add one.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextMuted
                                        )
                                    }
                                }

                                if (!campaign.status.equals("Posted", ignoreCase = true)) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Button(
                                            onClick = { imagePickerLauncher.launch(arrayOf("image/*")) },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = PulseBlue,
                                                contentColor = AppBackground
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Upload,
                                                contentDescription = "Upload Poster",
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (!campaign.imageUrl.isNullOrBlank()) "Replace Poster" else "Upload Poster",
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Editable Post & Hashtags Card
                        Card(
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
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Campaign Post & Hashtags",
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    if (!isEditingPost) {
                                        IconButton(onClick = { isEditingPost = true }) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Post",
                                                tint = PulseBlue
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                if (isEditingPost) {
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

                                    Spacer(modifier = Modifier.height(12.dp))

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

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                isEditingPost = false
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
                                                viewModel.editCampaign(editCampaignPost, editHashtags)
                                                isEditingPost = false
                                                Toast.makeText(context, "Campaign updated", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = PulseBlue,
                                                contentColor = AppBackground
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Save Changes", style = MaterialTheme.typography.titleSmall)
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "Post Copy",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = campaign.campaignPost,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary
                                    )

                                    if (campaign.hashtags.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Hashtags",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = campaign.hashtags,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = PulseBlue
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFullScreenImage) {
        ZoomableImageDialog(imageUrl = campaign?.imageUrl) {
            showFullScreenImage = false
        }
    }
}

@Composable
fun DetailSection(
    title: String,
    content: String
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        ),
        border = BorderStroke(1.dp, CardBorder)
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = title,
                style =
                    MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScheduleDialog(
    initialLinkedIn: String?,
    initialInstagram: String?,
    initialTeams: String?,
    initialWhatsapp: String?,
    postedPlatforms: Set<String> = emptySet(),
    onDismiss: () -> Unit,
    onSave: (UpdateScheduleRequest) -> Unit
) {
    val context = LocalContext.current
    var linkedInTime by remember { mutableStateOf(initialLinkedIn) }
    var instagramTime by remember { mutableStateOf(initialInstagram) }
    var teamsTime by remember { mutableStateOf(initialTeams) }
    var whatsappTime by remember { mutableStateOf(initialWhatsapp) }

    var activePickerPlatform by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker && activePickerPlatform != null) {
        val initialEpoch = when (activePickerPlatform) {
            "LinkedIn" -> linkedInTime
            "Instagram" -> instagramTime
            "Teams" -> teamsTime
            "Whatsapp" -> whatsappTime
            else -> null
        }?.let {
            try {
                java.time.OffsetDateTime.parse(it).toInstant().toEpochMilli()
            } catch (e: Exception) {
                null
            }
        }

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialEpoch
        )

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
                activePickerPlatform = null
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            showDatePicker = false
                            val selectedInstant = Instant.ofEpochMilli(selectedMillis)
                            val localDate = selectedInstant.atZone(ZoneId.of("UTC")).toLocalDate()
                            
                            val defaultTime = when (activePickerPlatform) {
                                "LinkedIn" -> linkedInTime
                                "Instagram" -> instagramTime
                                "Teams" -> teamsTime
                                "Whatsapp" -> whatsappTime
                                else -> null
                            }?.let {
                                try {
                                    java.time.OffsetDateTime.parse(it).atZoneSameInstant(ZoneId.of("Asia/Kolkata")).toLocalDateTime()
                                } catch (e: Exception) {
                                    null
                                }
                            } ?: LocalDateTime.now(ZoneId.of("Asia/Kolkata"))

                            android.app.TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    val localDateTime = LocalDateTime.of(localDate, LocalTime.of(hour, minute))
                                    val istZonedDateTime = localDateTime.atZone(ZoneId.of("Asia/Kolkata"))
                                    val instant = istZonedDateTime.toInstant()
                                    if (instant.isBefore(Instant.now())) {
                                        Toast.makeText(context, "Schedule time cannot be in the past", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val isoString = instant.toString()
                                        when (activePickerPlatform) {
                                            "LinkedIn" -> linkedInTime = isoString
                                            "Instagram" -> instagramTime = isoString
                                            "Teams" -> teamsTime = isoString
                                            "Whatsapp" -> whatsappTime = isoString
                                        }
                                    }
                                    activePickerPlatform = null
                                },
                                defaultTime.hour,
                                defaultTime.minute,
                                false
                            ).show()
                        } else {
                            showDatePicker = false
                            activePickerPlatform = null
                        }
                    }
                ) {
                    Text("OK", color = PulseBlue)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        activePickerPlatform = null
                    }
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Modify Publishing Schedules",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val allPlatforms = listOf(
                    Triple("LinkedIn", "LinkedIn", linkedInTime),
                    Triple("Instagram", "Instagram", instagramTime),
                    Triple("Teams", "MS Teams Group", teamsTime),
                    Triple("Whatsapp", "WhatsApp Channel", whatsappTime)
                )

                val platforms = allPlatforms.filter { (key, _, _) ->
                    key.lowercase() !in postedPlatforms
                }

                platforms.forEach { (platformKey, platformLabel, currentTime) ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppBackground.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .border(1.dp, CardBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = platformLabel,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = if (!currentTime.isNullOrBlank()) {
                                formatCampaignDate(currentTime)
                            } else {
                                "Not Scheduled"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (!currentTime.isNullOrBlank()) TextSecondary else TextMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    activePickerPlatform = platformKey
                                    showDatePicker = true
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = PulseBlue
                                ),
                                border = BorderStroke(1.dp, CardBorder)
                            ) {
                                Text("Set Schedule")
                            }

                            if (!currentTime.isNullOrBlank()) {
                                TextButton(
                                    onClick = {
                                        when (platformKey) {
                                            "LinkedIn" -> linkedInTime = null
                                            "Instagram" -> instagramTime = null
                                            "Teams" -> teamsTime = null
                                            "Whatsapp" -> whatsappTime = null
                                        }
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = PulseRed
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear Schedule",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Clear")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val hasLinkedInChanged = linkedInTime != initialLinkedIn
            val hasInstagramChanged = instagramTime != initialInstagram
            val hasTeamsChanged = teamsTime != initialTeams
            val hasWhatsappChanged = whatsappTime != initialWhatsapp
            val isSaveEnabled = hasLinkedInChanged || hasInstagramChanged || hasTeamsChanged || hasWhatsappChanged

            Button(
                onClick = {
                    onSave(
                        UpdateScheduleRequest(
                            schdtimeLinkedIn = linkedInTime,
                            schdtimeInstagram = instagramTime,
                            schdtimeTeams = teamsTime,
                            schdtimeWhatsapp = whatsappTime,
                            isLinkedInModified = hasLinkedInChanged,
                            isInstagramModified = hasInstagramChanged,
                            isTeamsModified = hasTeamsChanged,
                            isWhatsappModified = hasWhatsappChanged
                        )
                    )
                },
                enabled = isSaveEnabled,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PulseGreen,
                    contentColor = AppBackground,
                    disabledContainerColor = PulseGreen.copy(alpha = 0.5f)
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
