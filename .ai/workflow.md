# Workflow

This document defines the engineering workflow for Hive AI.
It is the single source of truth for how work should be planned, implemented, reviewed, and verified.

## The Workflow

```
Planning → Implementation → Regression Review → Testing → Build Verification → Completion
```

Every change follows this sequence. No steps should be skipped.

## Planning

- Read the relevant source files before making changes
- Understand the existing pattern for the area you're modifying
- Identify which files need to change
- Assess regression risk using the Change Classification in `AGENTS.md`
- If the change is High Risk or Critical, enter PLAN MODE first
- Review `anti-patterns.md` before implementing

## Implementation

- Make minimal, focused changes
- Follow existing patterns exactly
- One logical change per implementation
- Prefer extending existing code over creating new code
- After each significant step, run a build to verify

## Regression Review

- Check that no unrelated files were modified
- Check that existing navigation still works
- Check that authentication is preserved
- Check that theme consistency is maintained
- Check that API contracts are preserved
- Check that repository architecture is preserved

## Testing

- Manual testing across all three roles (Admin, Designer, Reviewer)
- Verify login, navigation, CRUD operations
- Verify edge cases (empty states, error states, loading states)
- Test the specific change and adjacent functionality

Testing details → `testing.md`

## Build Verification

- `./gradlew assembleDebug` must pass
- Zero errors AND zero warnings
- No new deprecation warnings
- No new compiler warnings

## Completion

- Run the full review checklist → `review-checklist.md`
- Confirm Definition of Done from `AGENTS.md`
- Commit with a descriptive message
