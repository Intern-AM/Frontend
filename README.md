# 🐝 Speehive AI Hub - Android Mobile Application

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-blue?style=flat&logo=kotlin)
![Jetpack%20Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4?style=flat&logo=android)
![Min%20SDK](https://img.shields.io/badge/Min%20SDK-26%20(Android%208.0)-brightgreen)
![Target%20SDK](https://img.shields.io/badge/Target%20SDK-37-green)
![License](https://img.shields.io/badge/License-Proprietary-blue)

**Speehive AI Hub** is a modern Android application built with Jetpack Compose, Material 3, and Kotlin Coroutines. It serves as a central hub for managing AI-generated marketing campaigns, event schedules, creative media assets, multi-platform social media scheduling, audit logs, and social credentials under strict role-based access control.

---

## 🚀 Features & Role-Based Access Control (RBAC)

Speehive AI Hub enforces strict separation of capabilities across 3 distinct user roles via `RoleGuard` authorization:

### 👑 Admin
- **User Account Management**: Create new platform users with explicit roles (`Admin`, `Designer`, `Reviewer`), activate, or deactivate accounts.
- **System Audit Logs**: Monitor system-wide compliance logs with search and date filtering.
- **Social Media Credentials**: Manage OAuth credentials for connected platforms (LinkedIn, Instagram, MS Teams, WhatsApp).
- **Navigation Lockdown**: Dedicated administrative control center without cross-role UI mixing.

### 🎨 Designer
- **Exclusive Poster Upload & Replacement**: Only Designers have authority to upload or replace poster assets for campaigns and events.
- **Design Alignment**: Edit campaign posts and hashtags directly alongside image previews for optimal visual alignment.
- **AI Prompt Clipboard**: Copy AI generation image prompts with one tap directly from the image zoom dialog.

### 👁️ Reviewer
- **Campaign & Event Review**: Overview of active, scheduled, and generated marketing campaigns and upcoming events.
- **Per-Platform Social Scheduling**: Manage granular posting schedules per platform (LinkedIn, Instagram, MS Teams, WhatsApp) with past-date validation.
- **Approval Workflow**: Approve or reject generated campaigns with status locking.
- **Protected UI**: Poster upload controls are completely hidden from Reviewers.

### 🔔 Background Notifications & Workers
- **WorkManager Integration**: Periodically checks campaign schedules (`NotificationWorker`) to trigger local push notifications.
- **Deep Linking**: Notification alerts navigate directly to the Notification Center.

---

## 🏗️ Architecture & Tech Stack

The application follows the **MVVM (Model-View-ViewModel)** pattern with clean repository layer abstractions:

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

* **UI Layer**: Jetpack Compose with Material 3, custom 3D glassmorphism (`ThreeDEffects.kt`), and smooth pinch-to-zoom image dialogs.
* **Networking & DNS Failover**: Retrofit 2 + OkHttp with `AuthInterceptor` (JWT header injection), `TokenValidationInterceptor` (auto-logout on 401), `HttpLoggingInterceptor`, and **Hybrid DNS** (System DNS with Cloudflare DNS-over-HTTPS `1.1.1.1` fallback).
* **Media Handling**: Coil Compose with dynamic server base URL image rewriters and fallback placeholders.
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
│   ├── components/     # Reusable UI dialogs, navigation bar, status badges
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

### 1. Clone the Repository
```bash
git clone https://github.com/Intern-AM/Frontend.git
cd Frontend
```

### 2. Configure Backend Server Base URL
Configure the backend server URL in `RetrofitClient.kt`:
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
