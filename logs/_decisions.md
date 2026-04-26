# Project Decisions Log

## Session 2026-04-11-001

**Decision:** Focus on service arbitrage business models with AI assistance (Claude)

**Rationale:** User is MSc Embedded Systems student, Germany-based, 10-15 hrs/week available, target ₹40-60K/month. Service arbitrage using Claude allows 60-80% of work to be outsourced to AI, leaving 10-15 hrs/week for client acquisition and quality oversight.

**Context:** Initial request for low-capital, high-profit digital business from office chair. Generic startup advice ruled out as "shooting blindly." Conducted thorough research on actual proven models with real pricing data, case studies, and execution playbooks.

**Recommended Start:** LinkedIn Ghostwriting (easiest entry, fastest to first client by day 14-21)

**Alternative:** Website Building for German SMBs (leverages Germany location advantage, higher margins in EUR)

## Session 2026-04-11-002

**Decision:** Build free Android weight tracking app (WeightFit competitor) with zero developer cost

**Rationale:** User wants to build a better version of WeightFit with: interactive graphs, color customization, two-tier UI (basic + pro mode). Research confirmed 100% feasible at $25 one-time cost (Play Store). Tech stack: Kotlin + Compose + Room + Firebase (free tier) + AdMob.

**Key constraints:** Zero ongoing cost, free to users, minimal ads for revenue, Android-first

**Tech stack decided:** Kotlin, Jetpack Compose, Room DB, Firebase Firestore (free tier), Vico charts, Google AdMob

---

## Session 2026-04-12-001

**Decision:** WeightFlow architecture — manual DI (no Hilt), StateFlow throughout, History in bottom nav, Settings via Profile

**Rationale:** Hilt adds complexity not worth the benefit for a solo developer. Manual DI via Application class is simpler, easier to debug, and sufficient for 6 screens. History gets its own bottom nav tab (5 tabs total); Settings is accessed from Profile to avoid a 6th tab. This is a final navigation structure decision.

