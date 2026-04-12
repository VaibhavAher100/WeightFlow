# WeightFlow — Master PRD
_Created: 2026-04-12 | Source: grill-me session + product strategy_

---

## Problem Statement

Weight tracking apps force users into a false choice: free apps are bloated, ad-heavy, and push aggressive subscription upsells; paid apps charge $40–70/year for features that should be basic. Users who care about their health data have no genuinely free, beautiful, offline-first option that respects their data ownership and doesn't make them feel guilty for not paying.

Casual users get overwhelmed by calorie counting and macro tracking they didn't ask for. Serious trackers get locked out of the analytics they need behind paywalls. Neither group is served well.

---

## Solution

WeightFlow is a free, offline-first Android weight tracker built on three principles:

1. **Genuinely free.** Core features — unlimited logging, all charts, all themes, full history, goals, badges, CSV import/export — are free forever. No trial periods. No feature locks on the basics.
2. **Beautiful and fast.** "Athlete's Journal" aesthetic: warm dark base, electric lime accent, Bebas Neue numbers, Outfit UI. Logging takes one tap and three seconds.
3. **Data stays on device.** No account required. No cloud sync unless the user explicitly wants it (Pro, Phase 5). GDPR-compliant by architecture.

Advanced users who naturally discover deeper analytics features can unlock **Athlete Pro** — a one-time 4.99€ payment for serious trackers who want trend intelligence, body composition tracking, and health platform sync. Pro is never advertised aggressively; it is discovered contextually.

Revenue comes primarily from donations (Ko-fi / Liberapay / GitHub Sponsors) and secondarily from Pro unlock. The app sustains itself with zero ongoing server costs.

---

## User Stories

### Onboarding

1. As a new user, I want to confirm I am 13 or older before using the app, so that the app complies with COPPA requirements.
2. As a new user, I want to choose my preferred weight unit (kg / lbs / st) on first launch, so that all weights are shown in my preferred unit from day one.
3. As a new user, I want to enter my current weight during onboarding, so that my starting point is recorded immediately.
4. As a new user, I want to optionally set a goal weight during onboarding, so that the app can show me progress toward my target.
5. As a new user, I want onboarding to take under 30 seconds, so that I can start logging immediately without filling out a form.
6. As a new user, I want to optionally enable a daily reminder during onboarding with a time picker, so that I can build a consistent logging habit from the start.
7. As a new user, I want to see the full Home screen layout with motivating empty states from day one, so that I understand what the app will look like once I start logging.

### Logging

8. As a user, I want to tap a FAB to log my weight from any screen, so that logging is always one tap away regardless of where I am in the app.
9. As a user, I want a number pad to appear when I tap the FAB, so that weight entry is fast and thumb-friendly.
10. As a user, I want the log date to be pre-filled to today, so that I don't have to set it manually for a normal morning weigh-in.
11. As a user, I want to edit the log date before saving, so that I can backfill a missed entry.
12. As a user, I want my entry saved instantly with one tap, so that the flow is as fast as possible.
13. As a user, I want to see my new entry reflected on the Home screen immediately after logging, so that I get instant feedback.

### Home Screen

14. As a user, I want to see my current weight as a large prominent number on the Home screen, so that I can read my progress at a glance.
15. As a user, I want to see a trend indicator (up/down arrow + delta) next to my current weight, so that I know immediately whether I'm moving in the right direction.
16. As a user, I want to see a 7-day sparkline chart on the Home screen, so that I can see my recent trajectory without navigating to Trends.
17. As a user, I want to see a goal progress bar on the Home screen, so that I can track how close I am to my goal weight.
18. As a user, I want to see how many days until my goal target date on the Home screen, so that I feel a sense of urgency and momentum.
19. As a user, I want to see my current logging streak on the Home screen, so that I am motivated to maintain consistency.
20. As a user, I want to see my three most recent log entries on the Home screen, so that I can quickly verify my last few weigh-ins.
21. As a new user with no data, I want Home screen components to show motivating empty states (e.g., "Log 7 days to unlock your trend"), so that I understand what each component does and am encouraged to keep logging.
22. As a user who has reached their goal, I want the Home screen to shift to maintenance mode, showing a target range (±1kg around goal) instead of a progress bar, so that the app remains useful after I hit my goal.
23. As a user in maintenance mode, I want the Home screen to visually flag when my weight drifts outside my maintenance range, so that I can take corrective action.

