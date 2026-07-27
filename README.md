# 🐝 Speehive AI Hub — Main Branch

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-blue?style=flat&logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3%20%2B%203D%20Glassmorphism-4285F4?style=flat&logo=android)
![Min SDK](https://img.shields.io/badge/Min%20SDK-26%20(Android%208.0)-brightgreen)
![Target SDK](https://img.shields.io/badge/Target%20SDK-37-green)
![License](https://img.shields.io/badge/License-Proprietary-blue)

**Speehive AI Hub (`main`)** is an enterprise-grade Android application built with Jetpack Compose, Material 3, and Kotlin Coroutines. It serves as the central administrative and management hub for the Speehive AI automation platform—delivering campaign lifecycle management, multi-platform social scheduling (**LinkedIn**, **Instagram**, **MS Teams**, **WhatsApp**), media asset handling, real-time audit logging, and strict 3-role authorization.

---

## 🎯 What This Project Does

Speehive AI Hub manages corporate social media marketing with strict role boundary enforcement:
1. **AI Campaign Generation & Management**: Ingests corporate events, generates marketing post text, suggests hashtags, and manages campaign approval states (`Generated`, `Approved`, `Rejected`, `Posted`).
2. **Strict 3-Role Access Control**: Enforces clear capability boundaries between **Admins**, **Designers**, and **Reviewers** using `RoleGuard` authorization.
3. **Exclusive Poster Management**: Restricts image uploading and replacing strictly to **Designers**, keeping Reviewer workflows focused on content review and scheduling.
4. **Granular Multi-Platform Scheduling**: Tracks and schedules posts independently per platform with past-date prevention and individual channel status tracking (`Pending`, `Posted`, `Failed`).
5. **Resilient Network Layer**: Combines OkHttp interceptors with **Hybrid DNS over HTTPS** (Cloudflare `1.1.1.1` fallback) for domain resolution resilience across local and cellular networks.
6. **Background Scheduling Alerts**: Android WorkManager worker (`NotificationWorker`) periodically checks schedules and delivers deep-linked notifications.

---

## 🛠️ Core Tech Stack

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

## 🏛️ Architecture & System Design

The application follows the **Model-View-ViewModel (MVVM)** architectural pattern:

```
┌────────────────────────────────────────────────────────────────────────┐
│                          Jetpack Compose UI                            │
│  (AdminDashboardScreen, DashboardScreen, DesignerDashboardScreen, etc.)│
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ StateFlow / UI State
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                               ViewModel                                │
│ (DashboardViewModel, CampaignDetailViewModel, DesignerViewModel, etc.) │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Repository Contracts
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                            Repository Layer                            │
│  (ApiCampaignRepository, ApiEventRepository, ApiAdminRepository, etc.) │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
               ┌────────────────────┴────────────────────┐
               ▼                                         ▼
┌──────────────────────────────┐          ┌──────────────────────────────┐
│     SpeehiveApiService       │          │        SessionManager        │
│  (Retrofit + Hybrid DNS)     │          │    (Encrypted Session)       │
└──────────────────────────────┘          └──────────────────────────────┘
```

### 🔐 Strict 3-Role Isolation Model (`main`)

The `main` branch maintains strict boundaries between three distinct user roles:

```
                  ┌─────────────────────────────────────┐
                  │              RoleGuard              │
                  └──────────────────┬──────────────────┘
            ┌────────────────────────┼────────────────────────┐
            ▼                        ▼                        ▼
┌──────────────────────┐  ┌──────────────────────┐  ┌──────────────────────┐
│        Admin         │  │       Designer       │  │       Reviewer       │
│  - User Management   │  │  - Upload Posters    │  │  - Edit Copy         │
│  - Audit Logs        │  │  - Replace Posters   │  │  - Approve / Reject  │
│  - Platform Creds    │  │  - Edit Alignments   │  │  - Multi-Platform    │
│  - System View       │  │  - Copy Prompts      │  │    Scheduling        │
└──────────────────────┘  └──────────────────────┘  └──────────────────────┘
```

#### 👑 Admin
* **User Account Administration**: Create new users with `Admin`, `Designer`, or `Reviewer` roles; activate or deactivate user accounts.
* **Audit Logging**: Monitor real-time system audit logs with date filtering and search capabilities.
* **Social Media Credentials**: Add and update OAuth API credentials for connected platforms.
* **Locked Flow**: Dedicated administrative interface without mode-switching sliders.

#### 🎨 Designer
* **Exclusive Poster Authority**: Designers hold exclusive authority to upload and replace event and campaign posters.
* **Dedicated Dashboard**: Access assigned campaigns and events via `DesignerDashboardScreen`.
* **Design Alignment & Prompt Copy**: Edit post copy for visual alignment and copy AI image prompts with one tap.

#### 👁️ Reviewer
* **Campaign & Event Review**: Overview of active, scheduled, and generated marketing campaigns and upcoming events.
* **Per-Platform Social Scheduling**: Manage posting schedules independently per social channel (**LinkedIn**, **Instagram**, **MS Teams**, **WhatsApp**) with past-date validation.
* **Approval Decisioning**: Approve or reject campaign copy with status locking.
* **Protected UI**: Poster upload and replace controls are completely hidden from Reviewers.

---

## ⚙️ Configuration & Environment Settings

### Codebase Configuration (`RetrofitClient.kt`)

| Variable / Constant | Default Value | Description |
| :--- | :--- | :--- |
| `BASE_URL` | `https://debian.tailbd6bc8.ts.net/` | Root HTTP API endpoint for backend services |
| `CONNECT_TIMEOUT` | `30 Seconds` | OkHttp connection timeout limit |
| `READ_TIMEOUT` | `30 Seconds` | OkHttp socket read timeout limit |
| `WRITE_TIMEOUT` | `30 Seconds` | OkHttp socket write timeout limit |
| `DNS_OVER_HTTPS_URL` | `https://1.1.1.1/dns-query` | Cloudflare DoH endpoint for DNS bypass fallback |

### Local Session & Preferences (`SessionManager.kt`)

| Key | Storage | Description |
| :--- | :--- | :--- |
| `KEY_TOKEN` | SharedPreferences | Bearer JWT authentication token injected into API headers |
| `KEY_ROLE` | SharedPreferences | Active user role (`Admin`, `Designer`, or `Reviewer`) used by `RoleGuard` |
| `KEY_USER_NAME` | SharedPreferences | Display name of the currently authenticated user |
| `KEY_USER_ID` | SharedPreferences | Unique identifier of the logged-in user |
| `KEY_IS_LOGGED_IN` | SharedPreferences | Boolean session state check |

---

## 🚀 Step-by-Step Local Setup & Installation Guide

### Prerequisites
* **Android Studio**: Ladybug (2024.2.1) or newer.
* **JDK**: Java 17 (bundled Android Studio JBR recommended).
* **Android SDK**: API 37 (Build-Tools 35.0.0+).
* **Minimum Target Device**: Android 8.0 (API Level 26 / Oreo).

### 1. Clone & Checkout Main Branch
```bash
git clone https://github.com/Intern-AM/Frontend.git
cd Frontend
git checkout main
```

### 2. Configure Backend Endpoint URL
If connecting to a custom backend or local server IP, configure `BASE_URL` in `app/src/main/java/com/speehive/speehiveaihub/network/RetrofitClient.kt`:
```kotlin
object RetrofitClient {
    private const val BASE_URL = "https://debian.tailbd6bc8.ts.net/"
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

## 💡 Usage Examples & Workflows

### 1. Designer Poster Upload Workflow
1. Log in as a **Designer**.
2. Redirection lands on `DesignerDashboardScreen`.
3. Expand **View Campaigns** or **View Events**.
4. Tap **Upload Poster** (or **Replace Poster**) to select an image from the device gallery.

### 2. Reviewer Multi-Platform Scheduling
1. Log in as a **Reviewer**.
2. Navigate to a campaign detail view (`CampaignDetailScreen`).
3. Tap **Edit Schedule**.
4. Select posting times for **LinkedIn**, **Instagram**, **MS Teams**, or **WhatsApp**.
5. Tap **Save Schedule** (past dates are automatically validated and rejected).

### 3. Admin Account Creation
1. Log in as an **Admin**.
2. Redirection lands on `AdminDashboardScreen`.
3. Tap the **+** (Floating Action Button).
4. Select role (`Reviewer`, `Designer`, `Admin`), fill in name, email, and password, and tap **Create**.

---

## 📂 Repository Directory Structure

```
app/src/main/java/com/speehive/speehiveaihub/
├── data/               # Auth & Session Managers (Tokens, Roles, Timestamps)
├── models/             # Data models (Campaign, Event, User, AuditLog, PlatformPosting, etc.)
├── navigation/         # NavGraph, Screen routes, and RoleGuard authorization
├── network/            # Retrofit client, API service, DTOs, mappers, hybrid DNS
├── notification/       # WorkManager workers, schedulers, notification channels
├── repository/         # Repository contracts & API implementations
├── ui/                 # Jetpack Compose screens
│   ├── components/     # Reusable UI dialogs, navigation bar, status badges
│   └── theme/          # Typography, Color palettes, Material 3 Theme, ThreeDEffects
├── utils/              # Timezone (IST) & URI conversion helpers
└── viewmodel/          # ViewModels managing StateFlow UI state
```

---

## 📄 License
This project is proprietary software belonging to **Speehive Technologies**. All rights reserved.
