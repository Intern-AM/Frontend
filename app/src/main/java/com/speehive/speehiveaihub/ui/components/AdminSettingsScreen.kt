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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import com.speehive.speehiveaihub.utils.getFileName
import com.speehive.speehiveaihub.models.ValidationError
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Check
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.res.stringResource
import com.speehive.speehiveaihub.R


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

    val context = androidx.compose.ui.platform.LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.getFileName(context)
            viewModel.selectFile(uri, fileName)
        }
    }

    var showErrorsDialog by remember { mutableStateOf(false) }
    var errorsListToShow by remember { mutableStateOf<List<ValidationError>>(emptyList()) }

    var showImportSuccessCard by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.importResult) {
        val result = viewModel.importResult
        if (result?.success == true) {
            showImportSuccessCard = true
            kotlinx.coroutines.delay(5000L)
            showImportSuccessCard = false
            kotlinx.coroutines.delay(1000L) // Allow fade-out animation to complete
            viewModel.clearImportStatus()
        } else {
            showImportSuccessCard = false
        }
    }


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

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
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
                                Text(
                                    text = stringResource(R.string.bulk_import_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = stringResource(R.string.bulk_import_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // File selection status / chooser
                                if (viewModel.selectedFileName == null) {
                                    Button(
                                        onClick = { filePickerLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = PulseBlue
                                        )
                                    ) {
                                        Text(
                                            text = stringResource(R.string.select_file_btn),
                                            style = MaterialTheme.typography.titleSmall,
                                            color = AppBackground
                                        )
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Selected File:",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = TextSecondary
                                            )
                                            Text(
                                                text = viewModel.selectedFileName ?: "",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = TextPrimary,
                                                maxLines = 1
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.clearSelectedFile() },
                                            enabled = !viewModel.isValidatingFile && !viewModel.isImportingFile
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear selected file",
                                                tint = PulseRed
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Action Buttons and Progress Indicators
                                    if (viewModel.isValidatingFile) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                strokeWidth = 2.5.dp,
                                                color = PulseBlue
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = "Validating events file...",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextPrimary
                                            )
                                        }
                                    } else if (viewModel.isImportingFile) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                strokeWidth = 2.5.dp,
                                                color = PulseGreen
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = "Importing events to Google Calendar...",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextPrimary
                                            )
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            // Validate Button
                                            Button(
                                                onClick = { viewModel.validateFile() },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(20.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = PulseBlue
                                                ),
                                                enabled = viewModel.validationResult == null
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.validate_btn),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = AppBackground
                                                )
                                            }

                                            // Import Button
                                            val isImportEnabled = viewModel.validationResult?.isValid == true
                                            Button(
                                                onClick = { viewModel.importFile() },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(20.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = PulseGreen,
                                                    disabledContainerColor = PulseGreen.copy(alpha = 0.5f)
                                                ),
                                                enabled = isImportEnabled
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.import_btn),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = AppBackground
                                                )
                                            }
                                        }
                                    }
                                }

                                // Error/Validation Status Messages
                                viewModel.importErrorMessage?.let { errMsg ->
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = PulseRed.copy(alpha = 0.1f)
                                        ),
                                        border = BorderStroke(1.dp, PulseRed.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = "Error icon",
                                                tint = PulseRed,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = errMsg,
                                                color = PulseRed,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }

                                // Validation Success animated display
                                AnimatedVisibility(
                                    visible = viewModel.validationResult?.isValid == true,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    viewModel.validationResult?.let { result ->
                                        if (result.isValid) {
                                            Column {
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = PulseGreen.copy(alpha = 0.1f)
                                                    ),
                                                    border = BorderStroke(1.dp, PulseGreen.copy(alpha = 0.3f))
                                                ) {
                                                    Column(modifier = Modifier.padding(12.dp)) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = "Success",
                                                                tint = PulseGreen,
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(10.dp))
                                                            Text(
                                                                text = result.message ?: "Validation successful.",
                                                                color = PulseGreen,
                                                                style = MaterialTheme.typography.titleSmall
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Text(
                                                            text = "Total: ${result.totalRows} | Valid: ${result.validRows}",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = TextPrimary
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Validation Failure Reports
                                viewModel.validationResult?.let { result ->
                                    if (!result.isValid) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = PulseRed.copy(alpha = 0.1f)
                                            ),
                                            border = BorderStroke(1.dp, PulseRed.copy(alpha = 0.3f))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Warning,
                                                        contentDescription = "Failed",
                                                        tint = PulseRed,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Text(
                                                        text = "Validation failed.",
                                                        color = PulseRed,
                                                        style = MaterialTheme.typography.titleSmall
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "Total Rows: ${result.totalRows}\nValid Rows: ${result.validRows}\nInvalid Rows: ${result.invalidRows}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = TextPrimary
                                                )

                                                val errors = result.errors
                                                if (!errors.isNullOrEmpty()) {
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    Button(
                                                        onClick = {
                                                            errorsListToShow = errors
                                                            showErrorsDialog = true
                                                        },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = PulseRed
                                                        ),
                                                        shape = RoundedCornerShape(20.dp),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text(
                                                            text = stringResource(R.string.view_errors_btn),
                                                            style = MaterialTheme.typography.titleSmall,
                                                            color = AppBackground
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Success animated display
                                AnimatedVisibility(
                                    visible = showImportSuccessCard,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    viewModel.importResult?.let { result ->
                                        if (result.success == true) {
                                            Column {
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = PulseGreen.copy(alpha = 0.1f)
                                                    ),
                                                    border = BorderStroke(1.dp, PulseGreen.copy(alpha = 0.3f))
                                                ) {
                                                    Column(modifier = Modifier.padding(12.dp)) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = "Success",
                                                                tint = PulseGreen,
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(10.dp))
                                                            Text(
                                                                text = result.message ?: "All events imported successfully.",
                                                                color = PulseGreen,
                                                                style = MaterialTheme.typography.titleSmall
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Text(
                                                            text = "Total Rows: ${result.totalRows} | Imported: ${result.imported}",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = TextPrimary
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Import Failures / Re-Validation Failures Report
                                viewModel.importResult?.let { result ->
                                    if (result.success == false) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = PulseRed.copy(alpha = 0.1f)
                                            ),
                                            border = BorderStroke(1.dp, PulseRed.copy(alpha = 0.3f))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Warning,
                                                        contentDescription = "Failed",
                                                        tint = PulseRed,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Text(
                                                        text = result.message ?: "Import execution failed.",
                                                        color = PulseRed,
                                                        style = MaterialTheme.typography.titleSmall
                                                    )
                                                }
                                            }
                                        }
                                    } else if (result.isValid == false) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = PulseRed.copy(alpha = 0.1f)
                                            ),
                                            border = BorderStroke(1.dp, PulseRed.copy(alpha = 0.3f))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Warning,
                                                        contentDescription = "Failed",
                                                        tint = PulseRed,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Text(
                                                        text = "Re-validation failed prior to import.",
                                                        color = PulseRed,
                                                        style = MaterialTheme.typography.titleSmall
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "Total Rows: ${result.totalRows}\nValid Rows: ${result.validRows}\nInvalid Rows: ${result.invalidRows}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = TextPrimary
                                                )

                                                val errors = result.errors
                                                if (!errors.isNullOrEmpty()) {
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    Button(
                                                        onClick = {
                                                            errorsListToShow = errors
                                                            showErrorsDialog = true
                                                        },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = PulseRed
                                                        ),
                                                        shape = RoundedCornerShape(20.dp),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text(
                                                            text = stringResource(R.string.view_errors_btn),
                                                            style = MaterialTheme.typography.titleSmall,
                                                            color = AppBackground
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
                }
            }
        }
        if (showErrorsDialog) {
            ValidationErrorsDialog(
                errors = errorsListToShow,
                onDismiss = { showErrorsDialog = false }
            )
        }
    }
}

@Composable
private fun ValidationErrorsDialog(
    errors: List<ValidationError>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "File Validation Errors",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(errors) { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CardSurface
                        ),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Row ${error.row}" + if (!error.title.isNullOrBlank()) " - ${error.title}" else "",
                                style = MaterialTheme.typography.titleSmall,
                                color = PulseRed
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = error.reason,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", style = MaterialTheme.typography.titleSmall, color = PulseBlue)
            }
        }
    )
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