### Trends Screen

24. As a user, I want to view my weight history as a line chart, so that I can see my long-term trajectory.
25. As a user, I want to switch between line, bar, area, and candlestick chart types, so that I can visualise my data in the format that works best for me.
26. As a user, I want to filter my chart by time range (7D / 30D / 3M / 6M / 1Y / All), so that I can zoom in or out on my progress.
27. As a user who logs multiple times per day, I want to use the candlestick chart to see daily high/low/open/close weights, so that I can understand my intra-day weight variance.
28. As a free user, I want to see a blurred "Pro Analytics" card below the chart with a single unlock CTA, so that I am aware of advanced features without feeling pressured.
29. As a Pro user, I want to see an EWMA-smoothed trend line on my chart, so that I can see the real underlying trend without daily fluctuation noise.
30. As a Pro user, I want plateau detection alerts on my chart, so that I know when my weight has stalled and can adjust my approach.
31. As a Pro user, I want to see my rate of change (e.g., "−0.4kg/week"), so that I can gauge the pace of my progress.
32. As a Pro user, I want to see a goal projection with confidence interval on my chart, so that I have a realistic estimated date of goal completion.

### History Screen

33. As a user, I want to see a reverse-chronological list of all my weight entries, so that I can review my full history.
34. As a user, I want each history row to show the date and weight clearly, so that I can scan entries quickly.
35. As a user, I want to swipe left on an entry to delete it, so that I can remove incorrect entries without opening a separate screen.
36. As a user, I want to tap an entry to open an edit sheet, so that I can correct the weight or date of any past entry.
37. As a user, I want a confirmation prompt before a delete is finalised, so that I don't lose data by accident.

### Goals

38. As a user, I want to set a single active goal (current weight → goal weight → optional target date), so that the app can track my progress.
39. As a user, I want a celebration screen when I reach my goal weight, so that the app acknowledges my achievement.
40. As a user, I want to be presented with a clear choice at goal completion — "Maintain this weight" or "Set a new goal" — with a one-line explanation of each, so that I can decide my next phase without confusion.
41. As a user who chooses maintenance mode, I want the app to track my weight within a ±1kg target range, so that I can sustain my result.
42. As a user who chooses to set a new goal, I want to immediately chain to a new goal without losing my history, so that I can continue tracking toward a new target.
43. As a user, I want to edit my goal at any time from the Profile screen, so that I can adjust if my target changes.

### Badges

44. As a user, I want to earn the **First Weigh-In** badge when I log my first weight, so that my first action is acknowledged.
45. As a user, I want to earn the **Goal Set** badge when I set my first goal, so that goal-setting behaviour is rewarded.
46. As a user, I want to earn the **7-Day Streak** badge after 7 consecutive days logged, so that early consistency is recognised.
47. As a user, I want to earn the **30-Day Streak** badge after 30 consecutive days logged, so that sustained habits are celebrated.
48. As a user, I want to earn the **100-Day Streak** badge after 100 consecutive days logged, so that elite consistency is honoured.
49. As a user, I want to earn the **10 Logs** badge after 10 total entries, so that early volume milestones are recognised.
50. As a user, I want to earn the **50 Logs** badge after 50 total entries, so that I feel progress accumulating.
51. As a user, I want to earn the **365 Logs** badge after 365 total entries, so that one full year of tracking is celebrated as a major achievement.
52. As a user, I want to earn the **Halfway There** badge when I reach 50% of my goal, so that mid-journey progress is acknowledged.
53. As a user, I want to earn the **Goal Crusher** badge when I reach my goal weight, so that the biggest achievement is recognised.
54. As a user, I want to earn the **Comeback** badge when I log again after a 14+ day gap, so that returning users are welcomed back rather than shamed.
55. As a user, I want to earn the **Steady State** badge after 30 days in maintenance mode, so that maintaining results is celebrated as much as losing weight.
56. As a user, I want to see all my earned and locked badges in my Profile, so that I have a clear picture of my achievements and what's next.
57. As a user, I want a visual animation when I earn a badge, so that the moment feels rewarding.

