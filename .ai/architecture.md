# Architecture

This document describes the system architecture of Hive AI.
It is the single source of truth for architectural patterns and data flow.

## Data Flow

```
Screen → ViewModel → Repository → RetrofitClient → API
```

One direction only. Never reverse. Screens observe state. ViewModels manage state. Repositories access data. The network layer is isolated behind repositories.

## MVVM Pattern

- Screens are pure composables. They observe ViewModel state and emit events. They contain zero business logic.
- ViewModels own all business state via `mutableStateOf`. They call repositories via suspend functions. They never access Retrofit, OkHttp, or the network layer directly.
- Repositories are the only data access layer. All methods return `Result<T>`. They never throw exceptions to the UI.

## Repository Pattern

- 5 interface + implementation pairs in `repository/`
- Interfaces define contracts. Implementations are prefixed with `Api` (e.g., `ApiCampaignRepository`)
- Each implementation creates its own Retrofit service via `RetrofitClient.create()`
- DTOs live in `network/`. Domain models live in `models/`. Mappers live in `*Mapper.kt` files.
- Error handling: two-tier catch — `HttpException` → `AuthError.fromCode()`, `Exception` → raw

Full error handling patterns → `diagnostics.md`

## Dependency Wiring

- All dependencies are wired in `NavGraph.kt` via `remember {}` blocks
- No Hilt, no Dagger, no Koin, no service locator
- Repositories receive `SessionManager` + `AuthManager` (+ `Context` for file-upload repos)
- ViewModels receive repository interfaces via constructor parameters
- ViewModels are created with `remember {}` inside composable routes

## ViewModel Lifecycle

- Created with `remember {}` inside composable scopes
- Preserved during recomposition within the same route
- NOT retained across navigation (new instance on route re-entry)
- This is a deliberate tradeoff for simplicity over `ViewModelProvider`

## State Management

- `mutableStateOf` with `private set` for all internal state
- Exception: two-way binding fields in LoginViewModel (`email`, `password`)
- `isLoading` boolean for initial data loading (read operations)
- `isProcessing` boolean for mutation operations (write/action operations)
- `errorMessage: String?` for error state, cleared at start of every operation
- `successMessage: String?` for success feedback (AdminViewModel, DesignerViewModel only)
- `uploadingId: String?` for per-item processing indicator (DesignerViewModel)

Full naming conventions → `coding-standards.md`

## Error Handling

- Repositories return `Result<T>` — never throw exceptions to the UI
- ViewModels use `Result.fold(onSuccess, onFailure)` for all repository calls
- First-error-wins pattern for multi-source loads (first error is preserved, subsequent errors silently dropped)
- Typed error handling for auth-sensitive operations (LoginViewModel, CampaignDetailViewModel)
- `errorMessage` is cleared at the start of every operation to prevent stale errors

## Authentication

- JWT stored in SharedPreferences via `SessionManager`
- `AuthInterceptor` attaches Bearer token to all requests (skips login endpoint)
- `TokenValidationInterceptor` triggers logout on 401 responses (skips login endpoint)
- `AuthManager` holds `MutableStateFlow<AuthState>` — the ONLY StateFlow in the project
- `LaunchedEffect` in NavGraph reacts to auth state changes and redirects to login

## Navigation

- 9 routes defined in `Screen.kt` sealed class
- Role-based start destination: Admin → AdminDashboard, Designer → DesignerDashboard, else → Dashboard
- `findStartDestination().id` used for proper back stack management
- `popUpTo` with `inclusive = true` to clear back stack on logout and role-based navigation

## Roles

- Backend "Reviewer" = frontend uses "Reviewer" (NOT "User")
- CreateUserDialog offers: Reviewer, Designer, Admin
- Route-level role guards: Admin routes require Admin role, Designer routes require Designer role
- Any user (including Reviewer) can approve/reject campaigns and reject events

## Folder Structure

```
com.speehive.speehiveaihub/
├── MainActivity.kt              # Entry point
├── data/                        # SessionManager, AuthManager
├── models/                      # Domain data classes
├── navigation/                  # Screen routes, NavGraph
├── network/                     # Retrofit, DTOs, interceptors, mappers
├── repository/                  # Interface + Api* implementations
├── ui/
│   ├── theme/                   # Color, Type, Theme
│   ├── components/              # Reusable composables
│   └── *.kt                     # Screen composables
├── utils/                       # DateUtils, StatusUtils
└── viewmodel/                   # ViewModels
```
