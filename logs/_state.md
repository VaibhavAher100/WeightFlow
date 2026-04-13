---
last_session: 2026-04-13-003
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
- Kotlin 2.1.20 + Jetpack Compose BOM 2025.04.01
- AGP 9.1.0 + Gradle 9.3.1 (AGP 9.x has built-in Kotlin — no kotlin-android plugin)
- Room **2.7.0** (upgraded from 2.6.1 — not KSP2-compatible) | KSP 2.1.20-1.0.31
- DataStore 1.1.1 | Navigation Compose 2.8.9
- Vico 1.13.1 charts | Manual DI (no Hilt) | StateFlow
- Google Fonts: `ui-text-google-fonts` (Compose BOM) — Bebas Neue + Outfit via GMS provider
- Test: JUnit 4.13.2 + mockk 1.13.12 + turbine 1.1.0 + kotlinx-coroutines-test 1.8.1
- CI: GitHub Actions (tests + lint + build on every push)

## Phase Status

| Phase | Description | Status |
|-------|-------------|--------|
| 0 | Infrastructure (environment, agents, skills, conventions, product strategy, PRD, issues) | Complete |
| 1 | Foundation (Android project + Room + DataStore + NavGraph shell) | Complete |
| 2 | All 6 screens + ViewModels + Vico | **In progress — Vico + Onboarding screens remaining** |
| 3 | Onboarding + polish + empty/error states | Needs brainstorm |
| 4 | Play Store launch (privacy policy, Crashlytics, signed build, ASO) | Needs planning |
| 5 | Firebase sync + AdMob + iOS via KMP | Needs planning |

## Phase 2 Progress

| Item | Status |
|------|--------|
| HomeViewModel + HomeScreen | **DONE** (11 tests) |
| LogEntryViewModel + LogEntrySheet | **DONE** (18 tests) |
| TrendsViewModel + TrendsScreen | **DONE** (11 tests) — Vico chart wiring pending |
| HistoryViewModel + HistoryScreen | **DONE** (4 tests) |
| ProfileViewModel + ProfileScreen | **DONE** (5 tests) |
| OnboardingViewModel | **DONE** (17 tests) |
| OnboardingScreen composables (4 steps) | **TODO** — next session |
| Vico 1.13.1 chart in TrendsScreen | **TODO** — next session |
| Wire onboarding into MainActivity | **TODO** — check `onboardingComplete` pref |

## GitHub Repo
- URL: `https://github.com/VaibhavAher100/WeightFlow` (private)
- Rename before launch (name is temporary)
- PRD: issue #1
- Slice issues: #2-#23
- Architecture RFCs: #24-#29

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

## Architecture RFCs (implement during Phase 1-2)

| RFC | Issue | Decision |
|-----|-------|----------|
| BadgeObserver | #24 | Reactive (combine Room Flows), not imperative on insert |
| CsvImporter | #25 | Single entry point, CsvFormat enum in ParseResult |
| GoalStateMachine | #26 | Explicit sealed FSM with transition functions |
| StoredWeight + HomeUiStateMapper | #27 | Value class for Room + single conversion point in ViewModel — **IMPLEMENTED** |
| SortedEntries + named Repository methods | #28 | Type-safe ordering contract, delete getAllEntries() — **IMPLEMENTED** in WeightEntryDao + WeightRepository |
| HomeDataAggregator | #29 | ViewModel gets one dependency instead of five — **IMPLEMENTED** |

## Build Configuration (AGP 9.x specific)

- `gradle/libs.versions.toml`: Kotlin 2.1.20, KSP 2.1.20-1.0.31, Room 2.7.0, DataStore 1.1.1, NavCompose 2.8.9, Vico 1.13.1, Compose BOM 2025.04.01, mockk 1.13.12, turbine 1.1.0, **ui-text-google-fonts (BOM)**
- `gradle.properties`: `android.disallowKotlinSourceSets=false` (KSP + AGP 9.x compatibility)
- `app/build.gradle.kts` plugins: `android-application` + `kotlin-compose` + `ksp` (NO `kotlin-android`)
- XML theme: `Theme.AppCompat.DayNight.NoActionBar`
- Room schema export: `app/schemas/` (via `ksp { arg("room.schemaLocation", ...) }`)
- **font_certs.xml**: GMS downloadable fonts provider certs — must be in `res/values/` (not auto-bundled by `ui-text-google-fonts` in this build config)

## Implementation Plans

| Plan | File | Status |
|------|------|--------|
| Foundation (7 tasks) | `docs/plans/2026-04-11-weightflow-foundation.md` | Complete |
| All Screens (9 tasks) | `docs/plans/2026-04-11-weightflow-screens.md` | **In progress** |
| TDD execution order | `docs/plans/2026-04-12-tdd-order.md` | Steps 2-11 + all 6 Phase 2 ViewModels done |
| Phase 3-5 | TBD | Not yet written |

