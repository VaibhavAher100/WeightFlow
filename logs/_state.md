---
last_session: 2026-04-13-001
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
- Test: JUnit 4.13.2 + mockk 1.13.12 + turbine 1.1.0 + kotlinx-coroutines-test 1.8.1
- CI: GitHub Actions (tests + lint + build on every push)

## Phase Status

| Phase | Description | Status |
|-------|-------------|--------|
| 0 | Infrastructure (environment, agents, skills, conventions, product strategy, PRD, issues) | Complete |
| 1 | Foundation (Android project + Room + DataStore + NavGraph shell) | In progress |
| 2 | All 6 screens + ViewModels + Vico | Plan ready, blocked on Phase 1 |
| 3 | Onboarding + polish + empty/error states | Needs brainstorm |
| 4 | Play Store launch (privacy policy, Crashlytics, signed build, ASO) | Needs planning |
| 5 | Firebase sync + AdMob + iOS via KMP | Needs planning |

## Phase 1 Progress

| TDD Step | Module | Status |
|----------|--------|--------|
| Step 2 | WeightConverter + WeightUnit | GREEN (13 unit tests) |
| Step 3 | GoalProgressCalculator | GREEN (15 unit tests) |
| Step 4 | Room schema + DAOs | GREEN (20 instrumented tests — compile verified) |
| Step 5 | DataStore preferences | GREEN (7 instrumented tests — compile verified) |
| Step 6 | Repository layer | GREEN (17 instrumented tests — compile verified) |
| Step 7 | BadgeEngine | GREEN (24 unit tests) |
| Step 8 | CSV parsers (all 4) | GREEN (12 unit tests) |
| Step 9 | CsvExporter | GREEN (9 unit tests) |
| Step 10 | Theme system | Not started |
| Step 11 | NavGraph shell | Not started |

**Current test count: 74 unit tests passing (BUILD SUCCESSFUL verified 2026-04-13)**
**Instrumented tests: 37 compile-verified (Steps 4+5+6) — need device/emulator to run**

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
| StoredWeight + HomeUiStateMapper | #27 | Value class for Room + single conversion point in ViewModel |
| SortedEntries + named Repository methods | #28 | Type-safe ordering contract, delete getAllEntries() — IMPLEMENTED in WeightEntryDao + WeightRepository |
| HomeDataAggregator | #29 | ViewModel gets one dependency instead of five |

## Build Configuration (AGP 9.x specific)

- `gradle/libs.versions.toml`: Kotlin 2.1.20, KSP 2.1.20-1.0.31, Room 2.7.0, DataStore 1.1.1, NavCompose 2.8.9, Vico 1.13.1, Compose BOM 2025.04.01, mockk 1.13.12, turbine 1.1.0
- `gradle.properties`: `android.disallowKotlinSourceSets=false` (KSP + AGP 9.x compatibility)
- `app/build.gradle.kts` plugins: `android-application` + `kotlin-compose` + `ksp` (NO `kotlin-android`)
- XML theme: `Theme.AppCompat.DayNight.NoActionBar`
- Room schema export: `app/schemas/` (via `ksp { arg("room.schemaLocation", ...) }`)

## Implementation Plans

| Plan | File | Status |
|------|------|--------|
| Foundation (7 tasks) | `docs/plans/2026-04-11-weightflow-foundation.md` | In progress |
| All Screens (9 tasks) | `docs/plans/2026-04-11-weightflow-screens.md` | Ready (after Phase 1) |
| TDD execution order | `docs/plans/2026-04-12-tdd-order.md` | In progress |
| Phase 3-5 | TBD | Not yet written |

## Data Layer (Steps 4+5+6 complete)

All files in `app/src/main/java/com/weightflow/data/` (flat package — intentional, decided in sessions 006-007):

| File | Contents |
|------|----------|
| `WeightEntryEntity.kt` | `@Entity(tableName = "weight_entries")` — id, timestamp, weightKg, note |
| `WeightEntryDao.kt` | `insert(): Long`, `delete(): Int`, `getById()`, `getEntriesNewestFirst()`, `getEntriesOldestFirst()`, `getEntriesBetween()` (RFC #28 naming) |
| `UserProfileEntity.kt` | `@Entity(tableName = "user_profile")` — id=1 always, all profile fields, targetDate as epochDay Long |
| `UserProfileDao.kt` | `upsert(): Long`, `getProfile(): Flow<UserProfileEntity?>` |
| `AppDatabase.kt` | `@Database(version=1, exportSchema=true)` + `getInstance(Context)` singleton |
| `UserPrefsDataStore.kt` | Injected `DataStore<Preferences>` — weightUnit/themePalette/onboardingComplete Flows + setters |
| `WeightRepository.kt` | Wraps WeightEntryDao; maps Entity↔WeightEntry; RFC #28 named ordering methods |
| `UserProfileRepository.kt` | Wraps UserProfileDao; maps Entity↔UserProfile; LocalDate↔Long conversion (RFC #27 boundary) |

Instrumented tests in `app/src/androidTest/java/com/weightflow/data/`:
- `WeightEntryDaoTest.kt` — 7 tests
- `UserProfileDaoTest.kt` — 5 tests
- `UserPrefsDataStoreTest.kt` — 7 tests (custom TestDataStore context)
- `WeightRepositoryTest.kt` — 11 tests
- `UserProfileRepositoryTest.kt` — 6 tests

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

## Application Layer

| File | Contents |
|------|----------|
| `WeightFlowApp.kt` | `Application` class — lazy singletons: `database`, `weightRepository`, `userProfileRepository`, `userPrefsDataStore` (manual DI root) |

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

- [ ] TDD Step 10: Theme system — `ui/theme/Color.kt` (8 palettes), `Theme.kt` (dynamic MaterialTheme), `Type.kt` (Bebas Neue + Outfit) — no unit tests, manual verification on device
- [ ] TDD Step 11: NavGraph shell — `Screen.kt`, `NavGraph.kt`, `ShellScreen.kt`, `MainActivity.kt` — 4-tab bottom nav + FAB — no unit tests, manual verification
- [ ] Wire compliance-auditor agent check before first device build (GDPR checklist)

## Critical Before First Build
- [ ] Back up Android keystore to 3 locations (CRITICAL)
- [ ] Add Firebase Crashlytics in Phase 1 (not later)
- [ ] Age gate (13+) in onboarding (COPPA)
- [ ] Privacy policy live URL before Phase 4 (GitHub Pages)

## Next Session Should
1. TDD Step 10: Theme system
   - `app/src/main/java/com/weightflow/ui/theme/Color.kt` — 8 accent palettes + neutrals (Lime default #C8FF00, warm dark #0F0E0B)
   - `app/src/main/java/com/weightflow/ui/theme/Type.kt` — Bebas Neue (numbers) + Outfit (UI text)
   - `app/src/main/java/com/weightflow/ui/theme/Theme.kt` — dynamic MaterialTheme wrapper that reads themePalette from DataStore
   - Verification: manual build + visual check on device/emulator
2. TDD Step 11: NavGraph shell
   - `app/src/main/java/com/weightflow/ui/navigation/Screen.kt` — sealed class routes
   - `app/src/main/java/com/weightflow/ui/navigation/NavGraph.kt` — NavHost with 4-tab bottom nav
   - `app/src/main/java/com/weightflow/ui/shell/ShellScreen.kt` — Scaffold + BottomNavigation stub
   - `app/src/main/java/com/weightflow/MainActivity.kt` — single activity hosting NavHost
   - Verification: app launches, tabs switch, FAB visible
3. After Step 11: Phase 1 is complete → start Phase 2 (screens + ViewModels)
