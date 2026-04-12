# Issue 19: Badge integration (write-path wiring + animations + Profile display)

## Parent PRD
docs/specs/2026-04-12-weightflow-master-prd.md

## What to build
Wire the `BadgeEngine` into the write path so badges are evaluated on every log save. Surface newly earned badges with a celebration animation. Display the full badge grid in Profile.

End-to-end: user logs their 7th consecutive day → `BadgeEngine.evaluate()` runs → detects new SEVEN_DAY_STREAK badge → badge unlock animation appears → badge shown as earned (highlighted) in Profile badge grid.

## Acceptance criteria
- [ ] `BadgeEngine.evaluate()` called in `WeightRepository` after every insert/update
- [ ] Previously earned badges stored in `UserProfile` (as serialised Set or separate `EarnedBadge` table)
- [ ] New badge detection: diff between previous earned set and new evaluated set
- [ ] Badge unlock animation shown for each newly earned badge (bottom sheet or overlay with badge icon + name)
- [ ] Animation dismisses automatically after 3s or on tap
- [ ] Multiple badges earned simultaneously: shown sequentially
- [ ] Profile badge grid: 12 badges in fixed grid; earned = full colour with name; unearned = greyed silhouette with "?" label
- [ ] Tapping any badge (earned or not) shows tooltip: badge name + trigger description
- [ ] STEADY_STATE badge requires `maintenanceMode=true` + 30 days of logs since activation — tracked via `maintenanceModeActivatedAt` timestamp in `UserProfile`

## Blocked by
- Blocked by #6 (BadgeEngine)
- Blocked by #17 (Profile screen — badge grid display)

## User stories addressed
44–57 (all 12 badge stories), 57 (badge animation).
