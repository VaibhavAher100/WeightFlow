# Plan: Free Feature Parity With Zero and WeightFit

- **Date:** 2026-11-20
- **Rule set:** ADR-013 (every feature free, data stays on the device).
- **Evidence:** `docs/strategy/competitor-gap-analysis.md` and the Craft Anything project `.tools/investigations/competitors.cany`.
- **Rule:** we copy no code, text or assets from Zero or WeightFit. We build each feature from our own design.

## How Each Slice Runs

1. Write a short design note in the slice issue (brainstorm only when the design is open).
2. Write the failing tests first (domain tests, then ViewModel tests).
3. Implement the domain logic, then the ViewModel, then the Compose UI.
4. Add every new UI string to `values/strings.xml` and `values-de/strings.xml` (the parity test fails otherwise).
5. If the slice changes the Room schema, add a `Migration` object and a migration test.
6. Run `./gradlew testDebugUnitTest lintDebug detekt ktlintCheck`.
7. Run the privacy gate: Craft Anything `android-package-show` on the APK. Confirm no INTERNET permission and no new SDK with network access.
8. Install on the OnePlus 7T and compare the screen with the competitor screen it replaces.

## Phase A: Weight Tracking (both competitors)

| # | Slice | Replaces | Schema change | Size |
|---|-------|----------|---------------|------|
| A1 | **Import and restore.** Wire `CsvImporter` into Settings (CSV from WeightFlow, WeightFit, Happy Scale, Apple Health). Add restore from our encrypted ZIP. Add a full backup that includes the profile, the goal and the settings. Add a "Switch from WeightFit" path in onboarding. | WeightFit Drive backup (top complaint: data lost on a new phone) | No | M |
| A2 | **Trend weight.** An exponential moving average line under the raw entries. A screen that explains the calculation. A smoothing setting. A "normal daily swing" note when the latest entry is near the trend. | WeightFit trend weight and fluctuation check | No | M |
| A3 | **Honest goal forecast.** A date range, not one number. The forecast stops and shows a reason when the data is too sparse, too unstable, flat, or moving away from the goal. | WeightFit forecast (Premium) | No | M |
| A4 | **Safety flags.** Warnings for rapid loss, an underweight goal, and loss while underweight. No forecast for users under 18. | WeightFit safety flags | No | S |
| A5 | **History views.** Day, week, month and year views with real averages. Month headers with count, average and range. Change chips in green, red and grey. | WeightFit history | No | M |
| A6 | **Range-band chart.** Weekly lowest-to-highest bars with an average dot in Trends. | WeightFit statistics chart | No | M |
| A7 | **Decimal places.** A setting for 1 or 2 decimals, and 2-decimal entry in the wheel picker. | WeightFit setting (review request) | No | S |
| A8 | **Milestones.** A timeline of 25/50/75% of the goal and round-number crossings, linked to badges. | WeightFit milestones | No | S |
| A9 | **Home screen widget.** Current weight, trend and a quick-log action (Glance). | WeightFit widget (Premium) | No | M |
| A10 | **Health Connect.** Opt-in read and write of weight and height. | Both apps | No | L |
| A11 | **Progress photos.** Photos in app-private storage, linked to entries, included in the encrypted backup. | WeightFit (Premium) | Yes | L |
| A12 | **App lock.** Biometric or device-credential lock. | WeightFit (Premium) | No | S |
| A13 | **Reports.** Weekly and monthly summaries and an on-device PDF export. | WeightFit (Premium) | No | M |
| A14 | **Body measurements.** Waist first, then hips, chest and body fat. | Review requests | Yes | L |

## Phase B: Fasting and Daily Habits (Zero, on-device parts only)

| # | Slice | Replaces | Schema change | Size |
|---|-------|----------|---------------|------|
| B1 | **Fasting timer.** Presets (13h, 16:8, 18:6, 20:4, OMAD, custom), start and end, an ongoing-fast notification, fast history. | Zero timer | Yes | L |
| B2 | **Fasting stages.** An educational display of the hours of a fast, with a medical disclaimer. | Zero fasting zones (Plus) | No | S |
| B3 | **Water log.** A daily water goal and quick-add buttons. | Zero hydration (Plus) | Yes | M |
| B4 | **Daily rings and calendar.** A day ring for weigh-in, fast and water. A month calendar of day rings. | Zero rings and calendar | No | M |
| B5 | **Modular Home.** Home cards that the user can reorder and hide. Each empty card has one action button. | Zero Today and Trends cards | No | M |

## Out of Scope (needs a network or a cloud service)

- AI meal photo analysis and the protein score (Zero).
- Coach content, articles, videos and challenges with other users (Zero).
- Cloud backup and sync (WeightFit Google Drive). Export and import replace it.
- Wear OS (Phase 5, needs its own ADR).

## Status

| Slice | Status | Evidence |
|-------|--------|----------|
| A1a CSV import in Settings | Done 2026-11-20 | 333 unit tests and 13 instrumented tests pass. lint, detekt and ktlint pass. On-device import of a WeightFit-format file: 5 added, then 0 added and 5 present on re-import. Privacy gate PASS. |
| A1b Full backup and restore | Next | Needs an ADR for `org.json` as a test dependency. |
| A1c Onboarding restore and "switch from WeightFit" | Open | |

## Order

A1 first: without restore, users lose data when they change phones. Then A2, A3 and A4 together, because the forecast and the safety flags use the trend weight. Then A5 to A9. Phase B starts after A9. A10 to A14 run in parallel with Phase B when capacity allows.
