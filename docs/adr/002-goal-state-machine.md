# ADR-002: GoalStateMachine Sealed FSM

**Status:** Accepted  
**Date:** 2026-04-16  
**RFC:** #26

## Context
Goal state was tracked via scattered boolean flags and nullable fields in `UserProfile` (`goalWeightKg`, `maintenanceMode`, `maintenanceModeActivatedAt`). No explicit FSM existed. Phase 3 requires goal-completion UI, maintenance transitions, and "start new goal" chains — building these without a FSM would produce ad-hoc boolean logic in ViewModels.

## Decision
Introduce `GoalState` sealed class with four states: `NoGoal`, `Active(goalWeightKg, startWeightKg?, targetDate?)`, `Achieved(goalWeightKg, achievedAt)`, `Maintenance(goalWeightKg, achievedAt, maintenanceSince)`. `GoalStateMachine` provides pure transition functions (`setGoal`, `markAchieved`, `enterMaintenance`, `clearGoal`, `startNewGoal`) plus `rehydrate(profile)` for cold-start reconstruction and `applyToProfile(state, profile)` for persistence.

## Persistence
Added `achievedAtEpochDay: Long?` column to `user_profile` table via Room Migration 1→2. `GoalStateMachine` is pure domain — no Android/Room imports.

## Consequences
- Phase 3 UI drives state via `GoalStateMachine` transition functions, never boolean flags directly
- 14 unit tests in `GoalStateMachineTest.kt`
- `GoalProgressCalculator` stays unchanged — purely a stateless progress calculator
