# Theme

This document defines the design system for Hive AI.
It is the single source of truth for colors, typography, shapes, and spacing.

## The PureBlack Trap

`PureBlack` = #F8FAFC. It is the lightest color in the palette. It is the app background. It is NOT black. This is the single most important thing to know about this project's theme.

Every future engineer and AI agent must understand this before writing any UI code.

## Colors

### Background and Surface

| Constant | Hex | Usage |
|----------|-----|-------|
| PureBlack | #F8FAFC | Screen backgrounds, TopAppBar |
| CardSurface | #FFFFFF | Card/dialog backgrounds |
| ElevatedSurface | #F1F5F9 | Elevated surfaces |
| CardBorder | #E2E8F0 | Card outlines, text field borders |

### Text

| Constant | Hex | Usage |
|----------|-----|-------|
| TextPrimary | #0F172A | Primary text |
| TextSecondary | #64748B | Subtitles, descriptions |
| TextMuted | #94A3B8 | Timestamps, tertiary text |

### Accent

| Constant | Hex | Usage |
|----------|-----|-------|
| PulseBlue | #2563EB | Primary actions, focus borders, selected nav |
| PulseGreen | #16A34A | Approved/active status |
| PulseAmber | #F59E0B | Pending status |
| PulseRed | #EF4444 | Error/rejected, destructive actions |
| PulsePurple | #7C3AED | Scheduled status |

### Light Variants

| Constant | Hex | Usage |
|----------|-----|-------|
| PulseBlueLight | #EFF6FF | Blue-tinted card backgrounds |
| PulseGreenLight | #ECFDF5 | Success banners, green-tinted cards |
| PulseAmberLight | #FFFBEB | Amber-tinted card backgrounds |
| PulseRedLight | #FEF2F2 | Error banners, red-tinted cards |
| PulsePurpleLight | #F5F3FF | Purple-tinted card backgrounds |

## Typography

Two custom font families loaded from `res/font/`:

- **BitcountSingle** — display and headline levels only (branding, page headings)
- **Lilex** — everything else (titles, body, labels)

### Material 3 Style Mapping

| M3 Slot | Font | Weight | Size | Usage |
|---------|------|--------|------|-------|
| displayLarge | Bitcount | Bold | 28sp | Brand name, screen titles |
| headlineLarge | Bitcount | Bold | 32sp | Page-level headings |
| headlineMedium | Bitcount | Bold | 24sp | Section headers |
| headlineSmall | Bitcount | Bold | 20sp | Sub-headings, greetings |
| titleLarge | Lilex | SemiBold | 18sp | Card titles, button text |
| titleMedium | Lilex | SemiBold | 16sp | Card titles, user names |
| titleSmall | Lilex | SemiBold | 14sp | Button labels |
| bodyLarge | Lilex | Normal | 16sp | Primary body text |
| bodyMedium | Lilex | Normal | 14sp | Descriptions |
| bodySmall | Lilex | Normal | 12sp | Timestamps |
| labelLarge | Lilex | Medium | 14sp | Stat labels |
| labelMedium | Lilex | Medium | 12sp | Sub-labels |
| labelSmall | Lilex | Medium | 10sp | Uppercase badges |

## Shapes

| Value | Usage |
|-------|-------|
| 20.dp | Universal — cards, buttons, text fields, dialogs, FAB |
| 6.dp | Inline chips and tags |
| 12.dp | Dropdown menus |
| 100.dp | Status badges (pill/capsule) |

## Spacing

| Value | Usage |
|-------|-------|
| 4.dp | Tight spacing within cards |
| 8.dp | Standard tight spacing |
| 12.dp | Small gap (metadata rows) |
| 16.dp | Medium gap (between card sections, form fields) |
| 20.dp | Large gap (between fields and buttons), screen horizontal padding |
| 32.dp | Extra large gap (between dashboard sections) |
| 80.dp | Bottom padding for BottomNavBar clearance |

## Status Color Mapping

The `statusColor()` function in `StatusUtils.kt` maps status strings to colors:

| Status | Color |
|--------|-------|
| Approved, Active, Started | PulseGreen |
| Pending, Pending Approval, Draft | PulseAmber |
| Rejected, Cancelled, Inactive | PulseRed |
| Generated, Posted | PulseBlue |
| Scheduled | PulsePurple |
| Unknown | TextMuted |

## Hard Rules

- Never use `MaterialTheme.colorScheme.*` for colors — use direct constants
- Never use `FontFamily.Default`
- Never add dark mode
- Never use `Color.White` or `Color.Black` directly
- Always use `statusColor()` from StatusUtils for status colors

How theme is applied in components → `components.md`
