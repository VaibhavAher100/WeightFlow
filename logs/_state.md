---
last_session: 2026-04-12-005
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
- Kotlin 2.x + Jetpack Compose BOM 2024.09
- Room 2.6.1 | DataStore 1.1.1 | Navigation Compose 2.7.7
- Vico 1.13.1 charts | Manual DI (no Hilt) | StateFlow
- CI: GitHub Actions (tests + lint + build on every push)

## Phase Status

| Phase | Description | Status |
|-------|-------------|--------|
| 0 | Infrastructure (environment, agents, skills, conventions, product strategy, PRD, issues) | Complete |
| 1 | Foundation (Android project + Room + DataStore + NavGraph shell) | Plan ready, needs Android Studio |
| 2 | All 6 screens + ViewModels + Vico | Plan ready, blocked on Phase 1 |
| 3 | Onboarding + polish + empty/error states | Needs brainstorm |
| 4 | Play Store launch (privacy policy, Crashlytics, signed build, ASO) | Needs planning |
| 5 | Firebase sync + AdMob + iOS via KMP | Needs planning |

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
| SortedEntries + named Repository methods | #28 | Type-safe ordering contract, delete getAllEntries() |
| HomeDataAggregator | #29 | ViewModel gets one dependency instead of five |

## Implementation Plans

| Plan | File | Status |
|------|------|--------|
| Foundation (7 tasks) | `docs/plans/2026-04-11-weightflow-foundation.md` | Ready |
| All Screens (9 tasks) | `docs/plans/2026-04-11-weightflow-screens.md` | Ready (after Phase 1) |
| TDD execution order | `docs/plans/2026-04-12-tdd-order.md` | Ready |
| Phase 3-5 | TBD | Not yet written |

## Pre-Written Tests (71 failing, waiting for Android project)
- `app/src/test/java/com/weightflow/domain/WeightConverterTest.kt` (15 tests)
- `app/src/test/java/com/weightflow/domain/GoalProgressCalculatorTest.kt` (14 tests)
- `app/src/test/java/com/weightflow/domain/BadgeEngineTest.kt` (22 tests)
- `app/src/test/java/com/weightflow/domain/CsvParserTest.kt` (12 tests)
- `app/src/test/java/com/weightflow/domain/CsvExporterTest.kt` (8 tests)

## Environment
- **Open from:** `C:\Users\vaibh\Desktop\102\WeightFlow\`
- **Agents:** 35 in `.claude/agents/`
- **Skills:** 18 total in `.claude/skills/`
- **Plugins (active):** kotlin-lsp | swift-lsp | feature-dev | code-review | pr-review-toolkit | commit-commands | code-simplifier | hookify | claude-md-management | security-guidance | session-report | frontend-design
- **Codex CLI:** `codex` v0.118.0 installed globally
- **CLI Priority:** `gh` > github MCP | `codex` > agent for isolation | `npx skills` for skill mgmt | `./gradlew` for builds
- **CI:** `.github/workflows/android.yml` ready

## Open Items

- [ ] Create Android Studio project (Empty Activity, package com.weightflow, min SDK 26)
- [ ] Execute Plan 1 (Foundation) starting with `./gradlew testDebugUnitTest` to verify 71 red tests
- [ ] Run `compliance-auditor` agent before first build (GDPR checklist)
- [ ] Run `legal-advisor` agent to draft Privacy Policy (Phase 4)

## Critical Before First Build
- [ ] Back up Android keystore to 3 locations (CRITICAL)
- [ ] Add Firebase Crashlytics in Phase 1 (not later)
- [ ] Age gate (13+) in onboarding (COPPA)
- [ ] Privacy policy live URL before Phase 4 (GitHub Pages)

## Next Session Should
1. Open Android Studio -> New Project -> Empty Activity
   - Package: `com.weightflow`, Min SDK: 26, Language: Kotlin, Build: Gradle KTS
2. Run `./gradlew testDebugUnitTest` -> verify all 71 tests fail (RED)
3. Execute issue #2 (Android project setup + CI) following TDD order in `docs/plans/2026-04-12-tdd-order.md`
4. Use `codex` CLI for Room/DataStore boilerplate in issues #3 and #4
