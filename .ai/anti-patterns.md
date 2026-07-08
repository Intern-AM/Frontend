# Anti-Patterns

This document lists practices that must never be introduced to this codebase.
It is the single source of truth for architectural guardrails.

## Architecture Violations

- Business logic inside Compose screens
- Calling Retrofit directly from UI code
- Bypassing the Repository Pattern
- Bypassing the ViewModel layer
- Introducing a second architecture pattern
- Creating parallel implementation patterns
- Using `hiltViewModel()` or any DI framework
- Using `MutableStateFlow` in ViewModels

## Code Quality Violations

- Duplicating business logic across files
- Public mutable state without `private set`
- Unrelated code refactoring during feature work
- Large monolithic changes
- Unnecessary abstractions or dependencies
- Adding comments unless explicitly asked
- Adding unused imports
- Using `runBlocking` in any context

## Theme Violations

- Hardcoding theme colors in screens (use constants from `ui/theme/`)
- Using `MaterialTheme.colorScheme.*` instead of direct color constants
- Using `FontFamily.Default` (always BitcountSingle or Lilex)
- Adding dark mode support
- Using `Color.White` or `Color.Black` directly
- Using `RoundedCornerShape` values other than 20/16/12/6/100dp

## State Violations

- Using `MutableStateFlow` in ViewModels
- Removing `private set` from state properties
- Using nuclear `.clear()` on SharedPreferences
- Not clearing `errorMessage` at start of operations
- Not toggling `isLoading`/`isProcessing` correctly

## Navigation Violations

- Modifying navigation structure without explicit instruction
- Removing role-based routing
- Using `popUpTo(0)` instead of `findStartDestination().id`
- Forgetting to add `key` parameters to LazyColumn items

## Network Violations

- Adding token to login requests
- Triggering logout on login endpoint 401s
- Not validating MIME types before file upload
- Not cleaning up temp files in `finally` blocks
- Not checking file size before upload

## Review Guidance

Before introducing any change, review this list.
If the change resembles any anti-pattern, stop and discuss the architectural impact.

Architecture reference → `architecture.md`
Constraints reference → `coding-standards.md`
