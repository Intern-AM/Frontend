package com.speehive.speehiveaihub.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.speehive.speehiveaihub.R
import com.speehive.speehiveaihub.ui.theme.CardBorder
import com.speehive.speehiveaihub.ui.theme.PulseRed
import com.speehive.speehiveaihub.ui.theme.AppBackground
import com.speehive.speehiveaihub.ui.theme.TextPrimary
import com.speehive.speehiveaihub.ui.theme.TextSecondary
import com.speehive.speehiveaihub.viewmodel.LoginViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.speehive.speehiveaihub.ui.theme.CardSurface
import com.speehive.speehiveaihub.ui.theme.PulseBlue

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    LaunchedEffect(viewModel.isLoggedIn) {
        if (viewModel.isLoggedIn) {
            onLoginSuccess()
        }
    }
    var passwordVisible by remember {
        mutableStateOf(false)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(modifier = Modifier.height(80.dp))

            // Branding Section

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Image(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = "Hive AI Logo",
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {

                    Text(
                        text = "Hive AI",
                        style = MaterialTheme.typography.displayLarge,
                        color = TextPrimary
                    )

                    Text(
                        text = "INTELLIGENT SM AUTOMATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(96.dp))

            // Welcome Section

            Text(
                text = "Welcome back",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sign in to your workspace to continue.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Email Field

            OutlinedTextField(
                value = viewModel.email,
                onValueChange = {
                    viewModel.email = it
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                },
                label = {
                    Text(
                        text = "Email Address",
                        color = TextSecondary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
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

            Spacer(modifier = Modifier.height(20.dp))

            // Password Field

            OutlinedTextField(
                value = viewModel.password,
                onValueChange = {
                    viewModel.password = it
                },

                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                },

                trailingIcon = {
                    IconButton(
                        onClick = {
                            passwordVisible = !passwordVisible
                        }
                    ) {
                        Icon(
                            imageVector =
                                if (passwordVisible)
                                    Icons.Default.VisibilityOff
                                else
                                    Icons.Default.Visibility,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                },

                label = {
                    Text(
                        text = "Password",
                        color = TextSecondary
                    )
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),

                shape = RoundedCornerShape(20.dp),

                singleLine = true,

                visualTransformation =
                    if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),

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

            Spacer(modifier = Modifier.height(12.dp))

            if (viewModel.loginError != null) {

                Spacer(modifier = Modifier.height(16.dp))

                viewModel.loginError?.let { error ->
                    Text(
                        text = error,
                        color = PulseRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.onLoginClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                enabled = !viewModel.isLoading,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PulseBlue,
                    contentColor = AppBackground
                )
            ) {

                if (viewModel.isLoading) {

                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = AppBackground,
                        strokeWidth = 2.dp
                    )

                } else {

                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.titleLarge,
                        color = AppBackground
                    )
                }
            }
        }
    }
}
