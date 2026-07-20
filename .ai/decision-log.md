# Decision Log

This document records major architectural decisions.
It is the single source of truth for why the codebase is designed the way it is.

## Template

```markdown
### [Decision Title]
- **Date:** [Date]
- **Context:** [Why this decision was needed]
- **Alternatives:** [What was considered]
- **Trade-offs:** [Pros and cons]
- **Decision:** [What was chosen]
- **Reason:** [Why this approach was selected]
- **Impact:** [What files/patterns are affected]
```

## Existing Decisions

### Manual DI Over Framework
- **Date:** Project inception
- **Context:** Dependency injection needed for repository/ViewModel wiring
- **Alternatives:** Hilt, Dagger, Koin, Manual constructor injection
- **Trade-offs:** Frameworks add complexity and dependencies; manual is simpler but less scalable
- **Decision:** Manual constructor injection via `remember {}` in NavGraph
- **Reason:** Simplicity, no additional dependencies, full control over lifecycle
- **Impact:** All repositories and ViewModels wired in NavGraph.kt

### mutableStateOf Over MutableStateFlow
- **Date:** Project inception
- **Context:** State management needed for Compose UI
- **Alternatives:** MutableStateFlow, mutableStateOf, sealed class UiState
- **Trade-offs:** StateFlow requires collection; mutableStateOf integrates natively with Compose
- **Decision:** `mutableStateOf` with `private set` for all ViewModel state
- **Reason:** Native Compose integration, no collection boilerplate, direct recomposition
- **Impact:** All ViewModels use mutableStateOf; AuthManager uses StateFlow (only exception)

### Result<T> Over Exceptions
- **Date:** Project inception
- **Context:** Error handling needed across repository layer
- **Alternatives:** Exceptions, Result<T>, sealed class Either
- **Trade-offs:** Exceptions propagate unpredictably; Result is explicit and composable
- **Decision:** `Result<T>` return type for all repository methods
- **Reason:** Explicit error handling, no exception escaping to UI, composable with fold()
- **Impact:** All repository methods return Result; ViewModels use fold()

### remember{} Over hiltViewModel()
- **Date:** Project inception
- **Context:** ViewModel creation needed in Compose
- **Alternatives:** hiltViewModel(), viewModel(), remember{}, ViewModelProvider.Factory
- **Trade-offs:** remember{} bypasses ViewModel lifecycle; hiltViewModel() adds DI dependency
- **Decision:** `remember {}` inside composable routes
- **Reason:** No DI framework dependency, simpler setup, full control
- **Impact:** ViewModels not retained across navigation; recreated on route re-entry

### Direct Color Constants Over MaterialTheme.colorScheme
- **Date:** Project inception
- **Context:** Theme color system needed for consistent UI
- **Alternatives:** MaterialTheme.colorScheme.*, direct constants, custom color system
- **Trade-offs:** MaterialTheme enables dynamic theming; direct constants are simpler and predictable
- **Decision:** Direct color constants from `ui/theme/Color.kt`
- **Reason:** Simplicity, predictability, no dynamic theming requirement
- **Impact:** All screens use direct constants; MaterialTheme color scheme is decorative

### Single Light Theme Over Dark Mode
- **Date:** Project inception
- **Context:** Theme system needed for the application
- **Alternatives:** Light only, dark only, both with switching
- **Trade-offs:** Dark mode requires additional color definitions and testing
- **Decision:** Single light theme only
- **Reason:** Simplicity, no dark mode requirement from design, reduced maintenance
- **Impact:** Theme.kt defines lightColorScheme only; no dark variant exists

## Recording New Decisions

When making a significant architectural decision:

1. Add a new entry using the template above
2. Be honest about trade-offs
3. Record the actual reason, not the idealized reason
4. Note which files/patterns are affected
5. Ensure the decision is consistent with `manifesto.md`
