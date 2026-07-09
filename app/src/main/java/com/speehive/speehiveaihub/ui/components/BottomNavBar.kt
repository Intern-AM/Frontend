package com.speehive.speehiveaihub.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.speehive.speehiveaihub.R
import com.speehive.speehiveaihub.ui.theme.*

enum class BottomNavItem {
    HOME,
    EVENTS,
    CAMPAIGNS,
    NOTIFICATIONS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavBar(
    selected: BottomNavItem,
    visibleItems: List<BottomNavItem> = BottomNavItem.entries,
    notificationCount: Int = 0,
    onHomeClick: () -> Unit,
    onEventsClick: () -> Unit,
    onCampaignsClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = PulseBlue,
        selectedTextColor = PulseBlue,
        unselectedIconColor = TextSecondary,
        unselectedTextColor = TextSecondary,
        indicatorColor = PulseBlue.copy(alpha = 0.12f)
    )

    NavigationBar(
        containerColor = CardSurface
    ) {
        if (BottomNavItem.HOME in visibleItems) {
            NavigationBarItem(
                selected = selected == BottomNavItem.HOME,
                onClick = onHomeClick,
                icon = {
                    Icon(Icons.Default.Home, contentDescription = "Home")
                },
                label = {
                    Text(stringResource(R.string.nav_home))
                },
                colors = itemColors
            )
        }

        if (BottomNavItem.EVENTS in visibleItems) {
            NavigationBarItem(
                selected = selected == BottomNavItem.EVENTS,
                onClick = onEventsClick,
                icon = {
                    Icon(Icons.Default.Event, contentDescription = "Events")
                },
                label = {
                    Text(stringResource(R.string.nav_events))
                },
                colors = itemColors
            )
        }

        if (BottomNavItem.CAMPAIGNS in visibleItems) {
            NavigationBarItem(
                selected = selected == BottomNavItem.CAMPAIGNS,
                onClick = onCampaignsClick,
                icon = {
                    Icon(Icons.Default.Campaign, contentDescription = "Campaigns")
                },
                label = {
                    Text(stringResource(R.string.nav_campaigns))
                },
                colors = itemColors
            )
        }

        if (BottomNavItem.NOTIFICATIONS in visibleItems) {
            NavigationBarItem(
                selected = selected == BottomNavItem.NOTIFICATIONS,
                onClick = onNotificationsClick,
                icon = {
                    BadgedBox(
                        badge = {
                            if (notificationCount > 0) {
                                Badge(
                                    containerColor = PulseRed
                                ) {
                                    Text(
                                        text = notificationCount.toString(),
                                        color = AppBackground
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Inbox"
                        )
                    }
                },
                label = {
                    Text(stringResource(R.string.nav_inbox))
                },
                colors = itemColors
            )
        }
    }
}
