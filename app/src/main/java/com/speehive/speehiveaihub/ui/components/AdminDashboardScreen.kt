package com.speehive.speehiveaihub.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.unit.dp
import com.speehive.speehiveaihub.models.AdminUser
import com.speehive.speehiveaihub.viewmodel.AdminViewModel
import com.speehive.speehiveaihub.ui.theme.CardSurface
import com.speehive.speehiveaihub.ui.theme.CardBorder
import com.speehive.speehiveaihub.ui.theme.AppBackground
import com.speehive.speehiveaihub.ui.theme.TextPrimary
import com.speehive.speehiveaihub.ui.theme.TextSecondary
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import com.speehive.speehiveaihub.models.AuditLog
import com.speehive.speehiveaihub.ui.theme.PulseBlue
import com.speehive.speehiveaihub.ui.theme.PulseRed
import com.speehive.speehiveaihub.ui.theme.PulseGreen
import com.speehive.speehiveaihub.ui.theme.PulseGreenLight
import androidx.compose.ui.res.stringResource
import com.speehive.speehiveaihub.R
import com.speehive.speehiveaihub.utils.formatAuditDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onLogout: () -> Unit,
    onViewAuditLogs: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDesigner: () -> Unit,
    onNavigateToReviewer: () -> Unit
){

    var showCreateDialog by remember {
        mutableStateOf(false)
    }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.isLoading) {
        if (isRefreshing && !viewModel.isLoading) {
            isRefreshing = false
        }
    }

    if (showCreateDialog) {

        CreateUserDialog(

            onDismiss = {
                showCreateDialog = false
            },

            onCreateUser = { name, email, password, role ->

                viewModel.createUser(
                    name,
                    email,
                    password,
                    role
                )

                showCreateDialog = false
            }
        )
    }

    LaunchedEffect(viewModel.successMessage) {
        if (viewModel.successMessage != null) {
            kotlinx.coroutines.delay(3000L)
            viewModel.clearSuccessMessage()
        }
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

        topBar = {

            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.admin_header), style = MaterialTheme.typography.labelSmall)
                        Text(stringResource(R.string.dashboard_label), style = MaterialTheme.typography.displayLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextPrimary
                        )
                    }
                    TextButton(onClick = onLogout) {
                        Text(
                            stringResource(R.string.logout_btn),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            )
        },
        containerColor = AppBackground,

        floatingActionButton = {

            FloatingActionButton(
                onClick = {
                    showCreateDialog = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Reviewer"
                )
            }
        }

    ) { paddingValues ->

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.refresh()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            if (viewModel.isLoading && !isRefreshing) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

            } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),

                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                if (viewModel.successMessage != null) {
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
                                text = viewModel.successMessage ?: "",
                                color = TextPrimary,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                item {

                    Text(
                        text = "Admin Dashboard",
                        style = MaterialTheme.typography.headlineLarge
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )
                }

                item {

                    Column {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {

                            Box(modifier = Modifier.weight(1f)) {
                                StatsCard(
                                    title = "Users",
                                    value = viewModel.totalUsers.toString()
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                StatsCard(
                                    title = "Active",
                                    value = viewModel.activeUsers.toString()
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {

                            Box(modifier = Modifier.weight(1f)) {
                                StatsCard(
                                    title = "Inactive",
                                    value = viewModel.inactiveUsers.toString()
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                StatsCard(
                                    title = "Designers",
                                    value = viewModel.designerUsers.toString()
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {

                            Box(modifier = Modifier.width(170.dp)) {
                                StatsCard(
                                    title = "Admins",
                                    value = viewModel.adminUsers.toString()
                                )
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onViewAuditLogs,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PulseBlue
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.view_history_btn),
                                style = MaterialTheme.typography.titleSmall,
                                color = AppBackground
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = onNavigateToDesigner,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PulseGreen
                                )
                            ) {
                                Text(
                                    text = "Designer View",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = AppBackground
                                )
                            }

                            Button(
                                onClick = onNavigateToReviewer,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PulseBlue
                                )
                            ) {
                                Text(
                                    text = "Reviewer View",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = AppBackground
                                )
                            }
                        }
                    }
                }

                item {

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = stringResource(R.string.user_mgmt_label),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                item {

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )
                }
                items(
                    items = viewModel.users.filter { !it.role.equals("Admin", ignoreCase = true) },
                    key = { it.id }
                ) { user ->

                    UserCard(
                        user = user,

                        onActivate = {
                            viewModel.activateUser(user.id)
                        },

                        onDeactivate = {
                            viewModel.deactivateUser(user.id)
                        },

                        isProcessing = viewModel.isProcessing
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun StatsCard(
    title: String,
    value: String
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

            Text(
                text = title,
                color = TextSecondary,
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun UserCard(
    user: AdminUser,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit,
    isProcessing: Boolean = false
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
    ){

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = user.name,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )

            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Text(
                text = user.role,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text =
                    if (user.isActive)
                        "ACTIVE"
                    else
                        "INACTIVE",

                style = MaterialTheme.typography.labelMedium,
                color =
                    if (user.isActive)
                        PulseGreen
                    else
                        PulseRed
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (user.isActive) {

                    Button(
                        onClick = onDeactivate,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PulseRed,
                            disabledContainerColor = PulseRed.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        enabled = !isProcessing
                    ) {
                        Text(
                            text = "Deactivate",
                            style = MaterialTheme.typography.titleSmall,
                            color = AppBackground
                        )
                    }

                } else {

                    Button(
                        onClick = onActivate,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PulseGreen,
                            disabledContainerColor = PulseGreen.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        enabled = !isProcessing
                    ) {
                        Text(
                            text = "Activate",
                            style = MaterialTheme.typography.titleSmall,
                            color = AppBackground
                        )
                    }
                }
            }
        }
    }
}