package com.speehive.speehiveaihub.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import com.speehive.speehiveaihub.data.SessionManager
import com.speehive.speehiveaihub.navigation.Screen

@Composable
fun RoleGuard(
    sessionManager: SessionManager,
    navController: NavHostController,
    isAllowed: (String) -> Boolean,
    content: @Composable () -> Unit
) {
    val isLoggedIn = sessionManager.isLoggedIn()
    val role = sessionManager.getRole()

    if (!isLoggedIn) {
        LaunchedEffect(Unit) {
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    } else if (!isAllowed(role)) {
        LaunchedEffect(role) {
            val targetRoute = when {
                role.equals("Admin", ignoreCase = true) -> Screen.AdminDashboard.route
                role.equals("Designer", ignoreCase = true) -> Screen.DesignerDashboard.route
                else -> Screen.Dashboard.route
            }
            navController.navigate(targetRoute) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    } else {
        content()
    }
}
