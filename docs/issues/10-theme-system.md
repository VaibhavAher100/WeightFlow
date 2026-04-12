# Issue 10: Theme system (8 palettes + typography)

## Parent PRD
docs/specs/2026-04-12-weightflow-master-prd.md

## What to build
The complete visual design system: 8 accent colour palettes, dynamic MaterialTheme wrapper, and typography with Bebas Neue + Outfit fonts.

End-to-end: changing theme in DataStore to EMBER immediately re-renders the entire app with burnt orange accents. Bebas Neue is used for all numeric displays; Outfit for all UI text.

## Acceptance criteria
- [ ] `Color.kt`: base palette (bg #0F0E0B, card #1C1B18, elevated #252420, text-primary #F2F0E8, text-secondary #8C8A80, success #4ED98A, danger #FF6B6B) + 8 accent objects (Lime #C8FF00, Ember #FF6B35, Ice #00D4FF, Plasma #BF5AF2, Steel #8E9BAE, Crimson #FF3B30, Gold #FFD60A, Chalk #F5F5F5)
- [ ] `Theme.kt`: `WeightFlowTheme` composable accepts `accentPalette` + `darkTheme` params; wraps `MaterialTheme` with correct color scheme; Chalk accent auto-applied in light mode
- [ ] `Type.kt`: `WeightFlowTypography` — Bebas Neue for display/numeric styles, Outfit (300–800) for all UI styles
- [ ] Fonts bundled as assets (not fetched at runtime)
- [ ] `ThemeAccent` enum matches DataStore `themePref` values
- [ ] RTL-safe: all padding/alignment uses `start/end` semantics

## Blocked by
- Blocked by #1 (project setup)

## User stories addressed
61 (8 colour themes), 62 (dark/light/system mode), 80 (RTL layout safety).
