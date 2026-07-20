# 🐝 Hive AI - Android Mobile Application

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-blue?style=flat&logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4?style=flat&logo=android)
![Min SDK](https://img.shields.io/badge/Min%20SDK-26%20(Android%208.0)-brightgreen)
![Target SDK](https://img.shields.io/badge/Target%20SDK-37-green)
![License](https://img.shields.io/badge/License-MIT-blue)

**Hive AI** is a modern Android application built with Jetpack Compose, Material 3, and Kotlin Coroutines. It serves as a central hub for managing marketing campaigns, event schedules, creative media assets, audit logs, and social media credentials across multi-tiered user roles.

---

## 🚀 Features & Role-Based Access Control (RBAC)

Hive AI supports 3 distinct user roles with strict UI and navigation authorization:

### 👑 Admin
- **User Management**: Create new platform accounts, assign roles (`Admin`, `Designer`, `Reviewer`), and view account details.
- **Audit Logs**: Monitor real-time system audit logs for administrative compliance.
- **Social Media Credentials**: Add and update OAuth credentials for connected platforms.
- **Role Switcher**: Access both Designer and Reviewer dashboards directly.

### 🎨 Designer
- **Asset Management**: View campaigns requiring creative designs and collateral.
- **Media Upload**: Upload image assets directly to campaigns via multi-part HTTP requests.
- **Design Review Status**: Track approval statuses of submitted designs.

### 👁️ Reviewer / Standard User
- **Campaign Dashboard**: Overview of active, scheduled, and completed marketing campaigns.
- **Interactive Schedule Editor**: Update campaign schedules with IST timezone formatting.
- **Event Management**: Track upcoming events linked to social campaigns.
- **Notification Center**: View background alerts with seen/unseen tracking badges.

### 🔔 Background Notifications & Workers
- **WorkManager Integration**: Schedules background workers (`NotificationWorker`) to check campaign schedules and trigger local push notifications.
- **Deep Linking**: Tapping a notification opens the application directly to the Notification Center.

---

## 🏗️ Architecture & Tech Stack

The application follows the **MVVM (Model-View-ViewModel)** architectural pattern with clean repository abstractions:

```
                  ┌──────────────────────┐
                  │   Jetpack Compose    │
                  │   UI (Screens/Cards) │
                  └──────────┬───────────┘
                             │
                             ▼
                  ┌──────────────────────┐
                  │      ViewModel       │
                  │  (StateFlow / UI)    │
                  └──────────┬───────────┘
                             │
                             ▼
                  ┌──────────────────────┐
                  │  Repository Layer    │
                  │ (ApiRepositories)    │
                  └──────────┬───────────┘
                             │
            ┌────────────────┴────────────────┐
            ▼                                 ▼
┌──────────────────────┐          ┌──────────────────────┐
│  SpeehiveApiService  │          │    SessionManager    │
│  (Retrofit 2 + Gson) │          │  (SharedPreferences) │
└──────────────────────┘          └──────────────────────┘
```

* **UI Layer**: Jetpack Compose with Material 3, `rememberNavController`, and responsive layouts.
* **Networking**: Retrofit 2 + OkHttp with custom `AuthInterceptor` (JWT header injection) and `TokenValidationInterceptor` (auto-logout on 401 unauthenticated).
* **Image Rendering**: Coil Compose with fallback placeholders and dynamic server base URL image formatting.
* **Date & Time Handling**: Java 8 `java.time.OffsetDateTime` converted to IST (Asia/Kolkata) timezone.

---

## 📂 Repository Structure

```
app/src/main/java/com/speehive/speehiveaihub/
├── data/               # Auth & Session Managers (Tokens, Roles, Timestamps)
├── models/             # Data classes (Campaign, Event, User, AuditLog, etc.)
├── navigation/         # NavGraph, Screen routes, and RoleGuard authorization
├── network/            # Retrofit client, API interface, DTOs, mappers, interceptors
├── notification/       # WorkManager workers, schedulers, notification channels
├── repository/         # Repository contracts & API implementations
├── ui/                 # Jetpack Compose screens
│   ├── components/     # Reusable UI dialogs, navigation bar, status badges
│   └── theme/          # Typography, Color palettes, Material 3 Theme
├── utils/              # Timezone (IST) & URI conversion helpers
└── viewmodel/          # Jetpack ViewModels managing StateFlow UI state
```

---

## 🛠️ Prerequisites & Getting Started

### Prerequisites
* **Android Studio**: Ladybug (2024.2.1) or newer recommended.
* **JDK**: Java 17.
* **Android SDK**: API 37 (Build-Tools 35.0.0+).
* **Minimum Device**: Android 8.0 (API Level 26).

### 1. Clone the Repository
```bash
git clone https://github.com/Intern-AM/Frontend.git
cd Frontend
```

### 2. Configure Backend Server Base URL
By default, the backend API endpoint is defined in `RetrofitClient.kt`:
```kotlin
private const val BASE_URL = "http://172.16.70.37:5019/"
```
Ensure your mobile device or emulator can reach this backend network IP address.

### 3. Build & Run via Command Line

* **Build Debug APK**:
  ```powershell
  ./gradlew assembleDebug
  ```

* **Run Unit Tests**:
  ```powershell
  ./gradlew test
  ```

* **Install to connected device/emulator**:
  ```powershell
  ./gradlew installDebug
  ```

---

## 📜 AI Engineering Guidelines

This repository includes an **AI Engineering Workspace** under `.ai/` containing development standards and agent playbooks:
- `.ai/AGENTS.md` - Agent instructions and task workflows.
- `.ai/architecture.md` - Technical architecture guidelines.
- `.ai/coding-standards.md` - Kotlin & Compose coding standards.
- `.ai/api-contract.md` - Backend API response contracts.

---

## 📄 License
This project is proprietary software belonging to **Hive AI**. All rights reserved.
