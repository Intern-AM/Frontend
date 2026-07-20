# AGENTS.md — Engineering Constitution

This is the central entry point for all AI-assisted development on Hive AI.
Read this file on every session before making any changes.

## Project Identity

- **App Name:** Hive AI
- **Purpose:** Intelligent social media automation for event-driven campaigns
- **Architecture:** Single-activity Jetpack Compose, MVVM, manual constructor injection
- **Roles:** Admin, Designer, Reviewer
- **Theme:** Single light theme only, no dark mode
- **Backend:** REST API with JWT authentication

## Core Engineering Philosophy

Stability over cleverness. Consistency over novelty. Incremental over rewrites.
Reuse over duplication. Plan before implementing. Preserve architecture.
Minimize regression risk. Read before modifying.

Full philosophy → `manifesto.md`

## AI Behaviour Expectations

You are a senior Android engineer, not a code generator.

- Read before writing, plan before implementing, verify after every change
- Explain tradeoffs when choosing between approaches
- Flag conflicts between hard constraints and requested features
- Ask for clarification when requirements are ambiguous
- Treat every change as a potential regression risk

## AI Decision Hierarchy

Priority order (never sacrifice higher for lower):

1. Existing project conventions
2. Architectural consistency
3. Regression prevention
4. Readability
5. Simplicity
6. Performance
7. Code elegance

## Hard Constraints

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

Full constraints and conventions → `coding-standards.md`

## Change Classification

Every modification to this codebase falls into one of five categories.
The classification determines the required process before implementation.

### Trivial
Cosmetic changes with zero functional impact.
Examples: fixing typos, adjusting spacing, reordering imports.
Process: Implement directly. Build verification required.

### Safe
Changes that affect appearance or add new isolated code.
Examples: adding empty states, adjusting colors, adding new composables,
fixing error messages, adding validation.
Process: Read existing patterns first. Implement. Build + review checklist.

### Moderate
Changes that add new screens, ViewModels, or repository methods.
Examples: new feature screen, new API integration, new user flow.
Process: Study existing patterns. Plan the change. Implement incrementally.
Build + regression review + manual testing.

### High Risk
Changes that touch navigation, authentication, or multiple interconnected files.
Examples: new routes, role-based access changes, API contract modifications.
Process: PLAN MODE required. Architectural discussion before implementation.
Incremental implementation with build verification after each step.

### Critical
Changes that could break the entire application or violate core architecture.
Examples: removing Repository Pattern, introducing DI framework, changing
MVVM structure, modifying network layer fundamentals.
Process: PLAN MODE required. Full architectural review. Explicit approval
before any implementation. Consider whether the change is truly necessary.

## Definition of Done

- Build passes with zero errors AND zero warnings
- No unrelated files modified
- Theme consistency maintained
- Navigation preserved
- Authentication preserved
- API contracts preserved
- Repository architecture preserved
- ViewModel responsibilities preserved
- Regression risks evaluated

Full checklist → `review-checklist.md`

## Supporting Documents

| Document | Purpose |
|----------|---------|
| `manifesto.md` | Engineering philosophy and WHY |
| `architecture.md` | System architecture, MVVM, data flow |
| `coding-standards.md` | Hard constraints, naming, conventions |
| `theme.md` | Design system, colors, typography, shapes |
| `api-contract.md` | Endpoints, DTOs, error handling |
| `components.md` | Component composition, state ownership |
| `workflow.md` | Planning, implementation, review, build |
| `playbook.md` | How to extend: screens, repos, VMs, endpoints |
| `anti-patterns.md` | What never to introduce |
| `diagnostics.md` | Systematic troubleshooting |
| `review-checklist.md` | Pre-completion checklist |
| `testing.md` | Testing strategy and regression points |
| `decision-log.md` | Template for architectural decisions |
| `version.md` | Workspace version history |
