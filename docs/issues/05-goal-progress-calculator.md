# Issue 5: GoalProgressCalculator domain module + tests

## Parent PRD
docs/specs/2026-04-12-weightflow-master-prd.md

## What to build
A pure Kotlin module that computes all goal-related derived state from raw weight data. No Android dependencies. Feeds the Home screen UiState and the goal completion flow.

End-to-end: given startWeight=100kg, goalWeight=80kg, currentWeight=90kg, targetDate=60 days away → progressPercent=50%, daysRemaining=60, isInMaintenanceZone=false.

## Acceptance criteria
- [ ] `GoalProgressCalculator` object with pure function: `calculate(startKg, goalKg, currentKg, targetDate?, maintenanceRangeKg): GoalState`
- [ ] `GoalState` data class: progressPercent (Float 0–100), daysRemaining (Int?), isGoalReached (Boolean), isInMaintenanceZone (Boolean), driftDirection (ABOVE/BELOW/IN_RANGE), maintenanceMessage (String?)
- [ ] Handles null targetDate (no deadline set)
- [ ] Handles weight gain goal (goal > start)
- [ ] Maintenance zone: currentKg within ±maintenanceRangeKg of goalKg
- [ ] No Android imports
- [ ] `GoalProgressCalculatorTest`: progress %, goal reached detection, maintenance zone in/out/above/below, null target date, gain goal

## Blocked by
- Blocked by #1 (project setup)

## User stories addressed
17–18 (Home progress bar + days remaining), 38–43 (goal flow), 22–23 (maintenance mode).
