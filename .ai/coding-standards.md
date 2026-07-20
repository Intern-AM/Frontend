# Coding Standards

This document defines hard constraints, naming conventions, and coding rules.
It is the single source of truth for how code should be written in this project.

## Hard Constraints

These rules are non-negotiable. Violating them is a bug.

- MUST preserve MVVM architecture
- MUST preserve Repository Pattern (interface + `Api*` implementation)
- MUST minimize modified files per change
- MUST inspect existing implementations before introducing new patterns
- MUST preserve API contracts unless explicitly instructed
- MUST maintain backwards compatibility whenever practical
- MUST consider regression risks before implementation
- NEVER rewrite working modules without explicit instruction
- NEVER refactor unrelated code
- NEVER introduce unnecessary abstractions or dependencies
- NEVER duplicate business logic
- NEVER bypass ViewModels or Repositories
- NEVER use `MutableStateFlow` in ViewModels
- NEVER use `hiltViewModel()` or any DI framework
- NEVER remove `private set` from ViewModel state properties
- NEVER use `FontFamily.Default` — always BitcountSingle or Lilex
- NEVER hardcode colors in screens — use theme constants from `ui/theme/`
- NEVER add dark mode

Philosophy behind these rules → `manifesto.md`

## Naming Conventions

### Files
- PascalCase, descriptive: `CampaignDetailScreen.kt`, `ApiCampaignRepository.kt`
- Screen composables: `*Screen` suffix (`DashboardScreen`, `LoginScreen`)
- Repository implementations: `Api` prefix (`ApiCampaignRepository`)
- ViewModels: `*ViewModel` suffix (`DashboardViewModel`)
- DTOs: descriptive names (`LoginRequest`, `CampaignResponse`)
- Mappers: `*Mapper.kt` suffix (`EventMapper.kt`)

### State Properties
- camelCase, descriptive: `isLoading`, `errorMessage`, `isProcessing`
- Booleans: `is` or `has` prefix
- Nullable error states: `errorMessage: String?`

### Routes
- snake_case strings: `"admin_dashboard"`, `"campaign_detail"`

### Packages
- `data/` — SessionManager, AuthManager
- `models/` — Domain data classes
- `navigation/` — Screen routes, NavGraph
- `network/` — Retrofit, DTOs, interceptors, mappers
- `repository/` — Interface + Api* implementations
- `ui/` — Screens, components, theme
- `utils/` — Utility functions
- `viewmodel/` — ViewModels

## File Organization

- One primary composable per file
- Small helper composables can share a file with their parent
- ViewModels in `viewmodel/`
- Models in `models/`
- DTOs in `network/`
- Mappers in `*Mapper.kt` files

## Import Style

- Existing files use wildcard imports — preserve this style when editing
- New files should prefer explicit imports
- Never add unused imports

## Coroutines

- Always `viewModelScope.launch` for ViewModel coroutines
- Never global scope
- Never `runBlocking`

## State Property Rules

- `private set` on all internal state properties
- Exception: two-way binding fields in LoginViewModel (`email`, `password`)
- `isLoading` for reads, `isProcessing` for writes
- Clear `errorMessage` at start of every operation

## Error Messages

- User-friendly, action-oriented
- Typed for auth errors: `AuthError.TokenExpired`, `AuthError.Unauthorized`
- Default fallback message in `onFailure` blocks

## Compose Conventions

- State via parameters, events via lambda parameters
- Components are stateless when possible (receive data, emit events)
- Local UI state (expanded/collapsed, password visibility) lives in the composable
- Business state lives in the ViewModel

Full anti-patterns to avoid → `anti-patterns.md`
