# Issue 14: Home screen (dashboard + empty states + maintenance mode)

## Parent PRD
docs/specs/2026-04-12-weightflow-master-prd.md

## What to build
The primary screen users see every day. Full dashboard layout with all components always present, showing motivating empty states until data exists. Switches to maintenance mode display after goal is reached.

End-to-end: user with 8 days of data sees current weight (large), trend arrow + delta, 7-day sparkline, goal progress bar, days to goal, streak counter, last 3 entries. User with 0 data sees the same layout with motivating empty state copy in each component.

## Acceptance criteria
- [ ] `HomeScreen` composable renders all 6 dashboard components: current weight hero, trend indicator, 7-day sparkline, goal progress bar + days remaining, streak counter, recent entries list (last 3)
- [ ] All components present on day 1 with empty states: e.g. sparkline shows "Log 7 days to unlock your trend", progress bar shows "Set a goal to track progress"
- [ ] Current weight: Bebas Neue font, large, in user's unit; trend arrow (↑/↓/→) + delta vs previous entry
- [ ] Sparkline: Vico line chart, last 7 entries, accent colour line, no axes
- [ ] Goal progress: linear progress bar (0–100%), label shows "X.Xkg to go" + "N days left" if target date set
- [ ] Maintenance mode: replaces progress bar with range indicator (±1kg band); drifting outside range shown in danger colour
- [ ] Streak counter: consecutive days logged; resets if gap > 1 day
- [ ] `HomeViewModel`: `StateFlow<HomeUiState>` collecting from WeightRepository + UserProfileRepository + GoalProgressCalculator
- [ ] `HomeViewModelTest`: correct UiState for: no data, 1 entry, 7 entries, goal set, goal reached, maintenance in-range, maintenance drifting
- [ ] RTL-safe; content descriptions on all interactive elements

## Blocked by
- Blocked by #4 (WeightConverter)
- Blocked by #5 (GoalProgressCalculator)
- Blocked by #9 (repository layer)
- Blocked by #11 (NavGraph shell)

## User stories addressed
14–23 (all Home screen stories).
