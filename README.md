# BuzzHive — v2 Redesign Branch

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-blue?style=flat&logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3%20%2B%203D%20Glassmorphism-4285F4?style=flat&logo=android)
![Min SDK](https://img.shields.io/badge/Min%20SDK-26%20(Android%208.0)-brightgreen)
![Target SDK](https://img.shields.io/badge/Target%20SDK-37-green)

**BuzzHive (`feature/v2-redesign`)** is an Android mobile administration client built with Jetpack Compose, Material 3, and Kotlin Coroutines. It serves as a central hub for managing AI-driven marketing campaigns, social media event schedules, multi-platform post publishing, creative poster collateral, and system-wide audit logging.

This `feature/v2-redesign` branch features a streamlined **2-role architecture** (consolidating Designer tasks directly into Reviewers), dynamic 3D glassmorphism styling, hybrid DNS resolution, and granular per-platform scheduling.

---

## What This Project Does

BuzzHive automates the end-to-end lifecycle of corporate social media marketing:
1. **AI Campaign Orchestration**: Ingests event details, generates campaign copy, suggests hashtags, and attaches AI-generated poster prompt concepts.
2. **Consolidated Review & Asset Approval**: Allows reviewers to edit post text, approve/reject generated campaigns, and upload or replace final poster assets directly in a unified view.
3. **Granular Multi-Platform Scheduling**: Tracks and schedules posts independently across individual social channels (**LinkedIn**, **Instagram**, **MS Teams**, **WhatsApp**) with past-date prevention and platform status tracking (`Pending`, `Posted`, `Failed`).
4. **Resilient Network Layer**: Maintains continuous API connectivity using custom token refresh mechanisms and hybrid DNS failover for local/cellular network environments.
5. **Background Alerts**: Periodically polls pending schedules using Android WorkManager to deliver deep-linked push notifications to device notification trays.

---

## Core Tech Stack

| Category | Technology | Usage / Details |
| :--- | :--- | :--- |
| **Language** | Kotlin 1.9.24 | Async concurrency via Coroutines & reactive state with `StateFlow` |
| **UI Framework** | Jetpack Compose (Material 3) | Declarative UI, responsive layouts, custom 3D glassmorphism (`ThreeDEffects.kt`) |
| **Architecture** | MVVM + Repository Pattern | Decoupled UI state, ViewModels, repository contracts, and network data sources |
| **Networking** | Retrofit 2 + Gson | REST API integration with custom OkHttp interceptors (`AuthInterceptor`, `TokenValidationInterceptor`) |
| **Network Resilience** | Hybrid DNS over HTTPS | System DNS resolution with Cloudflare DoH (`1.1.1.1`) fallback for domain connectivity |
| **Image Loading** | Coil Compose | Asynchronous image loading with fallback placeholders and server Base URL rewriters |
| **Date & Time** | Java 8 `OffsetDateTime` | Timezone formatting standardized to Indian Standard Time (`Asia/Kolkata`) via `DateUtils` |
| **Background Tasks** | Android WorkManager | Background polling workers (`NotificationWorker`) for alerts and deep-linking |
| **Build Tooling** | Gradle 8.x + Version Catalogs | Dependency management via `gradle/libs.versions.toml` |

---

## Architecture & System Design

The application follows the **Model-View-ViewModel (MVVM)** architectural pattern:

```
┌────────────────────────────────────────────────────────────────────────┐
│                          Jetpack Compose UI                            │
│  (AdminDashboardScreen, DashboardScreen, CampaignDetailScreen,         │
│   EventListScreen, AdminSettingsScreen, etc.)                          │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ StateFlow / UI State
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                               ViewModel                                │
│   (DashboardViewModel, CampaignDetailViewModel, AdminViewModel, etc.)   │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Repository Contracts
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                            Repository Layer                            │
│     (ApiCampaignRepository, ApiEventRepository, ApiAdminRepository)    │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
               ┌────────────────────┴────────────────────┐
               ▼                                         ▼
┌──────────────────────────────┐          ┌──────────────────────────────┐
│     SpeehiveApiService       │          │        SessionManager        │
│  (Retrofit + Hybrid DNS)     │          │    (Encrypted Session)       │
└──────────────────────────────┘          └──────────────────────────────┘
```

### Consolidated 2-Role System (`v2-redesign`)

In the `v2-redesign` branch, user roles are streamlined to eliminate workflow bottlenecks:

#### Admin
* **User Management**: Create user accounts with `Admin` or `Reviewer` roles; activate or deactivate user profiles.
* **Audit Logging**: View system audit logs with date filtering and search capabilities.
* **View Mode Switcher**: Toggle between Admin and Reviewer views using the interactive `ViewModeSwitcher` slider.
* **Admin Settings Panel**: Accessible via a gear icon on the dashboard, consolidating configuration and data ingestion tools:
  * **Social Media Credentials**: Configure and update OAuth access credentials and check expiration dates for connected social networks (LinkedIn, Instagram, MS Teams, WhatsApp). Shows warn states for expiring keys.
  * **Bulk Event Import**: Ingest events in bulk by uploading Excel files (`.xlsx`), with layout validation checks and a visible errors dialog interface.
* **Dashboard Overview**: View live stats on the admin dashboard, including counts of total users, active users, inactive users, designers, and admins.

#### Reviewer (Unified Reviewer + Designer)
* **Unified Feed**: Manage pending events and generated campaigns inline in a single stream (`EventListScreen`), removing the need for a separate campaigns screen.
* **Advanced Filters**: Seamlessly filter events by Month (Jan-Dec), Year, and Status ("All", "Pending", "Generated", "Rejected", "Completed") using a custom `SlidingStatusFilter` with dynamic routing integration.
* **Action Confirmations**: Guarded state transitions via a reusable `ConfirmationDialog` for lifecycle operations (Approve, Reject, Restore Event).
* **Pull-to-Refresh**: Refresh screens instantly with a pull gesture (`PullToRefreshBox`) implemented on core dashboards.
* **Inline Poster Management**: Upload or replace event and campaign posters directly within the campaign detail and review cards.
* **Multi-Platform Social Scheduling**: Set posting schedules independently per social platform (`LinkedIn`, `Instagram`, `Teams`, `Whatsapp`) with past-date prevention.
* **Copy AI Prompt**: Tap to copy image generation prompts directly to the system clipboard.
* **Clean Layout**: Top app bar is hidden on Reviewer dashboards to maximize content viewing space.

---

## Configuration & Environment Settings

### Codebase Configuration (`RetrofitClient.kt`)

| Variable / Constant | Default Value | Description |
| :--- | :--- | :--- |
| `BASE_URL` | `https://debian.tail72ffe0.ts.net/` | Root HTTP API endpoint for backend services |
| `CONNECT_TIMEOUT` | `30 Seconds` | OkHttp connection timeout limit |
| `READ_TIMEOUT` | `30 Seconds` | OkHttp socket read timeout limit |
| `WRITE_TIMEOUT` | `30 Seconds` | OkHttp socket write timeout limit |
| `DNS_OVER_HTTPS_URL` | `https://1.1.1.1/dns-query` | Cloudflare DoH endpoint for DNS bypass fallback |

### Local Session & Preferences (`SessionManager.kt`)

| Key | Storage | Description |
| :--- | :--- | :--- |
| `KEY_TOKEN` | SharedPreferences | Bearer JWT authentication token injected into API headers |
| `KEY_ROLE` | SharedPreferences | Active user role (`Admin` or `Reviewer`) used by `RoleGuard` |
| `KEY_USER_NAME` | SharedPreferences | Display name of the currently authenticated user |
| `KEY_USER_ID` | SharedPreferences | Unique identifier of the logged-in user |
| `KEY_IS_LOGGED_IN` | SharedPreferences | Boolean session state check |

---

## Step-by-Step Local Setup & Installation Guide

### Prerequisites
* **Android Studio**: Ladybug (2024.2.1) or newer.
* **JDK**: Java 17 (bundled Android Studio JBR recommended).
* **Android SDK**: API 37 (Build-Tools 35.0.0+).
* **Minimum Target Device**: Android 8.0 (API Level 26 / Oreo).

### 1. Clone & Checkout the Branch
```bash
git clone https://github.com/Intern-AM/Frontend.git
cd Frontend
git checkout feature/v2-redesign
```

### 2. Configure Backend Endpoint URL
If connecting to a custom local backend or IP, update `BASE_URL` in `app/src/main/java/com/speehive/speehiveaihub/network/RetrofitClient.kt`:
```kotlin
object RetrofitClient {
    private const val BASE_URL = "https://debian.tail72ffe0.ts.net/"
}
```

### 3. Build & Run via Command Line

#### Windows (PowerShell / CMD)
```powershell
cmd /c "set ""JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"" && gradlew assembleDebug"
```

#### macOS / Linux
```bash
export JAVA_HOME=/Applications/Android\ Studio.app/Contents/jbr/Contents/Home
./gradlew assembleDebug
```

#### Install Directly to Connected Device / Emulator
```powershell
./gradlew installDebug
```

#### Run Unit Tests
```powershell
./gradlew test
```

---

## Usage Examples & Workflows

### 1. Bulk Event Import
1. Log in as an **Admin**.
2. Tap the **Settings** gear icon in the top app bar.
3. Scroll to **Bulk Event Import** and tap **Select Excel File**.
4. Choose an `.xlsx` file from local device files.
5. Tap **Validate File**.
6. If formatting or data errors occur, tap **View Validation Errors** to view precise row/column failure reasons.
7. If the file is valid, tap **Import Events** to ingest them. A temporary success banner will verify the action.

### 2. Events & Campaigns Filtering
1. Log in as a **Reviewer**.
2. Navigate to the **Events** screen.
3. Use the top dropdowns to select a **Month** (e.g. "July") and/or **Year** (e.g. "2026").
4. Swipe or tap the sliding status bar (`SlidingStatusFilter`) to toggle between:
   * **All**: Lists all events and campaigns.
   * **Pending**: Displays raw events waiting for AI campaign generation.
   * **Generated**: Displays campaigns that have been compiled and scheduled.
   * **Rejected**: Lists campaigns rejected by reviewer.
   * **Completed**: Shows fully published events.
5. Drag down on the screen to trigger **Pull-to-Refresh** to fetch the latest state from the backend.

### 3. Per-Platform Schedule Editing
1. Log in as a **Reviewer**.
2. Navigate to a campaign detail view via the `CampaignDetailScreen`.
3. Tap **Edit Schedule**.
4. Select individual posting times for **LinkedIn**, **Instagram**, **MS Teams**, or **WhatsApp**.
5. Tap **Save Schedule** (invalid past dates are blocked automatically).

### 4. Uploading or Replacing Poster Media
1. In the campaign detail view, locate the **Campaign Poster** card.
2. Tap **Upload Poster** (or **Replace Poster**).
3. Select an image file (`image/*`) from the device gallery.
4. The media asset will upload and update the preview instantly.

---

## Repository Directory Structure

```
app/src/main/java/com/speehive/speehiveaihub/
├── data/               # Auth & Session Managers (Tokens, Roles, Timestamps)
├── models/             # Data models (Campaign, Event, User, AuditLog, PlatformPosting, BulkImportModels, etc.)
├── navigation/         # NavGraph, Screen routes, and RoleGuard authorization
├── network/            # Retrofit client, API service, DTOs, mappers, hybrid DNS
├── notification/       # WorkManager workers, schedulers, notification channels
├── repository/         # Repository contracts & API implementations
├── ui/                 # Jetpack Compose screens
│   ├── components/     # Reusable UI dialogs (ConfirmationDialog, ZoomableImageDialog), AdminSettingsScreen, SlidingStatusFilter, ViewModeSwitcher, BottomNavBar
│   └── theme/          # Typography, Color palettes, Material 3 Theme, ThreeDEffects
├── utils/              # Timezone (IST), Date formatting, and Uri/File helpers
└── viewmodel/          # ViewModels managing StateFlow UI state (DashboardViewModel, AdminViewModel, CampaignDetailViewModel, EventViewModel)
```
