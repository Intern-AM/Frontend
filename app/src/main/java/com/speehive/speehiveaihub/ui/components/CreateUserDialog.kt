package com.speehive.speehiveaihub.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.speehive.speehiveaihub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUserDialog(
    onDismiss: () -> Unit,
    onCreateUser: (
        name: String,
        email: String,
        password: String,
        role: String
    ) -> Unit
) {

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("User") }
    var expanded by remember { mutableStateOf(false) }

    val roles = listOf("User", "Designer", "Admin")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Create User",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
        },
        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = {
                        Text(
                            text = "Name",
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
                    value = email,
                    onValueChange = { email = it },
                    label = {
                        Text(
                            text = "Email",
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
                    value = password,
                    onValueChange = { password = it },
                    label = {
                        Text(
                            text = "Password",
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

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text(
                                text = "Role",
                                color = TextSecondary
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
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

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        containerColor = CardSurface
                    ) {
                        roles.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option,
                                        color = TextPrimary
                                    )
                                },
                                onClick = {
                                    role = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },

        confirmButton = {

            Button(
                onClick = {

                    onCreateUser(
                        name,
                        email,
                        password,
                        role
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PulseBlue
                )
            ) {
                Text(
                    text = "Create",
                    color = PureBlack
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
