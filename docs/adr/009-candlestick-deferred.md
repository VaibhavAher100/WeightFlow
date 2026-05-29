# 9. Candlestick chart deferred

Date: 2026-05-29
Status: Accepted

## Context
PRD stories 25 & 27 specify four chart types: line, bar, area, candlestick.
Plan 2 built a ChartType toggle; the 2026-05-07 UI/UX overhaul removed it.
This change restores line, bar, and area.

## Decision
Defer candlestick. It requires an intra-day OHLC (open/high/low/close per day)
data model derived from multiple same-day weigh-ins, plus verification of Vico
1.13.1 candlestick support — a feature-sized effort, not a quick restore.

## Consequences
- Trends ships with 3 of 4 planned chart types.
- Candlestick is tracked as future work, not silent drift.
- Revisit when intra-day logging / OHLC aggregation is scoped.
