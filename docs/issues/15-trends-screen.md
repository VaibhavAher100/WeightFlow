# Issue 15: Trends screen (4 chart types + 6 filters + blurred Pro card)

## Parent PRD
docs/specs/2026-04-12-weightflow-master-prd.md

## What to build
The Trends screen with full Vico chart implementation: 4 chart types, 6 time filters, and a blurred "Pro Analytics" card below the chart for free users.

End-to-end: user selects "3M" filter + "Candlestick" chart type → chart re-renders showing daily high/low/open/close for the past 3 months. Free user sees blurred Pro card below with "Unlock Athlete Analytics" CTA.

## Acceptance criteria
- [ ] Chart type selector: Line / Bar / Area / Candlestick — tab-style toggle above chart
- [ ] Time filter: 7D / 30D / 3M / 6M / 1Y / All — chip row below selector
- [ ] Vico chart renders correctly for all 4 types with real data
- [ ] Candlestick: shows daily high/low/open/close (requires multiple entries per day; falls back to line if single entry per day)
- [ ] Chart accent colour matches active theme; axes in text-secondary colour
- [ ] Empty state: "Start logging to see your trends" when no data
- [ ] Pro Analytics card: blurred preview section below chart, visible to free users, single "Unlock Athlete Analytics" CTA (tapping opens upgrade sheet — stubbed for now, full IAP in issue #X)
- [ ] `TrendsViewModel`: `StateFlow<TrendsUiState>` (chartData, selectedType, selectedRange, proUnlocked=false)
- [ ] RTL-safe; chart has content description for accessibility

## Blocked by
- Blocked by #9 (repository layer)
- Blocked by #11 (NavGraph shell)

## User stories addressed
24–28 (free Trends stories), 29–32 (Pro Trends stories — stubbed/blurred).
