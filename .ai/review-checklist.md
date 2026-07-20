# Review Checklist

Run this checklist before every implementation is considered complete.
Every item must be verified.

## Build

- [ ] Project builds successfully (`./gradlew assembleDebug`)
- [ ] No new warnings introduced
- [ ] No new deprecation warnings

## Code Integrity

- [ ] No unrelated files modified
- [ ] No dead code or orphaned imports introduced
- [ ] No unnecessary comments added
- [ ] No duplicate code introduced

## Architecture

- [ ] MVVM architecture preserved
- [ ] Repository Pattern preserved
- [ ] ViewModel responsibilities preserved
- [ ] API contracts preserved
- [ ] Navigation structure preserved
- [ ] Authentication flow preserved

## Theme

- [ ] Theme consistency maintained
- [ ] Direct color constants used (not MaterialTheme.colorScheme.*)
- [ ] Correct typography (BitcountSingle for headings, Lilex for rest)
- [ ] Standard shape values used (20/16/12/6/100dp)

## State

- [ ] `private set` on all ViewModel state properties
- [ ] `errorMessage` cleared at start of operations
- [ ] `isLoading`/`isProcessing` toggled correctly
- [ ] `key` parameters on all LazyColumn items

## Regression

- [ ] Regression risks evaluated
- [ ] Existing screens still function correctly
- [ ] Authentication works across all roles
- [ ] Navigation works correctly
- [ ] Error handling covers failure cases
- [ ] Loading states properly managed

## Completion

- [ ] Hard constraints from AGENTS.md not violated
- [ ] No anti-patterns introduced (check `anti-patterns.md`)
- [ ] Workflow followed (check `workflow.md`)
