package com.speehive.speehiveaihub.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.speehive.speehiveaihub.ui.components.FigmaStatusBadge
import com.speehive.speehiveaihub.ui.theme.*
import com.speehive.speehiveaihub.viewmodel.CampaignDetailViewModel
import com.speehive.speehiveaihub.utils.formatCampaignDate
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignDetailScreen(
    campaignId: String,
    viewModel: CampaignDetailViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(campaignId) {
        viewModel.loadCampaign(campaignId)
    }

    val campaign = viewModel.campaign
    val isLoading = viewModel.isLoading

    Scaffold(
        containerColor = PureBlack,

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
                            contentDescription = null
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PureBlack
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
                                onBack()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),

                        shape = RoundedCornerShape(20.dp),

                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PulseRed
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
                                onBack()
                            }
                        },

                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),

                        shape = RoundedCornerShape(20.dp),

                        colors = ButtonDefaults.buttonColors(
                            containerColor = PulseGreen,
                            contentColor = PureBlack
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

                        /* INSERT IMAGE BLOCK HERE */

                        if (!campaign.imageUrl.isNullOrBlank()) {

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
                                        text = "Generated Image",
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Spacer(
                                        modifier = Modifier.height(12.dp)
                                    )

                                    AsyncImage(
                                        model = campaign.imageUrl,
                                        contentDescription = "Campaign Image",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(250.dp)
                                    )
                                }
                            }

                        } else {

                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = CardSurface
                                ),
                                border = BorderStroke(1.dp, CardBorder)
                            ) {

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(250.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Image not generated yet",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                        DetailSection(
                            title = "Campaign Post",
                            content = campaign.campaignPost
                        )

                        DetailSection(
                            title = "Hashtags",
                            content = campaign.hashtags
                        )
                    }
                }
            }
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
