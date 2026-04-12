# Issue 6: BadgeEngine domain module + tests (all 12 badges)

## Parent PRD
docs/specs/2026-04-12-weightflow-master-prd.md

## What to build
A pure Kotlin engine that evaluates which badges a user has earned given their full log history and profile. Deterministic, side-effect free. Called on every write to Room; result stored/compared to surface new badge unlocks.

End-to-end: given a list of 7 consecutive daily entries + a goal set → `BadgeEngine.evaluate(logs, profile)` returns `{FIRST_WEIGH_IN, GOAL_SET, SEVEN_DAY_STREAK, TEN_LOGS}`.

## Acceptance criteria
- [ ] `Badge` enum: FIRST_WEIGH_IN, GOAL_SET, SEVEN_DAY_STREAK, THIRTY_DAY_STREAK, HUNDRED_DAY_STREAK, TEN_LOGS, FIFTY_LOGS, THREE_SIXTY_FIVE_LOGS, HALFWAY_THERE, GOAL_CRUSHER, COMEBACK, STEADY_STATE
- [ ] `BadgeEngine.evaluate(logs: List<WeightEntry>, profile: UserProfile): Set<Badge>` — pure function
- [ ] Streak calculation: consecutive calendar days (not 24h windows)
- [ ] COMEBACK: triggered when current log follows a 14+ day gap in history
- [ ] STEADY_STATE: 30 calendar days in maintenance mode (requires maintenanceMode=true in profile + 30 days of logs since mode activated)
- [ ] No Android imports
- [ ] `BadgeEngineTest`: each badge triggers at correct threshold; no badge fires early; earned set is idempotent (same input → same output); all 12 badges tested independently

## Blocked by
- Blocked by #2 (Room schema — needs WeightEntry shape)

## User stories addressed
44–57 (all 12 badge stories).
