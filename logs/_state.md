---
last_session: 2026-05-09-001
status: active
environment: isolated (WeightFlow/ only)
---

# WeightFlow Project State

_Single source of truth. Updated every session via `/wrap`._
_Always open `WeightFlow/` in Claude Code, never the root `102/`._

---

## App Identity
- **Name:** WeightFlow | **Package:** `com.weightflow`
- **Aesthetic:** "Athlete's Journal" — warm dark (#0A0A08) + electric lime (#C8FF00)
- **Design language:** Zero's ceremony + Whoop's authority + WeightFlow's warmth
- **Fonts:** Bebas Neue (display/numbers, negative tracking) + Outfit (UI body)
- **Icon:** W mark (weight-trend chart line) in lime on dark — adaptive icon (API 26+)
- **Position:** Genuinely free weight tracker. No subscription. Data stays on device.

## Revenue Model (Locked — Opus 4.7 reviewed)
- Core: always free (CSV import/export, all charts, all themes, history, badges)
- Pro: 4.99 EUR one-time (trend intelligence, body comp, health sync)
- Pro discovery: contextual only, no persistent CTA anywhere
- Donations: Ko-fi + Liberapay + GitHub Sponsors (Settings → Legal section)
- **AdMob: NOT in v1.0** — deferred to Phase 5. Opus: 3 revenue paths = death for solo dev.

## Tech Stack (Locked)
- Kotlin 2.2.10 + Jetpack Compose BOM 2025.04.01
- AGP 9.1.1 + Gradle 9.3.1 (AGP 9.x has built-in Kotlin — no kotlin-android plugin)
- Room **2.7.0** | KSP 2.3.2 | DataStore 1.1.1 | Navigation Compose 2.8.9
- Vico 1.13.1 charts | Manual DI (no Hilt) | StateFlow
- WorkManager 2.10.1 — opt-in daily reminder (not auto-scheduled; toggle in Settings)
- Google Fonts: Bebas Neue + Outfit via GMS provider
- Test: JUnit 4.13.2 + mockk 1.13.12 + turbine 1.1.0 + kotlinx-coroutines-test 1.8.1
- CI: GitHub Actions (tests + lint + build on every push)

## Repo
- URL: `https://github.com/VaibhavAher100/WeightFlow` (**public** — made public 2026-05-07 for Pages)
- GitHub Pages: `https://vaibhavaher100.github.io/WeightFlow/` (live, Jekyll, docs/ on main)
- Rename before launch (current name is temporary)
- PRD: issue #1 | Slice issues: #2-#23 | Architecture RFCs: #24-#29

## Git — IMPORTANT
- **No Claude co-authorship ever.** No `Co-Authored-By: Claude` or Anthropic traces in any commit.
- Full history was rewritten 2026-04-17 — all prior Claude co-author lines removed.
- Always write commit messages manually. No co-author trailers.

## Phase Status

| Phase | Description | Status |
|-------|-------------|--------|
| 0 | Infrastructure (environment, agents, skills, conventions, product strategy, PRD, issues) | Complete |
| 1 | Foundation (Android project + Room + DataStore + NavGraph shell) | Complete |
| 2 | All 6 screens + ViewModels + Vico + RFCs #24-26 | Complete (166 tests GREEN) |
| 3 | Polish + badge UI + goal banners + settings + accessibility + WorkManager | Complete (188 tests GREEN) |
| 4 | Play Store launch (privacy policy, signed build, ASO) | **In progress** |
| 5 | Firebase sync + iOS via KMP | Needs planning |

## Phase 4 — IN PROGRESS (sessions 2026-04-26-001, 2026-05-07-001, 2026-05-07-002, 2026-05-08-001, 2026-05-08-002, 2026-05-08-003, 2026-05-09-001)

| Item | Status |
|------|--------|
| COPPA age gate — year-of-birth picker (13+ threshold) | **DONE** — 18→13 fixed (Opus review) |
| POST_NOTIFICATIONS runtime request | **DONE** (MainActivity) |
| R8/ProGuard rules | **DONE** (proguard-rules.pro) |
| isMinifyEnabled=true + isShrinkResources=true | **DONE** |
| Signing config — validate all 4 creds, fail fast | **DONE** — validateReleaseSigning task (build-engineer) |
| Android keystore generated | **DONE** — `weightflow-release.jks`, RSA 2048, valid to 2051 |
| Keystore backed up | **DONE** — Desktop + Documents + **TODO: Google Drive (3rd location)** |
| GDPR Art. 17: Delete all data | **DONE** (ProfileScreen + DAOs) |
| GDPR Art. 20: CSV export (plaintext + encrypted + minimal) | **DONE** — 3 formats, AES-256 ZIP, 12+ char password |
| Network Security Config | **DONE** — cleartext blocked in release |
| Device-transfer exclusion | **DONE** — data_extraction_rules.xml `<device-transfer>` block |
| Medical disclaimer + safe-messaging | **DONE** — HomeScreen + OnboardingScreen |
| Privacy policy + ToS written | **DONE** — `docs/privacy/` (updated for SQLCipher + 3 export formats) |
| Privacy policy live URL | **DONE** — `https://vaibhavaher100.github.io/WeightFlow/privacy-policy/` |
| Privacy links before data entry | **DONE** — OnboardingScreen AgeGate footer |
| Privacy links in Settings | **DONE** — Settings → Legal section |
| Age gate flash fix | **DONE** — tri-state `null/true/false` in MainActivity |
| Edit individual entry | **DONE** — tap row → AlertDialog, `updateEntry()` in Room |
| Reminder opt-in toggle | **DONE** — Settings toggle schedules/cancels WorkManager |
| **Reminder permission state fix** | **DONE** — request permission BEFORE toggle (mobile-app-developer) |
| Competitor gap analysis | **DONE** — `docs/strategy/competitor-gap-analysis.md` |
| P0 stats (TrendsScreen) | **DONE** — all-time H/L/avg/count, 7D/30D change, rate, ETA to goal |
| BMI context (ProfileScreen) | **DONE** — category, normal range, difference from normal |
| App icon redesign | **DONE** — W mark vector, lime on dark, adaptive icon |
| Typography letter-spacing | **DONE** — display negative, label positive, body zero |
| Settings screen redesign | **DONE** — 2-col theme grid, plus export format selector + encrypted export dialog |
| Color token improvements | **DONE** — WFTokens warmer, accentSoft helper added |
| **Full UI/UX overhaul** | **DONE** — Zero/Whoop design language, all 6 screens, 17 commits (2026-05-08) |
| **SECURITY AUDIT REMEDIATION** | **DONE** (2026-05-08-002) — all 7 findings resolved |
| ├─ #1 HIGH: Database encryption | **DONE** — SQLCipher 4 + Android Keystore (kotlin-specialist) |
| ├─ #2 MEDIUM: CSV parser robustness | **DONE** — RFC-4180 kotlin-csv + 46 fuzz tests (kotlin-specialist) |
| ├─ #3 MEDIUM: GitHub Actions hardening | **DONE** — SHA pinning, dep-review, CodeQL, OSV, Dependabot (devops-engineer) |
| ├─ #4 MEDIUM: SECURITY.md accuracy | **DONE** — removed false cert-pinning claim, added scanning proof (security-auditor) |
| ├─ #5 MEDIUM: Encrypted CSV export | **DONE** — plaintext/AES-256 ZIP/minimal with security findings (kotlin-specialist) |
| ├─ #6 LOW: Notification permission state | **DONE** — request BEFORE toggle, show message if denied (mobile-app-developer) |
| └─ #7 LOW: Release signing validation | **DONE** — validateReleaseSigning task, all 4 creds checked (build-engineer) |
| **PRE-LAUNCH BUG FIXES (13 issues)** | **DONE** — 2026-05-08-002 (subagent execution) |
| ├─ SQLCipher key null on hardware devices | **DONE** — EncryptedSharedPreferences passphrase |
| ├─ Stones unit treated as kg | **DONE** — stToKg() + 5 branch fixes |
| ├─ Date off-by-one UTC+ timezones | **DONE** — atZone(systemDefault()).toLocalDate() |
| ├─ Onboarding discards initial weight | **DONE** — weightRepository.addEntry() in onComplete() |
| ├─ Save button stuck disabled | **DONE** — reset() + called on FAB tap |
| ├─ bundleRelease skips signing validation | **DONE** — wired to validateReleaseSigning |
| ├─ OSV scan bypassed release gate | **DONE** — removed continue-on-error |
| ├─ ZIP temp file leaked on cancel | **DONE** — delete on picker cancel |
| ├─ Password comment misleading | **DONE** — accurate comment + SECURITY.md |
| ├─ Privacy docs split | **DONE** — stub → canonical |
| ├─ Touch targets under 48dp | **DONE** — 36dp→48dp, 44dp→48dp |
| ├─ LocalDate.ofInstant API 34 regression | **DONE** — atZone().toLocalDate() |
| └─ OSV scan CI fully green | **DONE** — reusable workflow |
| **Device testing** | **DONE** — app launches, onboarding visible (2026-05-08-002) |
| **Debug SQLCipher bypass** | **DONE** — BuildConfig.DEBUG → plain Room (avoids main thread block) |
| **onboardingState null fix** | **DONE** — absent key emits false not null |
| **UI/UX plan verification** | **DONE** (2026-05-09-001) — all 12 tasks confirmed implemented, 3 pending files committed |
| **Keystore Google Drive backup** | **DONE** (2026-05-09) — Drive ID `18t1kAGv2ScYkcmDLoDVVQ0jkUXjZ3tiM` |
| **GitHub Actions signing secrets** | **DONE** (2026-05-09) — KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD set |
| **CI release build signed** | **DONE** (2026-05-09) — workflow wired; bundleRelease produces signed AAB |
| Firebase Crashlytics end-to-end | **TODO** — blocked on google-services.json |
| Play Store listing + ASO | **TODO** |
| AAB build + upload | **TODO** |
| Play Data Safety form | **TODO** |
| Ko-fi + Liberapay URLs | **TODO** — update in ProfileScreen once accounts created |
| Complete device testing (all screens) | **TODO** — onboarding visible, rest untested |

## Key Product Decisions (Locked)

| Decision | Choice |
|----------|--------|
| Nav | Home + Trends + History + Profile + FAB (no Log tab) |
| Log sheet | Drum-roll picker (whole + decimal), weight + date only |
| Home layout | Ritual weight block (64sp) + sparkline + stat cards + goal bar |
| Onboarding | 4 screens: age gate (13+) → unit → weight → goal, step dots |
| Age gate | 13+ globally (COPPA minimum). Year-of-birth field, not checkbox. |
| Charts | Line chart (Vico 1.13.1) + stat cards. Axes removed (ugly decimals). |
| Time filters | 7D / 30D / 3M / 6M / 1Y / All |
| Goals | Single active goal → GoalAchievedScreen → maintenance mode |
| Badges | 12 (Zero-inspired) |
| Pro price | 4.99 EUR one-time |
| Revenue priority | Donations first, Pro second. **No AdMob in v1.0.** |
| CSV import | WeightFit + Happy Scale + Apple Health + generic |
| Themes | 8 palettes (Lime default). Color-dot grid in Settings. |
| RTL | RTL-safe from Phase 2 (start/end semantics) |
| Weight storage | Always kg internally, unit conversion is display-only |
| Settings | Accessed via gear icon in ProfileScreen PageHeader |
| Reminder | Opt-in via Settings toggle — not auto-scheduled at startup |
| AdMob | NOT in v1.0. Deferred to Phase 5. |
| Design language | Zero ceremony + Whoop authority + lime warmth. Locked 2026-05-08. |

## Architecture RFCs (all implemented)

| RFC | Decision |
|-----|----------|
| BadgeObserver #24 | Reactive combine() over Room Flows |
| CsvImporter #25 | Single entry point, CsvFormat enum in ParseResult |
| GoalStateMachine #26 | Sealed FSM with transition functions |
| StoredWeight + HomeUiStateMapper #27 | Value class + single conversion in ViewModel |
| SortedEntries + named Repository methods #28 | Type-safe ordering contract |
| HomeDataAggregator #29 | ViewModel gets one dependency instead of five |

## Tests (GREEN — BUILD SUCCESSFUL 2026-05-08)

| File | Tests | Status |
|------|-------|--------|
| `domain/WeightConverterTest.kt` | 13 | GREEN |
| `domain/GoalProgressCalculatorTest.kt` | 15 | GREEN |
| `domain/BadgeEngineTest.kt` | 24 | GREEN |
| `domain/BadgeObserverTest.kt` | 6 | GREEN |
| `domain/CsvParserTest.kt` | 12 | GREEN |
| `domain/CsvParserFuzzTest.kt` | 46 | GREEN ✨ NEW (session 2026-05-08-002) |
| `domain/CsvImporterTest.kt` | 5 | GREEN |
| `domain/CsvExporterTest.kt` | 27 | GREEN (was 9, +18 new) |
| `domain/GoalStateMachineTest.kt` | 14 | GREEN |
| `data/DatabaseKeyManagerTest.kt` | 11 | GREEN ✨ NEW (session 2026-05-08-002) |
| `ui/home/HomeViewModelTest.kt` | 18 | GREEN |
| `ui/logentry/LogEntryViewModelTest.kt` | 24 | GREEN |
| `ui/trends/TrendsViewModelTest.kt` | 18 | GREEN |
| `ui/history/HistoryViewModelTest.kt` | 7 | GREEN |
| `ui/profile/ProfileViewModelTest.kt` | 16 | GREEN |
| `ui/onboarding/OnboardingViewModelTest.kt` | 24 | GREEN |
| `ui/home/HomeUiStateMapperTest.kt` | 11 | GREEN |
| `ui/settings/SettingsViewModelTest.kt` | 15 | GREEN (was 8, +7 new) |
| **TOTAL** | **~280+** | **0 failures** |

Session 2026-05-08-002 added:
- 46 CSV fuzz tests (quoted commas, CRLF/LF, huge datasets, malformed rows, blanks, encoding)
- 11 Database encryption key manager tests
- 18 Encrypted export + minimal CSV tests
- 7 Notification permission state + export format tests

## ViewModel Test Patterns (Locked)

- **Dispatcher:** `StandardTestDispatcher()` + `Dispatchers.setMain` in `@Before`
- **stateIn initial skip:** `awaitRealState()` — consumes Loading before asserting
- **Settings-style VMs (no Loading):** `expectMostRecentItem()` after `advanceUntilIdle()`
- **mockk for repos:** `every { repo.flowProp } returns MutableStateFlow(...)` (not `coEvery`)
- **Synchronous actions:** no `advanceUntilIdle()` needed
- **Coroutine-launching actions:** call `advanceUntilIdle()` after
- **LogEntryViewModel:** uses `SharingStarted.WhileSubscribed(5_000)` + `uiState.launchIn(viewModelScope)` to seed combine; tests mock `getEntriesNewestFirst()` returning `flowOf(emptyList())`

## Build Configuration (AGP 9.x specific)

- `gradle/libs.versions.toml`: Kotlin 2.2.10, KSP 2.3.2, Room 2.7.0, DataStore 1.1.1, NavCompose 2.8.9, Vico 1.13.1, Compose BOM 2025.04.01, mockk 1.13.12, turbine 1.1.0, WorkManager 2.10.1
- Firebase deps **commented out** — activate after adding `google-services.json`
- `gradle.properties`: `android.disallowKotlinSourceSets=false` (KSP + AGP 9.x)
- `app/build.gradle.kts` plugins: `android-application` + `kotlin-compose` + `ksp` (NO `kotlin-android`)
- Release signing: **fail-fast** — `check()` throws if KEYSTORE_PATH not in local.properties
- `isMinifyEnabled = true` + `isShrinkResources = true` in release
- Room schema export: `app/schemas/` — currently at **schema v2**

## Keystore (CRITICAL)

- **File:** `WeightFlow/weightflow-release.jks` (gitignored via `*.jks`)
- **local.properties** (not committed): KEYSTORE_PATH, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD
- **Backup 1:** `Desktop/WeightFlow-Keystore-BACKUP/`
- **Backup 2:** `Documents/WeightFlow-Keystore-BACKUP/`
- **Backup 3:** Google Drive — `weightflow-release.jks` (ID: `18t1kAGv2ScYkcmDLoDVVQ0jkUXjZ3tiM`, owner: vaibhavaher100@gmail.com, uploaded 2026-05-09)
- SHA256: `F7:1B:CA:7A:4D:12:CE:FB:E5:E5:38:91:D0:5F:99:99:DA:84:36:6A:0F:6B:84:66:0E:3D:A9:C9:00:DC:CC:CF`
- Valid until: 2051-05-01

## Privacy / Legal

- **Privacy Policy (live):** `https://vaibhavaher100.github.io/WeightFlow/privacy-policy/`
- **Terms of Service (live):** `https://vaibhavaher100.github.io/WeightFlow/terms-of-service/`
- **GitHub Pages:** `docs/` on `main` branch, Jekyll minima theme
- Age threshold: 13+ globally (COPPA minimum). Year-of-birth field.
- Backup/device-transfer: cloud backup excluded, device-transfer excluded, CSV export as migration path
- Crashlytics: NOT active. Commented deps. Activate with `google-services.json` in Phase 5.

## UI Layer (post session 2026-05-08-001 — FULL OVERHAUL COMPLETE)

| Component | Status |
|-----------|--------|
| App icon | **DONE** — W mark vector, lime on dark, adaptive icon |
| Typography | **DONE** — Bebas Neue display (tabular-nums on stats), Outfit body |
| Color tokens | **DONE** — WFTokens updated: lime-tinted grays, accentSoft, accentBorder |
| Design system | **DONE** — Zero/Whoop-inspired spec at `docs/superpowers/specs/2026-05-07-ui-ux-overhaul-design.md` |
| WheelPicker | **DONE** — `ui/components/WheelPicker.kt`, snapping LazyColumn, hairlines |
| WFHaptics | **DONE** — `ui/components/HapticsHelper.kt`, tick/confirm/celebrate |
| ShellScreen | DONE |
| HomeScreen | **DONE** — 64sp ritual weight block, underglow, sparkline, Start/Lost/Goal cards, goal bar |
| GoalAchievedScreen | **DONE** — full-screen glow burst, journey stats, CTA → Profile |
| TrendsScreen | **DONE** — coaching sentence, producerReady guard on chart |
| HistoryScreen | **DONE** — bold 20sp day numbers, delta chips (pill), today lime tint |
| ProfileScreen | **DONE** — journey card (Start/Now/Goal + progress bar + ETA) |
| LogEntry sheet | **DONE** — drum-roll picker, 68sp live number, save animation, new-low celebration |
| OnboardingScreen | **DONE** — step dots (active=pill), "STEP N OF 4" eyebrow |
| SettingsScreen | **DONE** — 2-col theme grid with color dots, active lime border |

## Guardrails (Active)

| Rule | Event | Purpose |
|------|-------|---------|
| `hookify.tdd-production-guard.local.md` | file | Warns before any `app/src/main/java/**/*.kt` write |
| `hookify.completion-verification.local.md` | stop | Requires `BUILD SUCCESSFUL` evidence before completion |
| `hookify.session-start-plan-check.local.md` | prompt | Read `_state.md` + TDD plan before coding |

## Open Items

- [ ] **Complete device testing** — go through all screens, note any issues
- [ ] Back up keystore to Google Drive — **CRITICAL, 3rd backup location**
- [ ] Build release AAB: `./gradlew bundleRelease`
- [ ] Upload AAB to Play Store internal testing track
- [ ] Complete Play Data Safety form in Play Console
- [ ] Play Store listing + ASO copy (android-aso skill)
- [ ] Wire Firebase Crashlytics end-to-end (needs google-services.json)
- [ ] Update Ko-fi + Liberapay URLs in ProfileScreen once accounts created
- [ ] Stone unit (ST): `kgToSt` converter not implemented — ST shows kg values in log entry coaching sentence (P1, not blocking)

## Next Session Should

1. **Complete device testing** — go through all screens, fix any issues found
2. **Back up keystore to Google Drive** — CRITICAL. 3rd location. Do before building AAB.
3. **Build release AAB** — `./gradlew bundleRelease` — should succeed, keystore is wired
4. **Play Store listing + ASO** — `android-aso` skill, write listing copy
5. **Internal track upload** — `android-playstore-setup` skill
6. **Play Data Safety form** — complete in Play Console (offline-first, no data sharing)
7. **Firebase Crashlytics** — unblock once `google-services.json` available

## Debug vs Release DB (CRITICAL — do not forget)

Debug builds use plain Room (no SQLCipher). Release builds use SQLCipher + EncryptedSharedPreferences key.
`BuildConfig.DEBUG` check in `AppDatabase.buildDatabase()`. Do NOT remove this — EncryptedSharedPreferences blocks main thread for 30-55s on cold start when called synchronously.
Long-term fix needed: move DB init off main thread (architectural change, Phase 5).

## Vico Note (API)
Vico 1.13.1 uses LEGACY API — context7 returns v3 docs (wrong). Use:
- `com.patrykandpatrick.vico.compose.chart.Chart`
- `com.patrykandpatrick.vico.compose.chart.line.lineChart`
- `com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer` + `FloatEntry`
- Chart crash guard: use `producerReady` flag — set true after `setEntriesSuspending().await()`

## Competitor Gap Analysis

Saved: `docs/strategy/competitor-gap-analysis.md`

P0 gaps (all closed):
- All-time stats + 7D/30D change + rate + ETA to goal → TrendsScreen ✓
- BMI category + normal range + diff → ProfileScreen ✓
- Edit individual entry → HistoryScreen ✓
- Drum-roll weight picker → LogEntry ✓
- Full UI/UX redesign (Zero/Whoop quality) → all screens ✓

P1 gaps (v1.1):
- Height in imperial (ft+in) for lbs/st users
- Weekly day-dot streak visualization
- Share app + Write a Review links in Settings
- Individual entry detail view
- Stone unit `kgToSt` conversion

P2 gaps (Phase 5):
- Calendar month view, Google Fit sync, multiple metric charts, challenges
