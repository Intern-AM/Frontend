# Diagnostics

This document provides systematic troubleshooting guidance.
It is the single source of truth for diagnosing problems in the codebase.

## Build Failures

**Symptom:** `./gradlew assembleDebug` fails

**Check:**
- Unused imports — remove any imports not referenced in the file
- Missing `private set` on ViewModel state properties
- Missing `key` parameters on LazyColumn `items()` calls
- Deprecated API usage — check for deprecated function calls
- Wrong package declaration — verify file is in correct package
- Missing required parameters in composable function calls

**Checklist:** `review-checklist.md`

## Navigation Issues

**Symptom:** Screen not reachable, wrong screen on launch, back button misbehaves

**Check:**
- Route defined in `Screen.kt` sealed class
- Route wired in `NavGraph.kt` composable
- Role guard matches intended access (Admin, Designer, Reviewer)
- `popUpTo` uses `findStartDestination().id` — never `popUpTo(0)`
- Parameterized routes have correct `navArgument` type
- `createRoute()` companion function generates correct path

Architecture reference → `architecture.md`

## Authentication Issues

**Symptom:** Login fails, unexpected logout, token not attached

**Check:**
- `AuthInterceptor` skips login endpoint (`api/Auth/login`)
- `TokenValidationInterceptor` skips login endpoint 401s
- `SessionManager` stores token, name, and role correctly
- `AuthManager` state transitions: Authenticated → Unauthenticated
- `LaunchedEffect` in NavGraph reacts to `Unauthenticated` state
- SharedPreferences key names match (`jwt_token`, `user_name`, `role`)

## Retrofit Failures

**Symptom:** Network errors, unexpected responses, crashes on parsing

**Check:**
- Base URL is correct and accessible
- Request DTO field names match backend expectations
- Response DTO field names match backend response
- Content-Type headers are correct (multipart for file uploads)
- `@Transient` on fields that should be excluded from serialization
- Error body parsed correctly via `AuthError.fromCode()`

API reference → `api-contract.md`

## State Management Bugs

**Symptom:** Stale data, missing loading indicators, error messages persist

**Check:**
- `errorMessage` cleared at start of every operation
- `isLoading` toggled correctly (true before, false after)
- `isProcessing` toggled correctly for mutations
- `private set` on all state properties
- Null safety on nullable state access (`campaign?.let { ... }`)
- `Result.fold()` handles both success and failure paths

Architecture reference → `architecture.md`

## Compose Issues

**Symptom:** Recomposition bugs, missing items, performance issues

**Check:**
- `key` parameter on all LazyColumn `items()` calls
- Stable types for LazyColumn items (data classes, not lambdas)
- State hoisting (business state in ViewModel, UI state in composable)
- `LaunchedEffect` key dependencies are correct
- No unnecessary recomposition triggers

## Theme Inconsistencies

**Symptom:** Wrong colors, wrong fonts, wrong shapes

**Check:**
- Direct color constants used (not `MaterialTheme.colorScheme.*`)
- Shape values from standard set (20/16/12/6/100dp)
- Typography uses BitcountSingle for headings, Lilex for rest
- `statusColor()` used for status badges
- No `FontFamily.Default` usage

Theme reference → `theme.md`

## Performance Issues

**Symptom:** Slow loading, janky scrolling, excessive recomposition

**Check:**
- Sequential repository calls that could be parallelized
- Unnecessary recomposition (unstable parameters in composables)
- Large lists without `key` parameters
- Missing `distinctBy` on duplicate data
- Temp files not cleaned up (disk space)

## Network Failures

**Symptom:** Connection refused, timeout, no response

**Check:**
- Internet permission in AndroidManifest
- Cleartext traffic enabled (HTTP backend)
- Token attached to request (non-login endpoints)
- Error response body parsed correctly
- OkHttpClient timeout configuration (30s default)

API reference → `api-contract.md`
