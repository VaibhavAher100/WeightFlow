# Issue 4: WeightConverter domain module + tests

## Parent PRD
docs/specs/2026-04-12-weightflow-master-prd.md

## What to build
A pure Kotlin stateless object that handles all weight unit conversions. No Android dependencies. Called at display time only — the database always stores kg.

End-to-end: `WeightConverter.kgToLbs(80.0)` returns `176.37`. All conversion round-trips are accurate to within 0.01.

## Acceptance criteria
- [ ] `WeightConverter` object: `kgToLbs`, `lbsToKg`, `kgToStones`, `stonesToKg`, `format(kg, WeightUnit): String`
- [ ] `format()` returns locale-appropriate string with unit suffix (e.g., "80.0 kg", "176.4 lbs", "12st 8lb")
- [ ] No Android imports
- [ ] `WeightConverterTest` (JVM unit test): all conversions, round-trips within 0.01, edge cases (0, large values), format output for all 3 units

## Blocked by
- Blocked by #1 (project setup)

## User stories addressed
60 (unit switching recalculates all displayed weights).
