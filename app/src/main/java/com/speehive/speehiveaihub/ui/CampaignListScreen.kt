package com.speehive.speehiveaihub.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.unit.dp
import com.speehive.speehiveaihub.models.Campaign
import com.speehive.speehiveaihub.ui.components.BottomNavBar
import com.speehive.speehiveaihub.ui.components.BottomNavItem
import com.speehive.speehiveaihub.ui.components.FigmaStatusBadge
import com.speehive.speehiveaihub.ui.components.ZoomableImageDialog
import com.speehive.speehiveaihub.ui.theme.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upload
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import com.speehive.speehiveaihub.R
import coil.compose.AsyncImage
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
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshSilently()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "HIVE AI HUB",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = stringResource(R.string.nav_campaigns),
                            style = MaterialTheme.typography.displayLarge
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBackground
                )
            )
        },
        bottomBar = {
            BottomNavBar(
                selected = BottomNavItem.CAMPAIGNS,
                onHomeClick = onNavigateHome,
                onEventsClick = onNavigateEvents,
                onCampaignsClick = {},
                onNotificationsClick = onNavigateNotifications
            )
        }
    ) { padding ->

        PullToRefreshBox(
            isRefreshing = viewModel.isLoading,
            onRefresh = { viewModel.refresh() },
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
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
            .deep3DCard(elevation = 10.dp)
            .clickable { onClick() },
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
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )

                FigmaStatusBadge(campaign.status)
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
                text = formatCampaignDate(campaign.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}