### Profile & Settings

58. As a user, I want to see my name, current weight, goal, and badge collection on the Profile screen, so that it serves as my personal dashboard.
59. As a user, I want to access Settings from the Profile screen, so that configuration is one level deep and not a top-level tab.
60. As a user, I want to change my weight unit (kg / lbs / st) in Settings and have all existing entries recalculate automatically, so that switching units doesn't corrupt my data.
61. As a user, I want to choose from 8 colour themes in Settings, so that the app feels personal.
62. As a user, I want to toggle dark / light / system display mode in Settings, so that the app matches my device preference.
63. As a user, I want to enable or disable my daily reminder and change its time in Settings, so that I can adjust my logging habit nudge.
64. As a user, I want to export all my data as a CSV from Settings, so that I own my data and can use it elsewhere.
65. As a user, I want to import a CSV from WeightFit, Happy Scale, Apple Health export, or a generic format in Settings, so that I can migrate from any other app without losing my history.
66. As a user, I want a "Delete all data" option in Settings with a strong confirmation prompt, so that I can wipe the app completely if needed.
67. As a user, I want to see the app version, changelog, privacy policy, open source licences, and donation links in Settings → About, so that I can learn about the app and support its development.
68. As a user, I want a "Rate this app" shortcut in Settings → About, so that I can leave a review easily if I love the app.

### Data Migration

69. As a WeightFit user, I want to import my WeightFit CSV export directly, so that I can switch apps in under a minute without manual data entry.
70. As a Happy Scale user, I want to import my Happy Scale CSV export directly, so that my full history transfers automatically.
71. As an Apple Health user, I want to import my Apple Health body mass export, so that my iOS history is preserved when I switch to Android.
72. As a user from any other app, I want to import a generic CSV by mapping date and weight columns, so that no app is a dead end.
73. As a user importing data, I want duplicate entries to be detected and skipped, so that my history isn't polluted with repeated logs.
74. As a user importing data, I want a summary after import ("127 entries imported, 3 skipped as duplicates"), so that I know exactly what happened.

### Notifications

75. As a user who enabled reminders, I want a daily notification at my chosen time prompting me to weigh in, so that logging becomes an automatic habit.
76. As a user, I want the notification to deep-link directly to the log bottom sheet, so that I can log with zero navigation after tapping the notification.
77. As a user, I want to disable reminders from Settings without navigating through system settings, so that opting out is frictionless.

### Accessibility & Internationalisation

78. As a screen reader user, I want all interactive elements to have content descriptions, so that TalkBack works correctly throughout the app.
79. As a user with a visual impairment, I want the app to support system font size scaling, so that text remains readable at any system text size.
80. As an Arabic, Hebrew, or Farsi speaker, I want layouts to mirror correctly in RTL, so that the app feels native in my language.
81. As a German speaker, I want the full app UI in German at launch, so that I don't have to use an English app.

### Pro / Monetisation

82. As a free user who discovers a Pro feature, I want to see a clear explanation of what Pro unlocks with a single payment CTA, so that the upgrade decision is informed and low-pressure.
83. As a user considering Pro, I want to pay a one-time fee (4.99€ or local equivalent) — not a subscription — so that I own the upgrade permanently.
84. As a Pro user, I want to restore my purchase on a new device, so that I don't have to pay twice.
85. As a user in Settings → About, I want to find donation links (Ko-fi, Liberapay, GitHub Sponsors), so that I can support the app's development if I find it valuable.

---

## Implementation Decisions

