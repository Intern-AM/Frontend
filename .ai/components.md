# Components

This document defines the design principles and composition patterns for UI components.
It is the single source of truth for how components are structured and composed.

## Composition Philosophy

- Components are composable functions, not classes
- One primary composable per file (small helpers can share)
- Components receive state via parameters, not by accessing ViewModels directly
- Components emit events upward via lambda parameters
- Screens own the Scaffold; components are content inside it

## State Ownership

- ViewModels own all business state
- Screens observe ViewModel state and pass it to components
- Components are stateless when possible (receive data, emit events)
- Local UI state (expanded/collapsed, password visibility) lives in the composable
- Business state never lives in a composable

## Reusable Patterns

### StatusBadge
Renders a colored pill badge showing uppercase status. Color determined by `statusColor()` utility. Used across all list screens for consistent status display.

### SectionHeader
Section title with "All" link for navigation to full list. Used on Dashboard to link to Events and Campaigns screens.

### StatCard
Metric display card with title and value. Used on Dashboard and AdminDashboard for statistics.

### BottomNavBar
Shared navigation component with Home, Events, Campaigns, Notifications tabs. Used on all main screens except Login and detail screens.

### StatusUtils
`statusColor()` function maps status strings to theme colors. Single source of truth for status-to-color mapping across the entire application.

## Design Philosophy

- Cards are the primary content container
- Status is communicated via colored badges
- Loading is communicated via centered spinners
- Errors are communicated via red-tinted cards (PulseRedLight background)
- Success is communicated via green-tinted cards (PulseGreenLight background)
- Empty states use centered secondary text

## Theme Integration

Components use direct color constants from `ui/theme/`, not `MaterialTheme.colorScheme.*`.
This is a deliberate architectural decision documented in `theme.md`.

Color reference → `theme.md`
Component recipes → `playbook.md`
