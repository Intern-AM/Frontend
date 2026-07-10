package com.speehive.speehiveaihub.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.speehive.speehiveaihub.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateCredentialDialog(
    provider: String,
    currentIsActive: Boolean,
    currentExpiresAt: String?,
    onDismiss: () -> Unit,
    onSave: (
        accessToken: String,
        expiresAt: String?,
        isActive: Boolean
    ) -> Unit
) {
    var accessToken by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(currentIsActive) }
    
    // Parse current expiration for display
    var selectedDateText by remember {
        mutableStateOf(
            if (!currentExpiresAt.isNullOrBlank()) {
                try {
                    val parsed = java.time.OffsetDateTime.parse(currentExpiresAt)
                    parsed.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                } catch (e: Exception) {
                    ""
                }
            } else {
                ""
            }
        )
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var expiresAtIsoString by remember { mutableStateOf(currentExpiresAt) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (!expiresAtIsoString.isNullOrBlank()) {
                try {
                    java.time.OffsetDateTime.parse(expiresAtIsoString).toInstant().toEpochMilli()
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val instant = Instant.ofEpochMilli(selectedMillis)
                            val offsetDate = instant.atZone(ZoneId.of("UTC")).toOffsetDateTime()
                            expiresAtIsoString = offsetDate.toString()
                            selectedDateText = offsetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        } else {
                            expiresAtIsoString = null
                            selectedDateText = ""
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = PulseBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
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
                text = "Update $provider Credentials",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = accessToken,
                    onValueChange = { accessToken = it },
                    label = {
                        Text(
                            text = "Access Token",
                            color = TextSecondary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PulseBlue,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = PulseRed,
                        focusedContainerColor = CardSurface,
                        unfocusedContainerColor = CardSurface
                    )
                )

                OutlinedTextField(
                    value = selectedDateText,
                    onValueChange = {},
                    readOnly = true,
                    label = {
                        Text(
                            text = "Expiration Date (Optional)",
                            color = TextSecondary
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Select Date",
                            tint = TextSecondary,
                            modifier = Modifier.clickable { showDatePicker = true }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PulseBlue,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = PulseRed,
                        focusedContainerColor = CardSurface,
                        unfocusedContainerColor = CardSurface
                    )
                )

                if (selectedDateText.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            selectedDateText = ""
                            expiresAtIsoString = null
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Clear Date", color = PulseRed)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Status",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AppBackground,
                            checkedTrackColor = PulseGreen,
                            uncheckedThumbColor = AppBackground,
                            uncheckedTrackColor = CardBorder
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (accessToken.isNotBlank()) {
                        onSave(accessToken, expiresAtIsoString, isActive)
                    }
                },
                enabled = accessToken.isNotBlank(),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PulseBlue
                )
            ) {
                Text(
                    text = "Save",
                    color = AppBackground
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Cancel",
                    color = TextSecondary
                )
            }
        }
    )
}
