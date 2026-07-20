# Playbook

This document explains how to extend the Hive AI codebase.
It is the single source of truth for adding screens, ViewModels, repositories, endpoints, navigation, and composables.

## Adding a Screen

1. Create a composable function in `ui/` named `*Screen.kt`
2. Add a route to `Screen.kt` sealed class
3. Wire the ViewModel and screen in `NavGraph.kt`
4. Follow the existing scaffold recipe: Scaffold → TopAppBar → content → optional BottomNavBar
5. Use theme constants from `ui/theme/` — never hardcoded colors

Architecture reference → `architecture.md`
Theme reference → `theme.md`

## Adding a ViewModel

1. Create in `viewmodel/` named `*ViewModel.kt`
2. Take repository interfaces via constructor parameters (not implementations)
3. Use `mutableStateOf` with `private set` for all internal state
4. Use `Result.fold(onSuccess, Failure)` for all repository calls
5. Clear `errorMessage` at start of every operation
6. Auto-load data in `init {}` for list screens
7. Use `isLoading` for reads, `isProcessing` for writes

Architecture reference → `architecture.md`
Coding standards → `coding-standards.md`

## Adding a Repository

1. Create interface in `repository/` defining the contract
2. Create `Api*` implementation in `repository/`
3. Take `SessionManager` + `AuthManager` via constructor (+ `Context` for file-upload repos)
4. Create Retrofit service via `RetrofitClient.create(sessionManager, authManager)`
5. Return `Result<T>` from all methods — never throw exceptions
6. Use two-tier catch: `HttpException` → `AuthError.fromCode()`, `Exception` → raw
7. Create DTOs in `network/` if needed
8. Create mapper in `*Mapper.kt` if needed

Architecture reference → `architecture.md`
API reference → `api-contract.md`

## Adding an API Endpoint

1. Add the endpoint to `SpeehiveApiService.kt`
2. Create request DTO in `network/` if needed
3. Create response DTO in `network/` if needed
4. Create mapper in `*Mapper.kt` if needed
5. Wire through repository interface and implementation
6. Consume in ViewModel via `Result.fold()`

API reference → `api-contract.md`

## Adding Navigation

1. Add route to `Screen.kt` sealed class
2. Wire composable in `NavGraph.kt`
3. If parameterized, add `navArgument` and `createRoute()` companion function
4. If role-restricted, add role guard in NavGraph
5. Use `findStartDestination().id` for `popUpTo` — never `popUpTo(0)`

Architecture reference → `architecture.md`

## Adding a Composable

1. Follow existing card/button/badge recipe from `components.md`
2. Use theme constants from `ui/theme/` — never hardcoded colors
3. Accept state via parameters
4. Emit events via lambda parameters
5. One composable per file (except small helpers)

Theme reference → `theme.md`
Component principles → `components.md`
