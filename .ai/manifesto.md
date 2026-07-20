# Engineering Manifesto

This document explains WHY Hive AI is developed the way it is.
Every engineering rule in this workspace traces back to one of these principles.

## Core Principles

### Stability Over Cleverness

We choose the boring, proven approach every time. Clever solutions create maintenance burden. The codebase should be readable by any engineer who knows Android fundamentals. If a solution requires explanation, it is the wrong solution.

### Consistency Over Novelty

We follow existing patterns even when a newer approach exists. Consistency reduces cognitive load. Novel patterns require team-wide adoption to be valuable. A codebase with one consistent pattern is better than a codebase with five "better" patterns.

### Incremental Over Rewrites

We extend, refactor, and improve incrementally. Large rewrites introduce risk that is disproportionate to their benefit. Small changes are reviewable, testable, and reversible. The best refactoring is the one nobody notices.

### Reuse Over Duplication

We extract shared logic into reusable components and utilities. Duplication diverges over time — each copy evolves independently, creating subtle inconsistencies. Shared code converges — improvements propagate automatically.

### Plan Before Implementing

We read existing code before modifying it. We understand the pattern before introducing new code. We assess regression risk before making changes. The five minutes spent reading saves the hour spent fixing.

### Preserve Architecture

MVVM is the law. Repositories are the data layer. ViewModels are the state layer. Screens are pure composables. We do not compromise this structure for convenience. Architecture is the foundation — you do not remove foundation stones to add a window.

### Minimize Regression Risk

Every change is evaluated for its blast radius. We prefer changes that affect fewer files. We prefer changes that are reversible. We verify builds after every change. The cost of a regression is always higher than the cost of caution.

### Read Before Modifying

Before touching any file, we read its surrounding context. We understand imports, dependencies, and patterns. We mimic existing code style. The codebase is the authority — not our assumptions about how it should be written.

### Small Isolated Changes

One logical change per implementation. One concern per file. One responsibility per component. Large changes hide bugs. Small changes expose them.

## Why These Principles Matter

These principles are not arbitrary. They emerge from a specific engineering context:

- This is a production application serving real users
- Multiple AI agents will work on this codebase over time
- Consistency enables AI agents to make correct decisions autonomously
- Architecture preservation prevents slow degradation over time
- Regression prevention protects users from broken experiences

When in doubt, return to these principles. They are the foundation of every rule in this workspace.
