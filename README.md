# 🐝 Speehive AI Hub - v2 Redesign Branch

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-blue?style=flat&logo=kotlin)
![Jetpack%20Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3%20%2B%203D%20Glassmorphism-4285F4?style=flat&logo=android)
![Min%20SDK](https://img.shields.io/badge/Min%20SDK-26%20(Android%208.0)-brightgreen)
![Target%20SDK](https://img.shields.io/badge/Target%20SDK-37-green)
![License](https://img.shields.io/badge/License-Proprietary-blue)

The **`feature/v2-redesign`** branch of **Speehive AI Hub** introduces a streamlined architecture that consolidates design and reviewer workflows into a unified **Reviewer** experience, alongside multi-platform social media scheduling, 3D glassmorphism aesthetics, AI prompt copying, and hybrid DNS resilience.

---

## 🚀 Key Features & Role Consolidation

### 👑 Admin
- **User Administration**: Create new user accounts, assign roles (`Admin`, `Reviewer`), and toggle user activation status (`Active`/`Inactive`).
- **System Audit Logs**: Monitor system-wide compliance logs with search and date filters.
- **Social Media Credentials**: Add and update OAuth credentials for connected platforms (LinkedIn, Instagram, MS Teams, WhatsApp).
- **View Mode Switcher**: Seamlessly switch dashboard view modes using the 3D `ViewModeSwitcher`.

### 👁️ Reviewer (Consolidated Designer Capabilities)
- **Unified Review & Asset Workflow**: View active, generated, approved, rejected, and published marketing campaigns and events.
- **Poster Upload & Replacement**: Upload or replace event/campaign poster images directly in-line with centered layout positioning.
- **Per-Platform Social Scheduling**: Customize posting dates and times individually per platform (`LinkedIn`, `Instagram`, `MS Teams`, `WhatsApp`) with past-date validation.
- **Copy AI Image Prompt**: One-tap action to copy AI generation image prompts directly to the clipboard from zoomable dialogs.
- **Top App Bar Cleanups**: Top app bar is hidden on Reviewer dashboards to maximize screen real estate.

### 🎨 3D Glassmorphism & Visual Aesthetics
- **3D Depth Styling (`ThreeDEffects`)**: Custom 3D cards, press effects, depth shadows, light highlights, and glassmorphic containers.
- **Interactive Image Viewing (`ZoomableImageDialog`)**: Full-screen zoomable media preview supporting double-tap, pinch-to-zoom, and prompt clipboard copying.

### 🔔 Background Notifications & Workers
- **WorkManager Engine**: `NotificationWorker` polls campaign status and social media credential expiry in the background.
- **Deep Linking**: Direct navigation from local push notifications into the Notification Center.

---

## 🏗️ Architecture & Tech Stack

The application follows the **MVVM (Model-View-ViewModel)** architectural pattern:

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
│ (Retrofit 2 + Gson + Hybrid DNS)    │           │ (SharedPreferences)  │
└─────────────────────────────────────┘           └──────────────────────┘
```

* **UI Layer**: Jetpack Compose with Material 3, custom 3D glassmorphism (`ThreeDEffects.kt`), and `ViewModeSwitcher`.
* **Networking & Hybrid DNS**: Retrofit 2 + Gson + OkHttp with custom `AuthInterceptor` (JWT injection), `TokenValidationInterceptor` (auto-logout on 401), `HttpLoggingInterceptor`, and **Hybrid DNS** (System DNS with Cloudflare DNS-over-HTTPS `1.1.1.1` fallback).
* **Media Handling**: Coil Compose with dynamic server base URL image URL rewriters and fallback placeholders.
* **Timezone Standard**: Java 8 `java.time.OffsetDateTime` formatted to IST (Asia/Kolkata) timezone (`DateUtils`).

---

## 📂 Repository Structure

```
app/src/main/java/com/speehive/speehiveaihub/
├── data/               # Auth & Session Managers (Tokens, Roles, Timestamps)
├── models/             # Data models (Campaign, Event, User, AuditLog, PlatformPosting, etc.)
├── navigation/         # NavGraph, Screen routes, and RoleGuard authorization
├── network/            # Retrofit client, API service, DTOs, mappers, hybrid DNS
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
* **JDK**: Java 17 (JBR recommended).
* **Android SDK**: API 37 (Build-Tools 35.0.0+).
* **Minimum Device**: Android 8.0 (API Level 26).

### 1. Clone the Repository & Checkout Feature Branch
```bash
git clone https://github.com/Intern-AM/Frontend.git
cd Frontend
git checkout feature/v2-redesign
```

### 2. Configure Backend Server Base URL
The backend API endpoint is configured in `RetrofitClient.kt`:
```kotlin
private const val BASE_URL = "https://debian.tailbd6bc8.ts.net/"
```

### 3. Build & Run via Command Line

* **Build Debug APK**:
  ```powershell
  cmd /c "set ""JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"" && gradlew assembleDebug"
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
