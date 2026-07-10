package com.speehive.speehiveaihub.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.speehive.speehiveaihub.models.SocialMediaCredential
import com.speehive.speehiveaihub.ui.theme.*
import com.speehive.speehiveaihub.utils.formatAuditDate
import com.speehive.speehiveaihub.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
    var selectedCredentialToUpdate by remember {
        mutableStateOf<SocialMediaCredential?>(null)
    }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.isLoading) {
        if (isRefreshing && !viewModel.isLoading) {
            isRefreshing = false
        }
    }

    LaunchedEffect(viewModel.successMessage) {
        if (viewModel.successMessage != null) {
            kotlinx.coroutines.delay(3000L)
            viewModel.clearSuccessMessage()
        }
    }

    LaunchedEffect(viewModel.errorMessage) {
        if (viewModel.errorMessage != null) {
            kotlinx.coroutines.delay(5000L)
            viewModel.clearError()
        }
    }

    selectedCredentialToUpdate?.let { cred ->
        UpdateCredentialDialog(
            provider = cred.provider,
            currentIsActive = cred.isActive,
            currentExpiresAt = cred.expiresAt,
            onDismiss = { selectedCredentialToUpdate = null },
            onSave = { accessToken, expiresAt, isActive ->
                viewModel.updateCredential(
                    provider = cred.provider,
                    accessToken = accessToken,
                    expiresAt = expiresAt,
                    isActive = isActive
                )
                selectedCredentialToUpdate = null
            }
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.loadCredentialsSilently()
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
                        Text("ADMIN", style = MaterialTheme.typography.labelSmall)
                        Text("Settings", style = MaterialTheme.typography.displayLarge)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.loadCredentials()
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

                    if (viewModel.errorMessage != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = PulseRedLight
                                ),
                                border = BorderStroke(1.dp, PulseRed)
                            ) {
                                Text(
                                    text = viewModel.errorMessage ?: "",
                                    color = TextPrimary,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    val expiringCreds = viewModel.getExpiringCredentials()
                    if (expiringCreds.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = PulseAmberLight
                                ),
                                border = BorderStroke(1.dp, PulseAmber)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "API CREDENTIALS EXPIRING SOON",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PulseAmber
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    expiringCreds.forEach { cred ->
                                        val formattedExpiry = if (cred.expiresAt != null) formatAuditDate(cred.expiresAt) else ""
                                        Text(
                                            text = "• ${cred.provider} key expires on $formattedExpiry",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Social Media Credentials",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    if (viewModel.credentials.isEmpty()) {
                        item {
                            Text(
                                text = "No credentials configured.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    } else {
                        items(viewModel.credentials) { credential ->
                            CredentialSettingsCard(
                                credential = credential,
                                onUpdateClick = { selectedCredentialToUpdate = credential }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CredentialSettingsCard(
    credential: SocialMediaCredential,
    onUpdateClick: () -> Unit
) {
    val isConfigured = !credential.maskedToken.isNullOrBlank()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        ),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = credential.provider,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isConfigured) "Configured" else "Not Configured",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isConfigured) PulseGreen else TextSecondary
                    )
                }
                Button(
                    onClick = onUpdateClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isConfigured) PulseBlue else PulseGreen
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = if (isConfigured) "Update" else "Add Token",
                        style = MaterialTheme.typography.titleSmall,
                        color = AppBackground
                    )
                }
            }

            if (isConfigured) {
                Spacer(modifier = Modifier.height(8.dp))
                val shortToken = if (credential.maskedToken.length > 8) {
                    credential.maskedToken.take(4) + "****" + credential.maskedToken.takeLast(4)
                } else {
                    "****"
                }
                Text(
                    text = "Token: $shortToken",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            if (isConfigured && !credential.expiresAt.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Expires: ${formatAuditDate(credential.expiresAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            if (!credential.updatedAt.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Last Updated: ${formatAuditDate(credential.updatedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