## UI Layer (Phase 2 complete — screens wired)

All ViewModels and Screens in `app/src/main/java/com/weightflow/ui/`:

| Package | Files |
|---------|-------|
| `ui/theme/` | `Color.kt`, `Type.kt`, `Theme.kt` |
| `ui/navigation/` | `Screen.kt`, `NavGraph.kt` (all 4 tabs wired with real VMs via `vmFactory`) |
| `ui/shell/` | `ShellScreen.kt` (ModalBottomSheet for LogEntry) |
| `ui/home/` | `HomeUiState.kt`, `HomeUiStateMapper.kt`, `HomeViewModel.kt`, `HomeScreen.kt` |
| `ui/logentry/` | `LogEntryUiState.kt`, `LogEntryViewModel.kt`, `LogEntryScreen.kt` |
| `ui/trends/` | `TrendsUiState.kt`, `TrendsViewModel.kt`, `TrendsScreen.kt` (Vico TODO) |
| `ui/history/` | `HistoryUiState.kt`, `HistoryViewModel.kt`, `HistoryScreen.kt` |
| `ui/profile/` | `ProfileUiState.kt`, `ProfileViewModel.kt`, `ProfileScreen.kt` |
| `ui/onboarding/` | `OnboardingUiState.kt`, `OnboardingViewModel.kt` (Screen TODO) |

**LogEntry pattern:** FAB → `showLogEntry = true` in ShellScreen → `ModalBottomSheet` overlays. Not a NavGraph destination.

## Domain Layer (Complete)

All files in `app/src/main/java/com/weightflow/domain/`:

| File | Contents |
|------|----------|
| `WeightUnit.kt` | `enum class WeightUnit { KG, LBS, ST }` |
| `WeightConverter.kt` | `object WeightConverter` + `data class StonesResult` |
| `WeightEntry.kt` | `data class WeightEntry` (pure domain) |
| `UserProfile.kt` | `data class UserProfile` (pure domain) |
| `GoalProgressCalculator.kt` | `object GoalProgressCalculator` + `DriftDirection` enum + `GoalProgress` data class |
| `BadgeEngine.kt` | `object BadgeEngine` + `enum class Badge` (12 variants) |
| `ParseResult.kt` | `sealed class ParseResult` (Success + Error) |
| `CsvParsers.kt` | `WeightFitParser`, `HappyScaleParser`, `AppleHealthParser`, `GenericCsvParser` |
| `CsvExporter.kt` | `object CsvExporter` |
| `StoredWeight.kt` | `@JvmInline value class StoredWeight(val kg: Double)` — RFC #27 |
| `HomeData.kt` | `data class HomeData(entries, profile, unit)` — RFC #29 aggregate |
| `HomeDataAggregator.kt` | interface + `HomeDataAggregatorImpl` using `combine()` — RFC #29 |

## Data Layer (Complete)

All files in `app/src/main/java/com/weightflow/data/` (flat package):

| File | Contents |
|------|----------|
| `WeightEntryEntity.kt` | `@Entity(tableName = "weight_entries")` |
| `WeightEntryDao.kt` | `insert():Long`, `delete():Int`, `getById()`, `getEntriesNewestFirst()`, `getEntriesOldestFirst()`, `getEntriesBetween()` |
| `UserProfileEntity.kt` | `@Entity(tableName = "user_profile")` — id=1 always |
| `UserProfileDao.kt` | `upsert():Long`, `getProfile():Flow<UserProfileEntity?>` |
| `AppDatabase.kt` | `@Database(version=1, exportSchema=true)` + singleton |
| `UserPrefsDataStore.kt` | weightUnit/themePalette/onboardingComplete Flows + setters |
| `WeightRepository.kt` | Wraps WeightEntryDao; RFC #28 named ordering methods |
| `UserProfileRepository.kt` | Wraps UserProfileDao; LocalDate↔Long conversion |

## Application Layer

| File | Contents |
|------|----------|
| `WeightFlowApp.kt` | Manual DI root — lazy: `database`, `weightRepository`, `userProfileRepository`, `userPrefsDataStore`, `homeDataAggregator` |

## Tests (**141 unit tests — all GREEN, BUILD SUCCESSFUL 2026-04-13**)

**Unit tests** — `app/src/test/java/com/weightflow/`:

| File | Tests | Status |
|------|-------|--------|
| `domain/WeightConverterTest.kt` | 13 | GREEN |
| `domain/GoalProgressCalculatorTest.kt` | 15 | GREEN |
| `domain/BadgeEngineTest.kt` | 24 | GREEN |
| `domain/CsvParserTest.kt` | 12 | GREEN |
| `domain/CsvExporterTest.kt` | 9 | GREEN |
| `ui/home/HomeViewModelTest.kt` | 11 | GREEN |
| `ui/logentry/LogEntryViewModelTest.kt` | 18 | GREEN |
| `ui/trends/TrendsViewModelTest.kt` | 11 | GREEN |
| `ui/history/HistoryViewModelTest.kt` | 4 | GREEN |
| `ui/profile/ProfileViewModelTest.kt` | 5 | GREEN |
| `ui/onboarding/OnboardingViewModelTest.kt` | 17 | GREEN |
| **TOTAL** | **141** | **0 failures** |

