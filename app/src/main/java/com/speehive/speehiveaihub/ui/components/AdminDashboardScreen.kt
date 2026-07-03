package com.speehive.speehiveaihub.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.speehive.speehiveaihub.models.AdminUser
import com.speehive.speehiveaihub.viewmodel.AdminViewModel
import com.speehive.speehiveaihub.ui.theme.CardSurface
import com.speehive.speehiveaihub.ui.theme.CardBorder
import com.speehive.speehiveaihub.ui.theme.PureBlack
import com.speehive.speehiveaihub.ui.theme.TextPrimary
import com.speehive.speehiveaihub.ui.theme.TextSecondary
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import com.speehive.speehiveaihub.models.AuditLog
import com.speehive.speehiveaihub.ui.theme.PulseBlue
import com.speehive.speehiveaihub.ui.theme.PulseRed
import com.speehive.speehiveaihub.ui.theme.PulseGreen
import com.speehive.speehiveaihub.ui.theme.PulseGreenLight
import com.speehive.speehiveaihub.utils.formatAuditDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onLogout: () -> Unit,
    onViewAuditLogs: () -> Unit
){

    var showCreateDialog by remember {
        mutableStateOf(false)
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
            kotlinx.coroutines.delay(3000)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(

        topBar = {

            TopAppBar(
                title = {
                    Column {
                        Text("ADMIN", style = MaterialTheme.typography.labelSmall)
                        Text("Dashboard", style = MaterialTheme.typography.displayLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PureBlack),
                actions = {
                    TextButton(onClick = onLogout) {
                        Text(
                            "Logout",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            )
        },
        containerColor = PureBlack,

        floatingActionButton = {

            FloatingActionButton(
                onClick = {
                    showCreateDialog = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create User"
                )
            }
        }

    ) { paddingValues ->

        if (viewModel.isLoading) {

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
                    .padding(paddingValues)
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

                    Button(
                        onClick = onViewAuditLogs,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PulseBlue
                        )
                    ) {
                        Text(
                            text = "View Activity History",
                            style = MaterialTheme.typography.titleSmall,
                            color = PureBlack
                        )
                    }
                }

                item {

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "User Management",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                item {

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )
                }
                items(viewModel.users.filter { !it.role.equals("Admin", ignoreCase = true) }) { user ->

                    UserCard(
                        user = user,

                        onActivate = {
                            viewModel.activateUser(user.id)
                        },

                        onDeactivate = {
                            viewModel.deactivateUser(user.id)
                        }
                    )
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
fun AuditLogCard(log: AuditLog) {

    val actionColor =
        when (log.action) {
            "CREATE_USER" -> PulseBlue
            "ACTIVATE_USER" -> PulseGreen
            "DEACTIVATE_USER" -> PulseRed
            "APPROVE_POST" -> PulseGreen
            "REJECT_POST" -> PulseRed
            "CANCEL_EVENT" -> PulseRed
            "GOOGLE_CALENDAR_UPDATED" -> PulseBlue
            "CREATE_CAMPAIGN" -> PulseBlue
            "APPROVE_EVENT" -> PulseGreen
            "REJECT_EVENT" -> PulseRed
            "POST_CAMPAIGN" -> PulseGreen
            else -> TextPrimary
        }
    val actionTitle =
        when(log.action) {
            "CREATE_USER" -> "Created User"
            "ACTIVATE_USER" -> "Activated User"
            "DEACTIVATE_USER" -> "Deactivated User"
            "APPROVE_POST" -> "Approved Campaign"
            "REJECT_POST" -> "Rejected Campaign"
            "CANCEL_EVENT" -> "Cancelled Event"
            "GOOGLE_CALENDAR_UPDATED" -> "Calendar Updated"
            "CREATE_CAMPAIGN" -> "Created Campaign"
            "APPROVE_EVENT" -> "Approved Event"
            "REJECT_EVENT" -> "Rejected Event"
            "POST_CAMPAIGN" -> "Posted Campaign"
            else -> log.action.replace("_", " ").lowercase()
                .replaceFirstChar { it.uppercase() }
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
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = actionTitle,
                style = MaterialTheme.typography.titleMedium,
                color = actionColor
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = log.details,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = formatAuditDate(log.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
@Composable
private fun UserCard(
    user: AdminUser,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit
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
                            containerColor = PulseRed
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "Deactivate",
                            style = MaterialTheme.typography.titleSmall,
                            color = PureBlack
                        )
                    }

                } else {

                    Button(
                        onClick = onActivate,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PulseGreen
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "Activate",
                            style = MaterialTheme.typography.titleSmall,
                            color = PureBlack
                        )
                    }
                }
            }
        }
    }
}