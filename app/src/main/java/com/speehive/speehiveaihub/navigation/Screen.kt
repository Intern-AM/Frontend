package com.speehive.speehiveaihub.navigation

sealed class Screen(val route: String) {

    object Login : Screen("login")

    object Dashboard : Screen("dashboard")

    object AdminDashboard : Screen("admin_dashboard")

    object EventList : Screen("events")

    object CampaignList : Screen("campaigns")

    object AuditLogs : Screen("audit_logs")

    object CampaignDetail : Screen("campaign_detail/{campaignId}") {

        fun createRoute(
            campaignId: String
        ) = "campaign_detail/$campaignId"
    }

    object Notifications : Screen("notifications")

    object DesignerDashboard : Screen("designer_dashboard")
}