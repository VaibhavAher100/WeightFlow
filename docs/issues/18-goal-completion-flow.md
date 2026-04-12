# Issue 18: Goal completion flow (celebration + maintain vs new goal)

## Parent PRD
docs/specs/2026-04-12-weightflow-master-prd.md

## What to build
The moment a user reaches their goal weight: a celebration screen with animation, then a clear binary choice — maintain current weight or chain to a new goal.

End-to-end: user logs a weight at or below goal → `GoalProgressCalculator` detects `isGoalReached=true` → celebration screen appears automatically with animation + Goal Crusher badge unlock → user taps "Maintain" → maintenance mode activates → Home screen switches to maintenance display.

## Acceptance criteria
- [ ] Goal reached detection: triggered on every save in `LogEntryViewModel` via `GoalProgressCalculator`
- [ ] Celebration screen: full-screen overlay with animation (confetti or Lottie), "Goal Crusher!" heading, goal weight displayed, "Goal Crusher" badge shown
- [ ] Choice card: "Maintain [X]kg" with one-line explanation ("Stay within ±1kg of your goal") vs "Set a new goal" with one-line explanation ("Keep pushing toward a new target")
- [ ] "Maintain" tapped: `UserProfile.maintenanceMode=true` written to Room; celebration dismisses; Home shows maintenance display
- [ ] "Set a new goal" tapped: navigates to goal-setting sheet (reuses onboarding goal screen); new goal saved; history preserved
- [ ] GOAL_CRUSHER badge awarded at celebration moment (integrates with BadgeEngine result from #19, or stubbed until then)
- [ ] "Set a new goal" chains without losing any log history
- [ ] RTL-safe

## Blocked by
- Blocked by #5 (GoalProgressCalculator)
- Blocked by #14 (Home screen — maintenance mode display)

## User stories addressed
39–42 (goal completion + maintain vs new goal), 53 (Goal Crusher badge moment).
