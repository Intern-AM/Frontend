# 🐝 Hive AI - Android Mobile Application

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-blue?style=flat&logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3%20%2B%203D%20Glassmorphism-4285F4?style=flat&logo=android)
![Min SDK](https://img.shields.io/badge/Min%20SDK-26%20(Android%208.0)-brightgreen)
![Target SDK](https://img.shields.io/badge/Target%20SDK-37-green)
![License](https://img.shields.io/badge/License-MIT-blue)

**Hive AI** is a state-of-the-art Android application built with Jetpack Compose, Material 3, and Kotlin Coroutines. It serves as the mobile administration and management hub for the Speehive AI automation platform—enabling end-to-end campaign lifecycle management, multi-platform scheduling (LinkedIn, Instagram), creative media editing, real-time audit logging, and role-based administration.

---

## 🚀 Key Features & Role-Based Access Control (RBAC)

Hive AI enforces strict role-based access control with intuitive, polished UI flows:

### 👑 Admin
- **User Administration**: Create new user accounts, assign roles (`Admin`, `Reviewer`), and toggle user activation status (Active/Deactive).
- **Audit Logs**: Monitor real-time system-wide audit logs with action filters for administrative compliance.
- **Social Media Credentials**: Manage OAuth credentials, view token expiry banners, and update tokens for connected platforms (LinkedIn, Instagram, Google Calendar).

### 👁️ Reviewer (Includes Consolidated Designer Capabilities)
- **Campaign Management & Review**: View active, generated, approved, rejected, and published marketing campaigns.
- **Per-Platform Multi-Platform Scheduling**: Customize posting dates and times individually per platform (e.g. LinkedIn, Instagram) and track per-platform status (`Posted`, `Failed`, `Pending`) along with failure diagnostics.
- **Creative Collateral & Poster Upload**: Upload campaign poster images and event images directly in-line, edit captions, hashtags, and CTAs before approval.
- **Event Synchronization**: Track upcoming events, view status details, cancel events, and trigger AI campaign generation workflows.
- **Interactive View Modes**: Toggle layout displays using the dynamic `ViewModeSwitcher`.

### 🎨 3D Glassmorphism & UI Aesthetics
- **Visual Elevation (`ThreeDEffects`)**: Custom 3D cards, depth shadows, light highlights, and glassmorphic container styling.
- **Media Experience (`ZoomableImageDialog`)**: Full-screen zoomable media preview supporting pinch-to-zoom and pan.

### 🔔 Background Notifications & WorkManager
- **WorkManager Engine**: `NotificationWorker` polls campaign status and social media credential expiry in the background every 15 minutes.
- **Dynamic Event Mapping**: Notifications display real Event Titles instead of raw IDs.
- **Platform Breakdown**: Notifications include platform posting summaries (e.g., *"Posted to 2 of 2 platform(s)"*).
- **Deep Linking**: Direct navigation from local push notifications into the Notification Center.

---

## 🏗️ Architecture & Tech Stack

The application follows the **MVVM (Model-View-ViewModel)** architectural pattern with clean repository abstractions:

```
                  ┌─────────────────────────────────────┐
                  │   Jetpack Compose + 3D Effects      │
                  │   UI (Screens / Cards / Dialogs)    │
                  └──────────────────┬──────────────────┘
                                     │
                                     ▼
                  ┌─────────────────────────────────────┐
                  │              ViewModel              │
                  │        (StateFlow / UI State)       │
                  └──────────────────┬──────────────────┘
                                     │
                                     ▼
                  ┌─────────────────────────────────────┐
                  │          Repository Layer           │
                  │         (ApiRepositories)           │
                  └──────────────────┬──────────────────┘
                                     │
            ┌────────────────────────┴────────────────────────┐
            ▼                                                 ▼
┌─────────────────────────────────────┐           ┌──────────────────────┐
│         SpeehiveApiService          │           │    SessionManager    │
│ (Retrofit 2 + Gson + OkHttp Logger) │           │ (SharedPreferences)  │
└─────────────────────────────────────┘           └──────────────────────┘
```

* **UI Layer**: Jetpack Compose with Material 3, custom 3D glassmorphism (`ThreeDEffects.kt`), and `ViewModeSwitcher`.
* **Networking**: Retrofit 2 + Gson + OkHttp `LoggingInterceptor` with custom `AuthInterceptor` (JWT injection) and `TokenValidationInterceptor` (auto-logout on 401 unauthenticated).
* **Media Handling**: Coil Compose with fallback placeholders and dynamic server base URL image URL rewriters.
* **Date & Time Handling**: Java 8 `java.time.OffsetDateTime` with IST (Asia/Kolkata) timezone utility formatting (`DateUtils`).

---

## 📂 Repository Structure

```
app/src/main/java/com/speehive/speehiveaihub/
├── data/               # Auth & Session Managers (Tokens, Roles, Timestamps)
├── models/             # Data classes (Campaign, Event, User, AuditLog, PlatformPosting, etc.)
├── navigation/         # NavGraph, Screen routes, and RoleGuard authorization
├── network/            # Retrofit client, API service, DTOs, mappers, interceptors
├── notification/       # WorkManager workers, schedulers, notification channels
├── repository/         # Repository contracts & API implementations
├── ui/                 # Jetpack Compose screens
│   ├── components/     # Reusable UI dialogs, navigation bar, status badges, ViewModeSwitcher
│   └── theme/          # Typography, Color palettes, Material 3 Theme, ThreeDEffects
├── utils/              # Timezone (IST) & URI conversion helpers
└── viewmodel/          # ViewModels managing StateFlow UI state
```

---

## 🛠️ Prerequisites & Getting Started

### Prerequisites
* **Android Studio**: Ladybug (2024.2.1) or newer.
* **JDK**: Java 17.
* **Android SDK**: API 37 (Build-Tools 35.0.0+).
* **Minimum Device**: Android 8.0 (API Level 26).

### 1. Clone the Repository
```bash
git clone https://github.com/Intern-AM/Frontend.git
cd Frontend
```

### 2. Configure Backend Server Base URL
The backend API endpoint is configured in `RetrofitClient.kt`:
```kotlin
private const val BASE_URL = "http://172.16.70.37:5019/"
```
Ensure your physical Android device or emulator is connected to the backend network.

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

This repository includes an **AI Engineering Workspace** under `.ai/` containing development standards and playbooks:
- `.ai/AGENTS.md` - Agent instructions and task workflows.
- `.ai/architecture.md` - Technical architecture guidelines.
- `.ai/coding-standards.md` - Kotlin & Compose coding standards.
- `.ai/api-contract.md` - Backend API response contracts.

---

## 📄 License
This project is proprietary software belonging to **Speehive Technologies**. All rights reserved.
