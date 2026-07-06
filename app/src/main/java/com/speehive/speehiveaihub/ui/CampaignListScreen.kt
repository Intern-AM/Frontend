package com.speehive.speehiveaihub.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.speehive.speehiveaihub.models.Campaign
import com.speehive.speehiveaihub.ui.components.BottomNavBar
import com.speehive.speehiveaihub.ui.components.BottomNavItem
import com.speehive.speehiveaihub.ui.components.FigmaStatusBadge
import com.speehive.speehiveaihub.ui.theme.*
import com.speehive.speehiveaihub.utils.formatCampaignDate
import com.speehive.speehiveaihub.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignListScreen(
    viewModel: DashboardViewModel,
    onNavigateHome: () -> Unit,
    onNavigateEvents: () -> Unit,
    onNavigateNotifications: () -> Unit,
    onCampaignClick: (String) -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomNavBar(
                selected = BottomNavItem.CAMPAIGNS,
                onHomeClick = onNavigateHome,
                onEventsClick = onNavigateEvents,
                onCampaignsClick = {},
                onNotificationsClick = onNavigateNotifications
            )
        },
        containerColor = PureBlack,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "REVIEW QUEUE",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            "Campaigns",
                            style = MaterialTheme.typography.displayLarge
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PureBlack
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                items(
                    items = viewModel.reviewQueue,
                    key = { it.campaignId }
                ) { campaign ->

                    val eventTitle = viewModel.events
                        .find { it.id == campaign.eventId }
                        ?.title
                        ?: "Unknown Event"

                    CampaignListCard(
                        campaign = campaign,
                        title = eventTitle,
                        onClick = {
                            onCampaignClick(
                                campaign.campaignId.toString()
                            )
                        }
                    )
                }
            }
        }
    }

}

@Composable
fun CampaignListCard(
    campaign: Campaign,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = campaign.campaignPost.take(120) +
                        if (campaign.campaignPost.length > 120) "..." else "",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            FigmaStatusBadge(campaign.status)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = formatCampaignDate(
                    campaign.createdAt
                ),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }

}
