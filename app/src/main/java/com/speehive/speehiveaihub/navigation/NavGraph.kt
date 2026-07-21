package com.speehive.speehiveaihub.navigation
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.speehive.speehiveaihub.data.AuthManager
import com.speehive.speehiveaihub.data.AuthState
import com.speehive.speehiveaihub.repository.*
import com.speehive.speehiveaihub.ui.*
import com.speehive.speehiveaihub.ui.components.*
import com.speehive.speehiveaihub.viewmodel.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.speehive.speehiveaihub.data.SessionManager

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current

    val sessionManager = remember {
        SessionManager(context)
    }

    val authManager = remember {
        AuthManager(sessionManager)
    }

    val authState by authManager.authState.collectAsState()

    val activity = context as? ComponentActivity

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Unauthenticated -> {
                if (sessionManager.isLoggedIn()) {
                    sessionManager.clearSession()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            }
            is AuthState.Authenticated -> {}
        }
    }

    LaunchedEffect(activity?.intent) {
        val openNotifications = activity?.intent?.getBooleanExtra("open_notifications", false) ?: false
        if (openNotifications && sessionManager.isLoggedIn()) {
            navController.navigate(Screen.Notifications.route) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
            }
            activity?.intent?.removeExtra("open_notifications")
        }
    }

    val userRepository: UserRepository = remember {
        ApiUserRepository(sessionManager, authManager)
    }

    val campaignRepository = remember {
        ApiCampaignRepository(sessionManager, authManager, context)
    }

    val eventRepository = remember {
        ApiEventRepository(sessionManager, authManager, context)
    }

    val adminRepository = remember {
        ApiAdminRepository(sessionManager, authManager)
    }
    val auditRepository = remember {
        ApiAuditRepository(sessionManager, authManager)
    }

    val dashboardViewModel: DashboardViewModel = viewModel {
        DashboardViewModel(campaignRepository, eventRepository)
    }

    val credentialRepository = remember {
        ApiSocialMediaCredentialRepository(sessionManager, authManager)
    }

    val adminViewModel: AdminViewModel = viewModel {
        AdminViewModel(adminRepository, auditRepository, credentialRepository)
    }


    var currentUserName by remember {
        mutableStateOf(
            sessionManager.getUserName()
        )
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {

        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashComplete = {
                    if (sessionManager.isLoggedIn()) {
                        val role = sessionManager.getRole()
                        when {
                            role.equals("Admin", ignoreCase = true) ->
                                navController.navigate(Screen.AdminDashboard.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            else ->
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                        }
                    } else {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Login.route) {

            val viewModel: LoginViewModel = viewModel {
                LoginViewModel(userRepository)
            }

            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {

                    currentUserName =
                        viewModel.currentUser?.name ?: "User"

                    val role =
                        sessionManager.getRole()

                    when {
                        role.equals(
                            "Admin",
                            ignoreCase = true
                        ) -> {
                            navController.navigate(
                                Screen.AdminDashboard.route
                            ) {
                                popUpTo(Screen.Login.route) {
                                    inclusive = true
                                }
                            }
                            adminViewModel.loadUsers()
                            adminViewModel.loadAuditLogs()
                        }

                        else -> {
                            navController.navigate(
                                Screen.Dashboard.route
                            ) {
                                popUpTo(Screen.Login.route) {
                                    inclusive = true
                                }
                            }
                        }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            RoleGuard(
                sessionManager = sessionManager,
                navController = navController,
                isAllowed = { true }
            ) {
                val viewModel: DashboardViewModel = viewModel {
                    DashboardViewModel(campaignRepository, eventRepository)
                }

                DashboardScreen(
                    viewModel = viewModel,
                    userName = currentUserName,
                    onNavigateToEvents = {
                        navController.navigate(Screen.EventList.route)
                    },
                    onNavigateToCampaigns = {
                        navController.navigate(Screen.CampaignList.route)
                    },
                    onNavigateToNotifications = {
                        navController.navigate(Screen.Notifications.route)
                    },
                    onNavigateToCampaignDetail = { campaignId ->
                        navController.navigate(
                            Screen.CampaignDetail.createRoute(campaignId)
                        )
                    },
                    onLogout = {
                        authManager.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                    isAdmin = sessionManager.getRole().equals("Admin", ignoreCase = true),
                    onNavigateToAdmin = {
                        navController.navigate(Screen.AdminDashboard.route) {
                            popUpTo(Screen.AdminDashboard.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
        composable(Screen.AdminDashboard.route) {
            RoleGuard(
                sessionManager = sessionManager,
                navController = navController,
                isAllowed = { it.equals("Admin", ignoreCase = true) }
            ) {
                AdminDashboardScreen(
                    viewModel = adminViewModel,
                    onLogout = {
                        authManager.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                    onViewAuditLogs = {
                        navController.navigate(
                            Screen.AuditLogs.route
                        )
                    },
                    onNavigateToSettings = {
                        navController.navigate(
                            Screen.AdminSettings.route
                        )
                    },
                    onNavigateToReviewer = {
                        navController.navigate(Screen.Dashboard.route)
                    }
                )
            }
        }
        composable(Screen.AdminSettings.route) {
            RoleGuard(
                sessionManager = sessionManager,
                navController = navController,
                isAllowed = { it.equals("Admin", ignoreCase = true) }
            ) {
                AdminSettingsScreen(
                    viewModel = adminViewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
        composable(Screen.EventList.route) {
            RoleGuard(
                sessionManager = sessionManager,
                navController = navController,
                isAllowed = { !it.equals("Designer", ignoreCase = true) }
            ) {
                val viewModel: EventViewModel = viewModel {
                    EventViewModel(eventRepository)
                }

                EventListScreen(
                    viewModel = viewModel,
                    onNavigateHome = {
                        navController.navigate(Screen.Dashboard.route) {
                            launchSingleTop = true
                            popUpTo(Screen.Dashboard.route)
                        }
                    },

                    onNavigateCampaigns = {
                        navController.navigate(Screen.CampaignList.route) {
                            launchSingleTop = true
                        }
                    },

                    onNavigateNotifications = {
                        navController.navigate(Screen.Notifications.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
        composable(Screen.CampaignList.route) {
            RoleGuard(
                sessionManager = sessionManager,
                navController = navController,
                isAllowed = { !it.equals("Designer", ignoreCase = true) }
            ) {
                CampaignListScreen(
                    viewModel = dashboardViewModel,
                    onNavigateHome = {
                        navController.navigate(Screen.Dashboard.route) {
                            launchSingleTop = true
                            popUpTo(Screen.Dashboard.route)
                        }
                    },

                    onNavigateEvents = {
                        navController.navigate(Screen.EventList.route) {
                            launchSingleTop = true
                        }
                    },

                    onNavigateNotifications = {
                        navController.navigate(Screen.Notifications.route) {
                            launchSingleTop = true
                        }
                    },

                    onCampaignClick = { id ->
                        navController.navigate(
                            Screen.CampaignDetail.createRoute(id)
                        )
                    }
                )
            }
        }
        composable(
            route = Screen.CampaignDetail.route,
            arguments = listOf(
                navArgument("campaignId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            RoleGuard(
                sessionManager = sessionManager,
                navController = navController,
                isAllowed = { !it.equals("Designer", ignoreCase = true) }
            ) {
                val campaignId =
                    backStackEntry.arguments?.getString("campaignId") ?: ""

                val viewModel: CampaignDetailViewModel = viewModel(
                    key = campaignId
                ) {
                    CampaignDetailViewModel(
                        campaignRepository,
                        eventRepository,
                        sessionManager
                    )
                }

                CampaignDetailScreen(
                    campaignId = campaignId,
                    viewModel = viewModel,
                    onBack = {
                        dashboardViewModel.refresh()
                        navController.popBackStack()
                    }
                )
            }
        }
        composable(Screen.AuditLogs.route) {
            RoleGuard(
                sessionManager = sessionManager,
                navController = navController,
                isAllowed = { it.equals("Admin", ignoreCase = true) }
            ) {
                AuditLogScreen(
                    viewModel = adminViewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
        composable(Screen.Notifications.route) {
            RoleGuard(
                sessionManager = sessionManager,
                navController = navController,
                isAllowed = { !it.equals("Designer", ignoreCase = true) }
            ) {
                val viewModel: NotificationViewModel = viewModel {
                    NotificationViewModel(
                        campaignRepository,
                        eventRepository,
                        sessionManager
                    )
                }

                NotificationScreen(
                    viewModel = viewModel,
                    onNavigateHome = {
                        navController.navigate(Screen.Dashboard.route) {
                            launchSingleTop = true
                            popUpTo(Screen.Dashboard.route)
                        }
                    },

                    onNavigateEvents = {
                        navController.navigate(Screen.EventList.route) {
                            launchSingleTop = true
                            popUpTo(Screen.EventList.route)
                        }
                    },

                    onNavigateCampaigns = {
                        navController.navigate(Screen.CampaignList.route) {
                            launchSingleTop = true
                            popUpTo(Screen.CampaignList.route)
                        }
                    }
                )
            }
        }
    }
}
