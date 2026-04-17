# ADR-003: BadgeObserver Reactive Combine Flow

**Status:** Accepted  
**Date:** 2026-04-16  
**RFC:** #24

## Context
`BadgeEngine.evaluate()` was defined but never called reactively. Badges could only be evaluated imperatively after each save — exactly what RFC #24 forbids. Phase 3 badge-unlock toasts require a reactive Flow that re-evaluates whenever entries or profile change.

## Decision
Introduce `BadgeObserver` interface and `BadgeObserverImpl`. Uses `combine(entriesFlow, profileFlow)` to reactively evaluate `BadgeEngine`. Surfaces `allEarnedBadges: Flow<Set<Badge>>` and `newlyUnlockedBadges: Flow<Set<Badge>>` (earned minus seen). Seen-badge state persists in `UserPrefsDataStore` via `stringSetPreferencesKey("seen_badges")`, preventing re-notification on cold start.

## Why DataStore for Seen Badges
Set<String> is a first-class DataStore Preferences type. No Room migration needed. Badge set is small (≤12 entries). Schema changes in Room for a simple set felt like overengineering.

## Consequences
- Phase 3 collects `newlyUnlockedBadges` in HomeViewModel to show badge toasts/animations
- `BadgeObserver` wired into `WeightFlowApp` manual DI
- 6 unit tests in `BadgeObserverTest.kt`
- `BadgeEngine` itself unchanged — pure evaluator