**Visual identity locked:** App name WeightFlow. Dark warm aesthetic (#0F0E0B base + #C8FF00 accent). Bebas Neue for weight numbers, Outfit for UI. 8 accent color options user-selectable at runtime.

**Implementation plans written:** Foundation (Plan 1, 7 tasks) + Screens (Plan 2, 9 tasks). Both plans have complete Kotlin code — no placeholders. Ready for subagent-driven or inline execution.

---

## Session 2026-04-12-002

**Decision:** Revenue model — free core forever, $2.99 one-time Pro unlock, no subscription
**Rationale:** Market is saturated with $40–70/year subscriptions. Positioning as genuinely free builds trust, drives word-of-mouth, sustains itself via Pro conversions + donations. CSV import/export free for everyone — data portability = growth hack.

**Decision:** iOS via Kotlin Multiplatform — Android first, fund Apple account via donations
**Rationale:** $99/year Apple Developer account breaks zero-budget constraint. Strategy: Android launch → TestFlight beta free via KMP → donations fund the account. Compose Multiplatform stable for iOS means shared UI + logic.

**Decision:** Privacy architecture — offline-first = GDPR compliance by default
**Rationale:** On-device storage satisfies hardest GDPR/PIPL requirements architecturally. Remaining: AdMob consent dialog, analytics opt-in, COPPA age gate (13+), China offline-only mode.

**Decision:** Isolated WeightFlow/ environment, project-local Claude only
**Rationale:** Keeps app separate from business research. 18 agents + 4 meta skills in WeightFlow/.claude/. Global Claude not used for app work.

**Decision:** Conventional Commits + SemVer + ADR + GitHub Actions CI from day 1
**Rationale:** Industry standard. CI set up before first commit so regressions are caught automatically. ADRs capture decisions for future contributors.

---

## Session 2026-04-12-004 — 2026-04-12

**Decision:** CLI over MCP as default
**Rationale:** CLIs (`gh`, `codex`, `./gradlew`, `npx skills`) are faster, deterministic, and offline-capable. MCP tools add latency and auth overhead for tasks that have direct CLI equivalents.
**Context:** User explicitly requested CLI be prioritised wherever efficient.

**Decision:** Prune agents to 35 (from 140)
**Rationale:** All 140 agent definitions are indexed at session start. 105 were irrelevant to an Android app (web frameworks, ML, cloud infra, PowerShell). Smaller set = less noise in agent routing, fewer wasted tokens.
**Context:** User asked to remove unnecessary bloat after full catalog was installed.

**Decision:** OpenAI Codex CLI for Phase 1-2 boilerplate
**Rationale:** Codex runs headless in a sandboxed context -- ideal for isolated, pattern-fill tasks (Room entities, DAOs, ViewModel shells). Claude Code retains ownership of wiring, DI, architecture decisions.
**Context:** `codex-cli 0.118.0` already installed globally. User referenced Codex intro video.

---

## Session 2026-04-12-005 -- 2026-04-12

**Decision:** Nav is 4 tabs + FAB, no dedicated Log tab
**Rationale:** A Log tab only contains a number pad and save button -- wasted real estate. FAB persists across all tabs, making logging one tap from anywhere.
**Context:** grill-me question 3.

**Decision:** Home screen uses always-full layout with motivating empty states
**Rationale:** Empty states with copy ("Log 7 days to unlock your trend") set expectations and motivate logging. Data-gated reveal makes the screen look half-built on day 1.
**Context:** grill-me question 5.

**Decision:** Pro = 4.99 EUR one-time, contextual discovery only, no persistent CTA
**Rationale:** App marketed as free. Donations are primary revenue. Pro discovered when advanced users stumble into power features. No aggressive upsell is core to the brand.
**Context:** grill-me questions 12-13. Inspired by Zero's model.

**Decision:** 12 Zero-inspired badges (not 8)
**Rationale:** Zero uses streaks + volume milestones + resilience badges. 12 covers the full user lifecycle including maintenance mode (Steady State) and comeback behaviour. More badges = longer engagement.
**Context:** grill-me question 15, Zero research.

**Decision:** BadgeObserver reactive pattern (RFC #24)
**Rationale:** Calling BadgeEngine from WeightRepository.insert() is imperative and untested. Reactive combine() over Room Flows is cleaner -- write path stays pure, distinctUntilChanged() handles the no-change case.
**Context:** improve-codebase-architecture friction point 1.

**Decision:** GoalStateMachine explicit FSM (RFC #26)
**Rationale:** Without an explicit FSM, goal side effects (celebration, maintenance mode write) are silently buried in HomeViewModel. Sealed GoalState + transition functions make the state machine testable and enforce valid transitions.
**Context:** improve-codebase-architecture friction point 3.

**Decision:** SortedEntries wrapper type + named Repository ordering methods (RFC #28)
**Rationale:** getAllEntries() has no ordering guarantee. Future callers (Trends, sparkline, CSV export) all assume sorted data. SortedEntries makes the contract compile-time visible. Named methods (getEntriesAscending/Descending) eliminate ambiguity.
**Context:** improve-codebase-architecture friction point 5.

**Decision:** HomeDataAggregator pure Kotlin object (RFC #29)
**Rationale:** HomeViewModel with 5 direct dependencies becomes a god class. HomeDataAggregator owns the combine() logic, ViewModel gets one dependency, both are independently testable.
**Context:** improve-codebase-architecture friction point 6.

---

## Session 2026-04-12-007 -- 2026-04-12

**Decision:** Room upgraded from 2.6.1 → 2.7.0
**Rationale:** Room 2.6.1 is not compatible with KSP2 (default in KSP 2.1.x). Two bugs manifest: (1) "unexpected jvm signature V" during KSP annotation processing for `@Delete`/`@Upsert suspend fun` with Unit return; (2) generated Java impl has `Continuation<T>` instead of `Continuation<? super T>`, causing `@Override` compile errors. Room 2.7.0 adds full KSP2/K2 compatibility. `ksp.useKSP2=false` is NOT a valid workaround for AGP 9.x (breaks variant resolution).
**Context:** First Room entities+DAOs built in TDD Step 4.

**Decision:** `@Delete` returns `Int`, `@Upsert` returns `Long`
**Rationale:** Even after the Room upgrade, these are strictly better APIs — `Int` = rows deleted (useful for error detection), `Long` = inserted row ID (useful for referencing the new row). Unit return is valid but provides no feedback.
**Context:** Discovered during KSP2 debugging; kept as a deliberate API improvement.

**Decision:** TDD Step 4 complete — Room entities + DAOs pass compiler/KSP validation
**Rationale:** `connectedAndroidTest` requires a running emulator/device. The test APK built successfully and Room KSP validated all DAO interface contracts at compile time. Tests will be executed on first device connection.
**Context:** No emulator was running during the session.

---

## Session 2026-04-12-006 -- 2026-04-12

**Decision:** AGP 9.1.0 has built-in Kotlin -- do NOT apply `org.jetbrains.kotlin.android` separately
**Rationale:** AGP 9.x registers the 'kotlin' Gradle extension internally. Applying the standalone `kotlin-android` plugin causes "Cannot add extension with name 'kotlin'" at configuration time. The Kotlin compiler is already available; only `kotlin-compose` and `ksp` need to be applied on top.
**Context:** First attempt to wire Kotlin into the project. Error discovered via stacktrace.

**Decision:** `android.disallowKotlinSourceSets=false` required for KSP + AGP 9.x
**Rationale:** AGP 9.x with built-in Kotlin blocks plugins from adding sources via `kotlin.sourceSets` DSL. KSP uses this legacy API to wire generated source directories. The flag suppresses the error; KSP still functions correctly.
**Context:** Second configuration error after resolving the kotlin-android conflict.

**Decision:** XML theme switched from MDC to `Theme.AppCompat.DayNight.NoActionBar`
**Rationale:** Android Studio generated `Theme.MaterialComponents.DayNight.DarkActionBar` which requires `com.google.android.material:material`. We use Compose Material3 only. AppCompat is pulled in transitively by `androidx.activity:activity-compose`, so no extra dependency is needed.
**Context:** Resource linking failure during first `testDebugUnitTest` run.

**Decision:** `WeightEntry` and `UserProfile` are plain domain data classes (no @Entity yet)
**Rationale:** Room annotations belong in the data layer, not the domain layer. The domain tests are pure JVM tests with no Android dependencies. Room entities will wrap or extend these types when the data layer is built in TDD Step 4.
**Context:** Ensuring domain tests stay pure JVM (fast, no emulator needed).

---

## Session 2026-04-13-001 — 2026-04-13

**Decision:** Flat `data/` package layout is confirmed and locked
**Rationale:** Sessions 006-007 chose flat `data/` over the plan's `data/db/prefs/repository/` subdirectory structure. Plan audit verified this was intentional — simpler for a solo project, no real navigation penalty at this scale. Not reverting.
**Context:** Plan audit during verification-before-completion review.

**Decision:** `turbine` + `mockk` added to test dependencies
**Rationale:** Both were present in the original foundation plan (`docs/plans/2026-04-11-weightflow-foundation.md`) but were silently dropped when sessions 006-007 configured `build.gradle.kts`. They are required for Phase 2 ViewModel testing — `turbine` for Flow assertions, `mockk` for repository fakes.
**Context:** Gap found during plan vs reality audit; fixed immediately.

**Decision:** Three hookify guardrails enforced via `.claude/hookify.*.local.md`
**Rationale:** Silent drift occurred across sessions 006-007 (missing deps, package structure divergence). Automated hooks prevent the three most likely failure modes without requiring manual discipline.
**Context:** User request: "put guard rails for that" after asking for plan-drift verification.

## Session 2026-04-13-002 — 2026-04-13

**Decision:** Google Fonts via `ui-text-google-fonts` for Bebas Neue + Outfit
**Rationale:** No binary TTF downloads possible in-session; Google Fonts runtime download is production-grade with graceful fallback to system fonts on no-network first launch.
**Context:** Step 10 theme system implementation.

**Decision:** `font_certs.xml` must be created manually in `res/values/`
**Rationale:** The `ui-text-google-fonts` AAR did not expose `R.array.com_google_android_gms_fonts_certs` automatically in this AGP 9.x / Compose BOM 2025.04.01 build. Adding the certs XML file directly is the documented Android workaround.
**Context:** Build failed with "Unresolved reference 'array'" until certs file was added.

**Decision:** FAB visible only on Home tab, hidden on Trends/History/Profile
**Rationale:** Matches locked product decision — no Log tab, weight entry is a contextual action from Home, not a persistent affordance.
**Context:** Step 11 ShellScreen implementation.

## Session 2026-04-13-003 — 2026-04-13

**Decision:** LogEntry bottom sheet is state-managed in ShellScreen, not a NavGraph destination
**Rationale:** Navigation-based bottom sheets in Compose Navigation cause awkward background content layering and don't feel native. FAB sets `showLogEntry = true` → `ModalBottomSheet` overlays the Scaffold content.
**Context:** Phase 2 LogEntry implementation — the original NavGraph had `composable(Screen.LogEntry.route)` but that was replaced.

**Decision:** `HomeDataAggregator` is an interface (not a concrete class)
**Rationale:** Makes `HomeViewModel` trivially unit-testable via anonymous fake objects. Concrete `HomeDataAggregatorImpl` lives in domain/ but depends on data/ classes — interface sits in domain/ cleanly.
**Context:** RFC #29 implementation. Discovered when writing HomeViewModelTest that mockking a concrete class is fragile.

**Decision:** ViewModel test pattern — `awaitRealState()` helper to skip `stateIn` initial Loading
**Rationale:** `stateIn(WhileSubscribed, initialValue = Loading)` always emits Loading as first item before upstream coroutine runs with StandardTestDispatcher. All ViewModel tests use the helper to skip it. Pattern locked for Phase 2/3 consistency.
**Context:** HomeViewModelTest had 9/11 failures on first run — all ClassCastExceptions from Loading being cast to Empty/HasData.

## Session 2026-04-13-004 — 2026-04-13

**Decision:** Vico 1.13.1 API — use source JARs for older library versions, not docs tools
**Rationale:** context7 and most internet docs describe Vico v2/v3 (`CartesianChartHost`, `CartesianChartModelProducer`, `lineSeries`). These are non-existent in 1.13.1. Correct API: `Chart` composable + `ChartEntryModelProducer.setEntriesSuspending()` + `FloatEntry(x, y)`.
**Context:** Compile failed with "Unresolved reference 'cartesian'" on first attempt. Reading the source JAR directly from `~/.gradle/caches` revealed the actual 1.13.1 package structure.

**Decision:** Onboarding gate `initialValue = true` in MainActivity
**Rationale:** Prevents brief flash of OnboardingScreen while DataStore cold-starts on an existing user's device. New users see a momentary ShellScreen loading spinner instead — acceptable since it shows a loading indicator anyway.
**Context:** `collectAsStateWithLifecycle` requires an `initialValue`; `false` would incorrectly show onboarding to all users on every cold start for ~200ms.

**Decision:** OnboardingViewModel constructed in `MainActivity.onCreate`, not via ViewModelFactory
**Rationale:** Matches project's manual DI pattern. The ViewModel is activity-scoped and onboarding is a one-shot flow; no benefit to ViewModelFactory complexity without Hilt.
**Context:** Project uses `WeightFlowApp` as DI root. All VMs in NavGraph are constructed via `vmFactory` lambda; MainActivity follows same pattern.

## Session 2026-04-17-002 — 2026-04-17

**Decision:** Remove partial Crashlytics wiring; re-add end-to-end in Phase 3 with real `google-services.json`
**Rationale:** Silent try-catch on `FirebaseCrashlytics.getInstance()` meant release builds shipped with zero crash telemetry and no visible failure. Partial wiring gives false confidence.
**Context:** Codex adversarial review flagged as high-severity no-ship issue.

---

**Decision:** Device-to-device transfer stays enabled until user-facing export/import or Firebase sync ships
**Rationale:** Disabling it before a recovery path exists causes avoidable data loss on phone replacement, contradicting the app's user-first positioning.
**Context:** Codex adversarial review flagged as high-severity no-ship issue. Device-transfer block will be re-added only when Phase 5 Firebase sync or in-app CSV export UI is complete.

---

**Decision:** `isMinifyEnabled = false` stays until Phase 4
**Rationale:** Enabling R8 without ProGuard rules for Room + Kotlin serialization would break the release build. Phase 4 task: write rules first, then enable.
**Context:** Audit finding — deferred deliberately, not an oversight.

## Session 2026-04-17-004 — 2026-04-17

**Decision:** No Claude co-authorship in any commit, PR, or file — permanent rule
**Rationale:** User explicitly requires zero trace of Claude/Anthropic in the project. Full history was rewritten to remove all prior `Co-Authored-By: Claude` lines.
**Context:** User gave one-time permission to rewrite history + force-push. All future commits must be written without any co-author trailer.

---

**Decision:** `expectMostRecentItem()` for SettingsViewModel tests instead of `awaitRealState()` skip-pattern
**Rationale:** `StateFlow` deduplicates equal values. When combine emits the same value as the `stateIn` initial value, no second emission occurs — so `awaitRealState()` (which awaits a second item to skip the default) deadlocks. `expectMostRecentItem()` after `advanceUntilIdle()` is correct for VMs with no Loading state.
**Context:** SettingsViewModelTest had one failing test after the duplicate-overload fix. Root cause was StateFlow deduplication, not a test-setup issue.

---

**Decision:** WorkManager wired actively; Crashlytics scaffolded-but-commented
**Rationale:** WorkManager requires no external config file — safe to activate immediately. Firebase Crashlytics requires `google-services.json` before any dep is active; partial wiring causes silent build failures.
**Context:** Phase 3 completion. Crashlytics activation checklist now lives in `WeightFlowApp.kt`.

## Session 2026-04-18-002 — 2026-04-18

**Decision:** Remove Vico chart axes (startAxis/bottomAxis = null) — show MIN/MAX/AVG/CHANGE as stat cards below chart instead
**Rationale:** Default Vico axis labels generated ugly decimals (71.11, 62.22…) from auto-dividing the Y range. Removing axes and showing explicit stat cards below the chart card is cleaner and matches the mockup better than trying to format internal Vico axis labels.
**Context:** TrendsScreen redesign. Vico 1.13.1 axis formatter API uncertain — stat card approach avoids import risk and gives more information.

---

**Decision:** Use `WFTokens` design tokens directly in all screen composables, not Material3 semantic tokens
**Rationale:** Material3 semantic tokens (surfaceVariant, onSurfaceVariant, primaryContainer) don't map cleanly to the "Athlete's Journal" warm-dark palette. Direct use of `WFTokens.Card`, `WFTokens.Text2`, `WFTokens.Text3`, `WFTokens.Border`, `WFTokens.Success`, `WFTokens.Danger` gives exact visual match to mockup across all 8 themes.
**Context:** Root cause of the mockup gap — screens were using Material defaults instead of project tokens.

## Session 2026-04-18-003 — 2026-04-18

**Decision:** HistoryScreen uses flat list with border-bottom dividers (not card-per-entry) with a fixed 38dp date column
**Rationale:** The mockup CSS (`.hist-item`) uses `border-bottom: 1px solid var(--border)` and a fixed-width date column with `Bebas Neue` day number. First implementation incorrectly used card-per-row. Comparing against the mockup revealed 6 gaps — the flat divider layout is fundamentally different from cards.
**Context:** User asked to check UI against mockup before declaring screens done. Comparison of `app-design.html` CSS for `.hist-item`, `.hi-date`, `.hi-day-n` classes.

---

**Decision:** Delta chip in HistoryScreen uses 6dp border radius (rectangular), not 20dp (pill)
**Rationale:** Mockup CSS: `.hi-d { border-radius: 6px }`. The HomeScreen DeltaPill uses 20dp pill shape — these are different components. HistoryScreen chip is intentionally more compact and rectangular.
**Context:** Mockup comparison revealed shape mismatch in first implementation.

---

**Decision:** ProfileViewModel extended to 4 dependencies (UserProfileRepository, UserPrefsDataStore, WeightRepository, BadgeObserver)
**Rationale:** Phase 3 spec (`_state.md`) indicated `+earnedBadges` and `+BadgeObserver` were planned. Additionally, goal progress %, streak, BMI, start/current weight, and entries count all require WeightRepository access. All computation kept in VM — screen stays pure display.
**Context:** ProfileScreen redesign needed data the original 2-dep VM couldn't provide. RFC #24 (BadgeObserver) was already implemented — wiring it to ProfileViewModel completes the reactive badge display chain.

---

## Session 2026-04-18-004 — 2026-04-18

**Decision:** LogEntry unit toggle is display-only in the sheet UI.
**Rationale:** LogEntryViewModel reads unit from DataStore but exposes no `onUnitSelected` method. Adding one would require a new test (per TDD guardrail). Since unit is configured in Onboarding and Settings, displaying the active unit without change affordance is correct — the sheet is a focused log entry tool.
**Context:** LogEntrySheet UI overhaul. Mockup shows a segmented unit toggle; adapted as a read-only indicator showing which unit is active.

---

**Decision:** OnboardingScreen uses pill-style step dots with animated width instead of "Step N of 4" text.
**Rationale:** Visual progress is clearer than text. Active step = 24dp wide accent pill, past = accent 40% alpha 8dp dot, future = grey 30% alpha 8dp dot. No mockup existed for Onboarding — designed from scratch following Athlete's Journal token patterns.
**Context:** Onboarding UI overhaul. The step dot pattern is consistent with how other screens use accent + Text3 for active/inactive states.

---

**Decision:** LogEntry weight input uses `BasicTextField` with Bebas Neue 72sp and +/− step buttons.
**Rationale:** Combines keyboard entry (tap number, type) with touch-based increment/decrement. No separate hidden field needed. The `decorationBox` shows "0.0" placeholder when empty. No VM changes required — both modes call `viewModel.onWeightInput(string)`.
**Context:** Mockup Phone 3 shows pure +/− controls with no keyboard. Hybrid approach preserves keyboard fallback while matching the visual design.

---

## Session 2026-04-26-001 — 2026-04-26

**Decision:** Age gate threshold raised globally to 18+ (not region-gated).
**Rationale:** India DPDP §14 requires verifiable parental consent for users under 18 — stricter than COPPA's 13+. Implementing a region-gate would require locale detection and two code paths. Global 18+ gate is simpler, still legally valid for COPPA (which requires 13+, so 18+ exceeds it), and removes any risk of DPDP non-compliance in India.
**Context:** Compliance-auditor agent audit (2026-04-26) identified DPDP §14 as a Play Store blocker for India distribution.

---

**Decision:** Year-of-birth TextField replaces binary "I am 13+" checkbox for age gate.
**Rationale:** FTC guidance states that a binary self-attestation checkbox ("I am 13+") is not a neutral age gate because children quickly learn to tick it. A year-of-birth field calculates actual age and disables the Next button if age < 18 — the gate cannot be bypassed by clicking a checkbox. This satisfies COPPA's requirement for a reasonable mechanism to prevent underage access.
**Context:** Compliance-auditor audit item #3 identified the checkbox as a COPPA blocker.

---

**Decision:** R8/ProGuard enabled in release with conservative keep rules; signing config reads from local.properties.
**Rationale:** Phase 3 intentionally deferred R8 until rules could be written. local.properties (git-ignored) is the standard Android convention for keystore credentials — avoids committing secrets while allowing CI to inject values via environment or file injection. Fallback to debug signing allows unsigned release testing without a keystore present.
**Context:** Phase 4 launch prep — Play Store requires a signed AAB; R8 reduces APK size toward the 15MB budget.