**Instrumented tests** — `app/src/androidTest/java/com/weightflow/data/` (need device/emulator):

| File | Tests | Status |
|------|-------|--------|
| `WeightEntryDaoTest.kt` | 7 | Compile-verified |
| `UserProfileDaoTest.kt` | 5 | Compile-verified |
| `UserPrefsDataStoreTest.kt` | 7 | Compile-verified |
| `WeightRepositoryTest.kt` | 11 | Compile-verified |
| `UserProfileRepositoryTest.kt` | 6 | Compile-verified |

## Guardrails (Active)

Three hookify rules in `.claude/`:
| Rule | Event | Purpose |
|------|-------|---------|
| `hookify.tdd-production-guard.local.md` | file | Warns before any `app/src/main/java/**/*.kt` write — confirms failing test exists first |
| `hookify.completion-verification.local.md` | stop | Checklist on session stop — requires `BUILD SUCCESSFUL` evidence |
| `hookify.session-start-plan-check.local.md` | prompt | Triggers on "continue/next step" — requires reading `_state.md` + TDD plan |

## ViewModel Test Patterns (Locked — reuse in Phase 2 remaining + Phase 3)

- **Dispatcher:** `StandardTestDispatcher()` + `Dispatchers.setMain` in `@Before`
- **stateIn initial skip:** `awaitRealState()` — consumes the Loading item before asserting real state
- **Fake aggregator:** anonymous object implementing interface — no mockk needed for pure Flow tests
- **mockk for repos:** `every { repo.flowProp } returns MutableStateFlow(...)` (not `coEvery`)
- **Synchronous VM actions** (`onWeightInput`, `onNextStep`): no `advanceUntilIdle()` needed; coroutine-launching actions (`onSave`, `onComplete`) need it

## Environment
- **Open from:** `C:\Users\vaibh\Desktop\102\WeightFlow\`
- **Agents:** 35 in `.claude/agents/`
- **Skills:** 18 total in `.claude/skills/`
- **Plugins (active):** kotlin-lsp | swift-lsp | feature-dev | code-review | pr-review-toolkit | commit-commands | code-simplifier | hookify | claude-md-management | security-guidance | session-report | frontend-design
- **Codex CLI:** `codex` v0.118.0 installed globally
- **CLI Priority:** `gh` > github MCP | `codex` > agent for isolation | `npx skills` for skill mgmt | `./gradlew` for builds
- **CI:** `.github/workflows/android.yml` ready

## Open Items

- [ ] Wire Vico 1.13.1 line chart into TrendsScreen (replace placeholder ChartView)
- [ ] Write OnboardingScreen composables (4 steps: AgeGate → Unit → CurrentWeight → Goal)
- [ ] Wire onboarding into MainActivity (check `onboardingComplete` pref on launch)
- [ ] Wire compliance-auditor agent check before first device build (GDPR checklist)
- [ ] Back up Android keystore to 3 locations (CRITICAL — before first release build)
- [ ] Add Firebase Crashlytics (carry forward — add early Phase 2 remainder / Phase 3)
- [ ] Age gate (13+) in onboarding (COPPA) — OnboardingViewModel done, needs Screen

## Critical Before First Build
- [ ] Back up Android keystore to 3 locations (CRITICAL)
- [ ] Add Firebase Crashlytics in early Phase 3 (not later)
- [ ] Age gate (13+) in onboarding screen (COPPA) — ViewModel gate implemented
- [ ] Privacy policy live URL before Phase 4 (GitHub Pages)

## Next Session Should

**Phase 2 — remaining items:**

1. **Vico chart in TrendsScreen** (Issue #16 Vico portion)
   - Wire `Vico 1.13.1` line chart into `TrendsScreen.kt`
   - Use `ChartPoint.displayValue` + `ChartPoint.timestamp` as x/y
   - Reference: `gradle/libs.versions.toml` has `vico = "1.13.1"` + `compose-m3` dependency

2. **OnboardingScreen composables** (Issue #13)
   - 4-step pager: AgeGate → Unit → CurrentWeight → Goal
   - Age gate MUST show "I confirm I am 13 or older" checkbox (COPPA)
   - Wire `OnboardingViewModel` already done

3. **MainActivity onboarding gate**
   - Collect `userPrefsDataStore.onboardingComplete`
   - Show `OnboardingScreen` if false, else `ShellScreen`