### Architecture
- MVVM with Repository pattern throughout. ViewModels expose `StateFlow<UiState>` consumed via `collectAsStateWithLifecycle()`.
- Manual DI via `WeightFlowApp` Application class. No Hilt — keeps complexity appropriate for a solo project.
- Room is the single source of truth. All UI reads from Room via `Flow`; writes go through the Repository.
- DataStore (not SharedPreferences) for user preferences: unit, theme, reminder time/enabled, onboarding complete flag.

### Navigation
- Single Activity. NavHost with 4 bottom-nav destinations: Home · Trends · History · Profile.
- Log entry is a `ModalBottomSheet` triggered by a FAB persistent across all tabs.
- Onboarding is a separate NavGraph shown only when `onboardingComplete = false` in DataStore.
- Settings is a composable destination pushed from Profile — not a top-level tab.

### Data Layer Modules
- `WeightEntryEntity` — fields: id (auto), timestamp (Long, epoch ms), weightKg (Double, always stored in kg regardless of display unit), note (reserved, empty string for now).
- `UserProfileEntity` — fields: id (singleton, always id=1), displayName, goalWeightKg, targetDate (nullable Long), heightCm (nullable), maintenanceMode (Boolean), maintenanceRangeKg (Double, default 1.0).
- `UserPrefsDataStore` — wraps DataStore Proto or Preferences. Exposes: unitPref (Flow), themePref (Flow), reminderEnabled (Flow), reminderHour (Flow), reminderMinute (Flow), onboardingComplete (Flow).
- All weights stored internally in kg. Conversion to lbs/st happens at display time only — never stored in user's preferred unit.

### Domain Modules (pure Kotlin, no Android deps)
- `WeightConverter` — stateless object. Functions: `kgToLbs(kg: Double)`, `lbsToKg(lbs: Double)`, `kgToStones(kg: Double)`, `stonesToKg(stones: Double)`, `format(kg: Double, unit: WeightUnit): String`.
- `GoalProgressCalculator` — input: current weight, goal weight, start weight, target date. Output: progressPercent, daysRemaining, isInMaintenanceZone, driftDirection. Pure function.
- `BadgeEngine` — input: full log list + user profile. Output: `Set<Badge>` of earned badges. Evaluated on every write. Pure function, deterministic.
- `CsvParser` — sealed class hierarchy: `WeightFitParser`, `HappyScaleParser`, `AppleHealthParser`, `GenericCsvParser`. Each returns `List<WeightEntry>` or `ParseResult.Error`. Pure function per parser.
- `CsvExporter` — input: `List<WeightEntry>` + unit preference. Output: CSV string. Pure function.

### UI Modules
- Home screen: `HomeUiState` holds currentWeightKg, trendDelta, sparklineData (last 7 entries), goalProgress, streakCount, recentEntries (last 3), maintenanceMode flag, maintenanceDrift.
- Trends screen: `TrendsUiState` holds chartData, selectedChartType, selectedTimeRange, proUnlocked flag. Pro data (EWMA series, plateauRanges, rateOfChange, projection) included but null when not Pro.
- Log bottom sheet: `LogEntryUiState` holds weightInput (String), selectedDate (LocalDate), isLoading, error.
- All ViewModels follow the pattern: `uiState: StateFlow<ScreenUiState>` + `onEvent(event: ScreenEvent)` handler.

