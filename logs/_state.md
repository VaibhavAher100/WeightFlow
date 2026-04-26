---
last_session: 2026-04-26-001
status: active
environment: isolated (WeightFlow/ only)
---

# WeightFlow Project State

_Single source of truth. Updated every session via `/wrap`._
_Always open `WeightFlow/` in Claude Code, never the root `102/`._

---

## App Identity
- **Name:** WeightFlow | **Package:** `com.weightflow`
- **Aesthetic:** "Athlete's Journal" — warm dark (#0F0E0B) + electric lime (#C8FF00)
- **Fonts:** Bebas Neue (numbers) + Outfit (UI)
- **Position:** Genuinely free weight tracker. No subscription. Data stays on device.

## Revenue Model (Locked)
- Core: always free (CSV import/export, all charts, all themes, history, badges)
- Pro: 4.99 EUR one-time (trend intelligence, body comp, health sync, ad removal)
- Pro discovery: contextual only, no persistent CTA anywhere
- Donations: Ko-fi + Liberapay + GitHub Sponsors (link in Settings -> About)
- Ads: small AdMob banner on free tier, non-core screens only

## Tech Stack (Locked)
- Kotlin 2.2.10 + Jetpack Compose BOM 2025.04.01
- AGP 9.1.1 + Gradle 9.3.1 (AGP 9.x has built-in Kotlin — no kotlin-android plugin)
- Room **2.7.0** (upgraded from 2.6.1 — not KSP2-compatible) | KSP 2.3.2
- DataStore 1.1.1 | Navigation Compose 2.8.9
- Vico 1.13.1 charts | Manual DI (no Hilt) | StateFlow
- WorkManager 2.10.1 (active — daily reminder notification)
- Google Fonts: `ui-text-google-fonts` (Compose BOM) — Bebas Neue + Outfit via GMS provider
- Test: JUnit 4.13.2 + mockk 1.13.12 + turbine 1.1.0 + kotlinx-coroutines-test 1.8.1
- CI: GitHub Actions (tests + lint + build on every push)

## Phase Status

| Phase | Description | Status |
|-------|-------------|--------|
| 0 | Infrastructure (environment, agents, skills, conventions, product strategy, PRD, issues) | Complete |
| 1 | Foundation (Android project + Room + DataStore + NavGraph shell) | Complete |
| 2 | All 6 screens + ViewModels + Vico + RFCs #24-26 | Complete (166 tests GREEN) |
| 3 | Polish + badge UI + goal banners + settings + accessibility + WorkManager | **Complete** (188 tests GREEN) — PR #30 merged |
| 4 | Play Store launch (privacy policy, signed build, ASO) | **In progress** |
| 5 | Firebase sync + AdMob + iOS via KMP | Needs planning |

## Phase 3 — COMPLETE (UI overhaul complete on main; PR #30 pending merge)

| Item | Status |
|------|--------|
| Error handling in LogEntryViewModel | **DONE** — try/catch, errorMessage surfaced |
| Badge snackbar on HomeScreen | **DONE** — SharedFlow + LaunchedEffect |
| BadgeGrid on ProfileScreen | **DONE** — FlowRow with earned/unearned states |
| GoalState banners on HomeScreen | **DONE** — Achieved + Maintenance banners |
| Settings screen | **DONE** (in `.worktrees/phase3/`, not yet merged — PR #30 open) |
| Accessibility pass | **DONE** — merged semantics, TalkBack labels, loading descs, chart desc |
| WorkManager daily reminder | **DONE** — `WeightReminderWorker`, POST_NOTIFICATIONS guarded |
| Crashlytics scaffold | **DONE** — deps commented, activation checklist in WeightFlowApp |
| 185+ unit tests GREEN | **DONE** — 187 after ProfileViewModelTest expanded |
| Lint clean | **DONE** |
| assembleDebug successful | **DONE** |
| PR #30 created | **DONE** — `feature/phase3-polish` → `main` (Settings + snackbar not yet in main) |
| ShellScreen UI overhaul | **DONE** (session 2026-04-18-002) |
| HomeScreen UI overhaul | **DONE** (session 2026-04-18-002) |
| TrendsScreen UI overhaul | **DONE** (session 2026-04-18-002) |
| HistoryScreen UI overhaul | **DONE** (session 2026-04-18-003) |
| ProfileScreen UI overhaul | **DONE** (session 2026-04-18-003) — BadgeObserver + WeightRepository wired |
| LogEntry sheet UI overhaul | **DONE** (session 2026-04-18-004) — BasicTextField Bebas Neue 72sp, +/- buttons, unit indicator, accent Save |
| OnboardingScreen UI overhaul | **DONE** (session 2026-04-18-004) — branding header, step dots, styled step cards |

## GitHub Repo
- URL: `https://github.com/VaibhavAher100/WeightFlow` (private)
- Rename before launch (name is temporary)
- PRD: issue #1 | Slice issues: #2-#23 | Architecture RFCs: #24-#29
- **PR #30 open:** Phase 3 polish — awaiting manual device testing before merge

## Git — IMPORTANT
- **No Claude co-authorship ever.** No `Co-Authored-By: Claude` or Anthropic traces in any commit.
- Full history was rewritten 2026-04-17 — all prior Claude co-author lines removed.
- Always write commit messages manually. No co-author trailers.

## Key Product Decisions (Locked, from grill-me 2026-04-12)

| Decision | Choice |
|----------|--------|
| Nav | Home + Trends + History + Profile + FAB (no Log tab) |
| Log sheet | Weight + date only (no note field) |
| Home layout | Always-full with motivating empty states |
| Onboarding | 4 screens: age gate -> unit -> weight -> goal |
| Charts | Line + Bar + Area + Candlestick |
| Time filters | 7D / 30D / 3M / 6M / 1Y / All |
| Goals | Single active goal -> maintenance mode -> optional new goal chain |
| Badges | 12 (Zero-inspired, all 12 listed in PRD) |
| Pro price | 4.99 EUR one-time, Play Billing localised |
| Revenue priority | Donations first, Pro second |
| CSV import | WeightFit + Happy Scale + Apple Health + generic |
| Themes | 8 palettes (Lime default) |
| RTL | RTL-safe from Phase 2 (start/end semantics) |
| Repo | Private now, rename before launch |
| Weight storage | Always kg internally, unit conversion is display-only |
| Settings | Accessed from Profile — not a 6th tab |

## Architecture RFCs (all implemented)

| RFC | Issue | Decision |
|-----|-------|----------|
| BadgeObserver | #24 | Reactive (combine Room Flows), not imperative on insert — **IMPLEMENTED** |
| CsvImporter | #25 | Single entry point, CsvFormat enum in ParseResult — **IMPLEMENTED** |
| GoalStateMachine | #26 | Explicit sealed FSM with transition functions — **IMPLEMENTED** |
| StoredWeight + HomeUiStateMapper | #27 | Value class for Room + single conversion point in ViewModel — **IMPLEMENTED** |
| SortedEntries + named Repository methods | #28 | Type-safe ordering contract, delete getAllEntries() — **IMPLEMENTED** |
| HomeDataAggregator | #29 | ViewModel gets one dependency instead of five — **IMPLEMENTED** |

## Phase 4 — IN PROGRESS (session 2026-04-26-001)

| Item | Status |
|------|--------|
| COPPA age gate — AgeDeclined snackbar | **DONE** (OnboardingScreen Scaffold + snackbar) |
| POST_NOTIFICATIONS runtime request | **DONE** (MainActivity) |
| R8/ProGuard rules | **DONE** (proguard-rules.pro — Room, DataStore, Coroutines, WorkManager, Vico, domain/data) |
| isMinifyEnabled=true + isShrinkResources=true | **DONE** (release build type) |
| Signing config from local.properties | **DONE** (falls back to debug if KEYSTORE_PATH not set) |
| Donation links in ProfileScreen | **DONE** (Ko-fi, Liberapay, GitHub Sponsors — update URLs before launch) |
| COPPA/DPDP: year-of-birth picker (18+ threshold) | **DONE** (OnboardingViewModel + OnboardingScreen) |
| GDPR Art. 17: Delete all data button | **DONE** (ProfileScreen + ProfileViewModel + DAOs) |
| Compliance audit (GDPR/COPPA/DPDP) | **DONE** — 9 blockers found, 4 fixed in code, 5 filed as issues #31-34 |
| Privacy policy live URL | **TODO** — needs GitHub Pages setup |
| Back up Android keystore | **TODO** — user action, CRITICAL before first release build |
| Firebase Crashlytics end-to-end | **TODO** — blocked on google-services.json |
| Play Store listing + ASO | **TODO** |
| AAB build + upload | **TODO** |

## Build Configuration (AGP 9.x specific)

- `gradle/libs.versions.toml`: Kotlin 2.2.10, KSP 2.3.2, Room 2.7.0, DataStore 1.1.1, NavCompose 2.8.9, Vico 1.13.1, Compose BOM 2025.04.01, mockk 1.13.12, turbine 1.1.0, WorkManager 2.10.1
- Firebase versions in `[versions]` block + library aliases commented — activate after adding `google-services.json`
- `gradle.properties`: `android.disallowKotlinSourceSets=false` (KSP + AGP 9.x compatibility)
- `app/build.gradle.kts` plugins: `android-application` + `kotlin-compose` + `ksp` (NO `kotlin-android`)
- XML theme: `Theme.AppCompat.DayNight.NoActionBar`
- Room schema export: `app/schemas/` — currently at **schema v2** (`achievedAtEpochDay` on `user_profile`)
- `isMinifyEnabled = true` + `isShrinkResources = true` in release — enabled Phase 4 (2026-04-26); R8 release build verified GREEN
- Signing config reads from `local.properties` (KEYSTORE_PATH / KEYSTORE_PASSWORD / KEY_ALIAS / KEY_PASSWORD); falls back to debug signing if not set
- `POST_NOTIFICATIONS` permission in `AndroidManifest.xml` (required for WorkManager notification on API 33+)

## Backup / Privacy Configuration

- `app/src/main/res/xml/backup_rules.xml` — API 30 and below: all DB + DataStore excluded from cloud backup
- `app/src/main/res/xml/data_extraction_rules.xml` — API 31+: cloud backup excluded; device-transfer enabled
- `android:allowBackup="true"` in manifest — required to reference `fullBackupContent`

## UI Layer (main branch — post overhaul sessions)

All ViewModels and Screens in `app/src/main/java/com/weightflow/ui/`:

| Package | Files | Overhaul Status |
|---------|-------|-----------------|
| `ui/theme/` | `Color.kt` (+WFTokens), `Type.kt`, `Theme.kt` | DONE |
| `ui/navigation/` | `Screen.kt`, `NavGraph.kt` (+BadgeObserver/WeightRepo to ProfileVM) | DONE |
| `ui/shell/` | `ShellScreen.kt` (styled nav bar) | DONE |
| `ui/home/` | `HomeUiState.kt`, `HomeUiStateMapper.kt`, `HomeViewModel.kt`, `HomeScreen.kt` (full overhaul) | DONE |
| `ui/logentry/` | `LogEntryUiState.kt`, `LogEntryViewModel.kt`, `LogEntryScreen.kt` | PENDING overhaul |
| `ui/trends/` | `TrendsUiState.kt`, `TrendsViewModel.kt`, `TrendsScreen.kt` (full overhaul) | DONE |
| `ui/history/` | `HistoryUiState.kt` (+delta fields), `HistoryViewModel.kt` (+delta compute), `HistoryScreen.kt` (full overhaul) | DONE |
| `ui/profile/` | `ProfileUiState.kt` (+8 fields), `ProfileViewModel.kt` (+BadgeObserver/WeightRepo), `ProfileScreen.kt` (full overhaul) | DONE |
| `ui/onboarding/` | `OnboardingUiState.kt` (birthYearInput), `OnboardingViewModel.kt` (year picker, 18+), `OnboardingScreen.kt` (year TextField + Scaffold snackbar) | DONE (2026-04-26) |
| `ui/settings/` | `SettingsUiState.kt`, `SettingsViewModel.kt`, `SettingsScreen.kt` — merged via PR #30 | IN MAIN (weight unit + theme only; CSV export pending #32) |

**Note:** `SettingsViewModelTest.kt` (3 tests) was in `.worktrees/phase3/` but merged via PR #30 — now in main.

## Worker Layer (Phase 3 new)

| Package | Files |
|---------|-------|
| `worker/` | `WeightReminderWorker.kt` — daily notification, `ExistingPeriodicWorkPolicy.KEEP` |

## Domain Layer (Complete)

All files in `app/src/main/java/com/weightflow/domain/` — unchanged from Phase 2.

## Data Layer (Phase 4 additions)

All files in `app/src/main/java/com/weightflow/data/`. Phase 4 additions (2026-04-26):
- `WeightEntryDao` — `deleteAll(): Int` added
- `UserProfileDao` — `deleteAll(): Int` added
- `WeightRepository` — `deleteAllEntries(): Int` added
- `UserProfileRepository` — `deleteProfile(): Int` added
- `UserPrefsDataStore` — `clearAllPreferences()` added

## Tests (**196 unit tests — all GREEN, BUILD SUCCESSFUL 2026-04-26**)

**Unit tests** — `app/src/test/java/com/weightflow/`:

| File | Tests | Status |
|------|-------|--------|
| `domain/WeightConverterTest.kt` | 13 | GREEN |
| `domain/GoalProgressCalculatorTest.kt` | 15 | GREEN |
| `domain/BadgeEngineTest.kt` | 24 | GREEN |
| `domain/BadgeObserverTest.kt` | 6 | GREEN |
| `domain/CsvParserTest.kt` | 12 | GREEN |
| `domain/CsvImporterTest.kt` | 5 | GREEN |
| `domain/CsvExporterTest.kt` | 9 | GREEN |
| `domain/GoalStateMachineTest.kt` | 14 | GREEN |
| `ui/home/HomeViewModelTest.kt` | 18 | GREEN |
| `ui/logentry/LogEntryViewModelTest.kt` | 20 | GREEN |
| `ui/trends/TrendsViewModelTest.kt` | 11 | GREEN |
| `ui/history/HistoryViewModelTest.kt` | 4 | GREEN |
| `ui/profile/ProfileViewModelTest.kt` | 12 | GREEN (+3 deleteAllData GDPR Art. 17) |
| `ui/onboarding/OnboardingViewModelTest.kt` | 23 | GREEN (+5 year-of-birth picker tests) |
| `ui/home/HomeUiStateMapperTest.kt` | 7 | GREEN |
| **TOTAL** | **196** | **0 failures** |

**Note:** `SettingsViewModelTest.kt` (3 tests) merged via PR #30 — now in main (not counted above since already accounted in +196).

**Instrumented tests** — `app/src/androidTest/java/com/weightflow/data/` (need device/emulator):

| File | Tests | Status |
|------|-------|--------|
| `WeightEntryDaoTest.kt` | 7 | Compile-verified |
| `UserProfileDaoTest.kt` | 5 | Compile-verified |
| `UserPrefsDataStoreTest.kt` | 7 | Compile-verified |
| `WeightRepositoryTest.kt` | 11 | Compile-verified |
| `UserProfileRepositoryTest.kt` | 6 | Compile-verified |

## ViewModel Test Patterns (Locked)

- **Dispatcher:** `StandardTestDispatcher()` + `Dispatchers.setMain` in `@Before`
- **stateIn initial skip:** `awaitRealState()` — consumes the Loading item before asserting real state
- **Settings-style VMs (no Loading state):** use `expectMostRecentItem()` after `advanceUntilIdle()` — StateFlow deduplicates, so no second emission when initial == default
- **Fake aggregator:** anonymous object implementing interface — no mockk needed for pure Flow tests
- **mockk for repos:** `every { repo.flowProp } returns MutableStateFlow(...)` (not `coEvery`)
- **Synchronous VM actions** (`onWeightInput`, `onNextStep`): no `advanceUntilIdle()` needed
- **Coroutine-launching actions** (`onSave`, `onComplete`): call `advanceUntilIdle()` after
- **4-flow combine VMs (ProfileViewModel):** mock all 4 flows in `@Before`; test each field independently

## Guardrails (Active)

Three hookify rules in `.claude/`:
| Rule | Event | Purpose |
|------|-------|---------|
| `hookify.tdd-production-guard.local.md` | file | Warns before any `app/src/main/java/**/*.kt` write — confirms failing test exists first |
| `hookify.completion-verification.local.md` | stop | Checklist on session stop — requires `BUILD SUCCESSFUL` evidence |
| `hookify.session-start-plan-check.local.md` | prompt | Triggers on "continue/next step" — requires reading `_state.md` + TDD plan |

## Environment
- **Open from:** `C:\Users\vaibh\Desktop\102\WeightFlow\`
- **Agents:** 35 in `.claude/agents/`
- **Skills:** 18 total in `.claude/skills/`
- **Plugins (active):** kotlin-lsp | swift-lsp | feature-dev | code-review | pr-review-toolkit | commit-commands | code-simplifier | hookify | claude-md-management | security-guidance | session-report | frontend-design
- **Codex CLI:** `codex` v0.118.0 installed globally
- **CLI Priority:** `gh` > github MCP | `codex` > agent for isolation | `npx skills` for skill mgmt | `./gradlew` for builds
- **CI:** `.github/workflows/android.yml` ready

## Open Items

- [ ] #31 — Privacy policy: write + publish to GitHub Pages (Play Store blocker)
- [ ] #32 — CSV export wired to Settings UI (GDPR Art. 20 portability)
- [ ] #33 — Medical disclaimer + safe-messaging banner (Play health policy)
- [ ] #34 — Network Security Config (block cleartext traffic in release)
- [ ] Back up Android keystore to 3 locations — CRITICAL before first release build
- [ ] Wire Firebase Crashlytics end-to-end once `google-services.json` available
- [ ] Update Ko-fi + Liberapay URLs in ProfileScreen once accounts created
- [ ] Complete Play Data Safety form in Play Console
- [ ] Play Store listing + ASO (android-aso skill)
- [ ] AAB build + Play Store internal track upload (android-playstore-setup skill)

## Critical Before First Build
- [ ] Back up Android keystore to 3 locations (CRITICAL)
- [ ] Add Firebase Crashlytics properly — needs `google-services.json` (checklist in WeightFlowApp.kt)
- [ ] Age gate (13+) in onboarding — ViewModel gate implemented ✓
- [ ] Privacy policy live URL before Phase 4 (GitHub Pages)

## Phase 4 Reminders (don't forget)
- Enable R8/ProGuard (`isMinifyEnabled = true`) — write Room + Kotlin rules first
- AAB not APK for Play Store submission
- APK budget: keep final download under 15MB

## UI Overhaul Status (2026-04-18, screen-by-screen)

| Screen | Status |
|--------|--------|
| ShellScreen (nav bar) | DONE |
| HomeScreen | DONE |
| TrendsScreen | DONE |
| HistoryScreen | DONE |
| ProfileScreen | DONE |
| LogEntry sheet | **DONE** (session 2026-04-18-004) |
| OnboardingScreen | **DONE** (session 2026-04-18-004) |

## Next Session Should

1. **#31** — Write privacy policy + publish to GitHub Pages (`privacy-policy-malik-taiar` skill)
2. **#32** — Wire CSV export/import to Settings UI (SAF + ActivityResultContracts)
3. **#33** — Add medical disclaimer to HomeScreen footer + safe-messaging banner on GoalStep
4. **#34** — Add Network Security Config XML
5. Generate Android keystore + back up to 3 locations (user action — CRITICAL)
6. Start Play Store listing + ASO (`android-aso` skill)

## Vico Note (for future sessions)
Vico 1.13.1 API — context7 returns v3 docs which are WRONG. Use these imports:
- `com.patrykandpatrick.vico.compose.chart.Chart`
- `com.patrykandpatrick.vico.compose.chart.line.lineChart`
- `com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis`
- `com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis`
- `com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer`
- `com.patrykandpatrick.vico.core.entry.FloatEntry`

## HistoryScreen Design Notes (locked 2026-04-18)
- Layout: flat list with `border-bottom` dividers — NOT cards per entry
- Date column: fixed 38dp, Bebas Neue day number (24sp) + abbreviated day name (9sp uppercase)
- Weight: display font (Bebas Neue) 22sp full display string (e.g. "82.4 kg")
- Today row: `accentDim` background highlight
- Delta chip: 6dp radius (rectangular), green/red/gray for down/up/same
- Month sticky headers: 10sp bold, letter-spacing 1.5sp, Text3 color
- Decorative search bar at top (non-functional, matches mockup aesthetic)

## ProfileScreen Design Notes (locked 2026-04-18)
- Avatar: 64dp, 20dp corners, linear gradient (accent → accent 30%), initials in onPrimary
- Streak row: 🔥 emoji + Bebas Neue number in Success color + "day streak" label
- Goal showcase: accent border, 3-column trio (start/current/goal) + progress bar + summary label
- Body stats 2×2 grid: Height / BMI / Logged / Streak — BMI cell uses accent border when available
- Badge row: horizontal LazyRow, all 12 badges, earned = accentDim bg + accentBorder, locked = Card bg
- ProfileViewModel now requires 4 deps: UserProfileRepository, UserPrefsDataStore, WeightRepository, BadgeObserver