### Theme System
- 8 accent palettes defined in `Color.kt` as named objects. Active palette selected at runtime from DataStore preference and passed into `MaterialTheme`.
- Base palette (warm dark): bg #0F0E0B, card #1C1B18, elevated #252420, surface #2E2D28, text-primary #F2F0E8, text-secondary #8C8A80.
- Chalk theme (#F5F5F5 accent) is the light mode default — applied automatically when display mode = Light.
- Typography: Bebas Neue for all numeric displays; Outfit (weights 300–800) for all UI text.

### Notification System
- `WorkManager` one-time `PeriodicWorkRequest` rescheduled daily. Fires at user-specified time.
- Notification taps deep-link to MainActivity with intent extra that auto-opens the log bottom sheet.
- `POST_NOTIFICATIONS` permission requested during onboarding reminder opt-in flow, not at cold launch.

### CSV Import
- Each parser is isolated and independently testable. `GenericCsvParser` detects date and weight columns by header name heuristics.
- Duplicate detection: entry is a duplicate if timestamp (truncated to day) + weightKg (rounded to 2dp) already exists.
- Import is a background operation via coroutine (not blocking UI). Result delivered via `StateFlow`.

### Pro IAP
- Google Play Billing v6. Single non-consumable in-app product: `athlete_pro_onetime`.
- Purchase state persisted locally in DataStore after verification. Restore purchase checks Play Billing on each app launch.
- Pro features gated by `proUnlocked: Boolean` from DataStore. No server-side verification required for Phase 4 (offline-first).

### Accessibility
- All interactive composables have `Modifier.semantics { contentDescription = "..." }`.
- `start/end` padding semantics used throughout — no `left/right` hardcoding. RTL layout correct by construction.
- Dynamic font size: use `sp` units throughout, never `dp` for text.

---

## Testing Decisions

**What makes a good test here:** Tests cover observable behaviour, not internal implementation. For domain modules: given inputs → expected outputs. For DAOs: given database state → expected query results. No mocking of the database — use Room's in-memory database builder for DAO tests. No testing of Compose rendering details — test ViewModel state transitions instead.

### Unit Tests (JVM, no device needed)
- `WeightConverterTest` — all conversions round-trip within 0.01 tolerance; edge cases (0, very large values, negative).
- `GoalProgressCalculatorTest` — progress %, maintenance zone detection, days remaining, drift direction.
- `BadgeEngineTest` — each of the 12 badge triggers fires at the correct condition; no badge fires early; earned badges are idempotent.
- `CsvParserTest` — each parser: valid file parses correctly; malformed file returns error; duplicate detection works; empty file handled.
- `CsvExporterTest` — output contains correct headers, correct unit conversion, correct date format.

### Instrumented Tests (Room in-memory DB)
- `WeightEntryDaoTest` — insert, query by date range, delete, update; flow emissions on change.
- `UserProfileDaoTest` — upsert singleton profile; read back correctly.

### ViewModel Tests (using `TestCoroutineDispatcher`)
- `HomeViewModelTest` — correct UiState emitted for: no data, 1 entry, 7 entries (sparkline), goal set, goal reached, maintenance mode.
- `LogEntryViewModelTest` — save emits success state; invalid input emits error state; date edit reflected in state.

---

## Out of Scope

- **Pro features (Phase 4–5):** EWMA, plateau detection, rate-of-change, goal projection, body composition tracking, Google Fit / Apple Health two-way sync. Architecture supports them; implementation deferred.
- **Cloud backup / Firebase sync** — Phase 5.
- **iOS / KMP** — Phase 5.
- **AdMob integration** — Phase 4.
- **Fasting timer** — 2027 roadmap.
- **Tablet / foldable adaptive layouts** — Phase 3 polish.
- **Hindi translation** — Phase 3.
- **Community-driven translations (Crowdin)** — Phase 3+.
- **F-Droid submission / open source** — deferred, decision not yet made.
- **Razorpay (India payments)** — Phase 4, after Play Billing is working.

---

## Further Notes

- **Keystore backup is CRITICAL** — must be done before the first release build and backed up to 3 locations. Losing the keystore means the app can never be updated on the Play Store.
- **Firebase Crashlytics** — add in Phase 1, not later. You won't know what's crashing in the wild without it.
- **APK size budget** — target <15MB download. Use AAB (not APK) for Play Store. Audit Vico + custom font assets before Phase 4.
- **Room migrations** — every schema change requires a `Migration` object. Never use `fallbackToDestructiveMigration()` in production.
- **All weights stored in kg internally** — unit conversion is a display concern only. This decision is locked and must not be revisited.
- **Revenue framing** — the app is marketed as free. Donations are the primary revenue ask. Pro is discovered silently. Never frame the app as "freemium" in marketing copy.
- **Play Store Small Business Program** — enroll at launch for 15% fee instead of 30%.
- **China locale** — Firebase features must be disabled for CN locale. Implement locale detection before Phase 5 cloud sync.
