# CLAUDE.md — WeightFlow

This file provides guidance to Claude Code when working inside the WeightFlow project directory.
This is an **isolated environment** — do not reference or modify anything outside `WeightFlow/`.

---

## Project Identity

- **App name:** WeightFlow
- **Package:** `com.weightflow`
- **Platform:** Android (Kotlin + Jetpack Compose) → iOS via KMP in Phase 5
- **Goal:** Free, beautiful weight tracker. No subscription. Data stays on device.
- **Play Store target:** ~$2.99 one-time Pro unlock. Core features always free.

---

## Where Everything Lives

```
WeightFlow/
├── .claude/
│   ├── agents/          ← 35 project-local agents (WeightFlow-relevant only)
│   ├── skills/          ← 18 skills (4 original + 14 marketplace, symlinked from .agents/skills/)
│   └── commands/        ← wrap.md session command
├── .agents/skills/      ← marketplace skills source (managed by npx skills)
├── .github/workflows/   ← android.yml CI (tests + lint + build on every push)
├── docs/
│   ├── strategy/        ← product-strategy.md, agents-and-skills.md, blindspots.md
│   ├── plans/           ← Plan 1 (Foundation), Plan 2 (Screens) — ready to execute
│   ├── specs/           ← design specs (brainstorming outputs)
│   ├── adr/             ← Architecture Decision Records
│   └── privacy/         ← privacy policy + ToS (required before Phase 4)
├── logs/
│   ├── _state.md        ← current project state (source of truth)
│   ├── _decisions.md    ← architecture decisions log
│   └── sessions/        ← per-session logs
├── mockups/
│   └── app-design.html  ← approved 5-screen design gallery
├── app/                 ← Android app module (created by Android Studio in Phase 1)
├── CHANGELOG.md
├── .gitignore
└── CLAUDE.md            ← this file
```

---

## Architecture (Locked — Do Not Revisit)

| Decision | Choice |
|----------|--------|
| Language | Kotlin 2.1.20 |
| UI | Jetpack Compose (BOM 2025.04.01) |
| DI | Manual via `WeightFlowApp.kt` Application class — no Hilt |
| State | `StateFlow` + `collectAsStateWithLifecycle()` |
| DB | Room **2.7.0** with KSP 2.1.20-1.0.31 (offline-first, single source of truth; 2.6.1 not KSP2-compatible) |
| Prefs | DataStore 1.1.1 via `UserPrefsDataStore` wrapper |
| Charts | Vico 1.13.1 (`compose-m3`) |
| Navigation | 4-tab bottom nav + FAB: Home · Trends · History · Profile (no Log tab) |
| Settings | Accessed from Profile — not a 6th tab |
| Min SDK | API 26 (Android 8.0) |
| Tests | Unit (JVM) + Instrumented (Room DAOs + Repositories) — mockk 1.13.12 + turbine 1.1.0 |
| AGP | 9.1.0 + Gradle 9.3.1 (AGP 9.x has built-in Kotlin — see Build Gotchas below) |
| Room | **2.7.0** (upgraded from 2.6.1 — 2.6.1 is not KSP2-compatible with Kotlin 2.x) |
| Fonts | `ui-text-google-fonts` (Compose BOM) — Bebas Neue (display/headline) + Outfit (body/label) via GMS provider; `res/values/font_certs.xml` must exist (not auto-bundled) |

---

## Build Commands

```bash
# Run from WeightFlow/ — prepend JAVA_HOME if java not in PATH
# JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew assembleDebug               # build debug APK
./gradlew testDebugUnitTest           # all unit tests (74 currently — verified 2026-04-13)
./gradlew connectedAndroidTest        # instrumented tests (needs device/emulator)
./gradlew testDebugUnitTest --tests "com.weightflow.domain.WeightConverterTest"
./gradlew lintDebug                   # lint
./gradlew assembleRelease             # signed release build (Phase 4)
```

---

## AGP 9.x Build Gotchas (CRITICAL — do not re-learn these)

AGP 9.1.0 changed how Kotlin integrates. These are non-obvious and will cost an hour to debug again.

| Issue | Symptom | Fix |
|-------|---------|-----|
| DO NOT apply `kotlin-android` plugin | "Cannot add extension with name 'kotlin'" at configuration | AGP 9.x has built-in Kotlin. Apply only `kotlin-compose` + `ksp` |
| KSP source sets conflict | "Using kotlin.sourceSets DSL is not allowed with built-in Kotlin" | Add `android.disallowKotlinSourceSets=false` to `gradle.properties` |
| Room 2.6.1 + KSP2 | "unexpected jvm signature V" then `@Override` mismatch in generated Java | **Use Room 2.7.0** — 2.6.1 predates KSP2/K2. `ksp.useKSP2=false` breaks AGP 9.x. |
| `@Delete`/`@Upsert` + `suspend` Unit return | Same KSP2 jvm signature V error | Return `Int`/`Long` from these methods — also better API (rows affected / row id) |
| XML theme needs AppCompat | Resource link fails with MDC theme | Use `Theme.AppCompat.DayNight.NoActionBar` — AppCompat is transitive via `activity-compose` |
| `kotlinOptions` not available | "Unresolved reference 'kotlinOptions'" | Not needed — removed; JVM target controlled via `compileOptions` |
| `R.array.com_google_android_gms_fonts_certs` unresolved | "Unresolved reference 'array'" when using `ui-text-google-fonts` | Create `res/values/font_certs.xml` manually — the AAR does not auto-bundle certs in this build config |
| `java` not in shell PATH | "JAVA_HOME is not set" | Use `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"` prefix |

---

## CLI Priority (Always Prefer CLI Over MCP)

Use CLIs first — faster, deterministic, offline-capable. Fall back to MCP only when no CLI exists.

| Task | Use CLI | Over |
|------|---------|------|
| All GitHub ops (PRs, issues, CI, releases) | `gh` CLI | github MCP |
| Parallel code generation / boilerplate | `codex` CLI (`codex-cli 0.118.0`) | — |
| Skill discovery + install | `npx skills search/add` | — |
| Android build + test + lint | `./gradlew …` | — |
| Play Store upload / screenshots | `gplay-gradle-build` skill | — |
| Git commits (conventional) | `git-commit` skill | — |
| Web scraping / crawling | `firecrawl` CLI | playwright MCP |
| Notion read/write | Notion MCP | — (no CLI) |
| Context docs | `context7` MCP | — (no CLI) |

---

## Agents Available (Project-Local, 35 total)

All agents live in `.claude/agents/`.

| Phase | Agent | Use For |
|-------|-------|---------|
| 0 | `compliance-auditor` | GDPR/COPPA audit before coding |
| 0 | `legal-advisor` | Privacy policy + ToS drafting |
| 0 | `kotlin-specialist` | All Kotlin/Compose/Room/KMP implementation |
| 1–2 | `mobile-app-developer` | Android-specific patterns, AAB, Play targets |
| 1–2 | `mobile-developer` | Cross-platform patterns, offline-first |
| 1–2 | `java-architect` | JVM patterns, Room migration safety |
| 1–2 | `build-engineer` | Gradle KTS, R8 proguard, APK budget |
| 1–2 | `database-administrator` | Room schema design, query tuning |
| 1–2 | `qa-expert` | Android unit + instrumented test strategy |
| 1–2 | `test-automator` | Automated test framework setup |
| 1–2 | `accessibility-tester` | Content descriptions, contrast, TalkBack |
| 1–2 | `security-auditor` | Data storage, keystore, DataStore encryption |
| 1–2 | `devops-engineer` | GitHub Actions CI/CD pipeline |
| 1–2 | `git-workflow-manager` | Branching strategy, conventional commits |
| 1–2 | `performance-engineer` | APK size, startup time, jank, Baseline Profile |
| 1–2 | `ui-designer` | Compose UI, "Athlete's Journal" aesthetic |
| 2–3 | `debugger` + `error-detective` | Crash diagnosis, stack traces |
| 2–3 | `refactoring-specialist` | Code cleanup without behaviour change |
| 3 | `ux-researcher` | Onboarding flow validation, empty-state UX |
| 3–4 | `architect-reviewer` | Architecture review before Phase gate |
| 4 | `content-marketer` | Store listing copy, screenshots, feature graphic |
| 4 | `seo-specialist` | Play Store keyword / ASO optimisation |
| 4 | `payment-integration` | Google Play Billing v6 + Apple IAP |
| 4+ | `documentation-engineer` | User-facing docs, in-app help |
| 5 | `swift-expert` | iOS native + KMP migration |
| 5 | `cloud-architect` | Firebase architecture, Firestore schema |
| Any | `code-reviewer` | Post-implementation review (or use `code-review` plugin) |
| Any | `competitive-analyst` | Competitive intelligence |
| Any | `market-researcher` | Market research |
| Any | `dependency-manager` | Dependency updates, vulnerability audit |

---

## Skills Available

### Project-local originals (`.claude/skills/`)
| Skill | When |
|-------|------|
| `hook-development` | Creating project hooks (TDD enforcer, session wrap trigger) |
| `writing-rules` | Hookify rules (prevent bad patterns) |
| `claude-automation-recommender` | After Phase 1 — audit what to automate |
| `claude-md-improver` | Keep this CLAUDE.md accurate as we build |

### Marketplace skills — Android & CI (`.claude/skills/` → symlinked)
| Skill | Phase | Source |
|-------|-------|--------|
| `mobile-android-design` | 1–2 | wshobson — 9.4K installs |
| `android-kotlin` | 1–2 | alinaqi — 1.3K installs |
| `android-clean-architecture` | 1–2 | affaan-m — 1.2K installs |
| `android-jetpack-compose` | 1–2 | thebushidocollective |
| `android-expert` | 1–2 | oimiragieo |
| `android-room-database` | 1 | krutikjain |
| `qa-testing-mobile` | 2–3 | vasilyu1983 |
| `testing-accessibility` | 3 | wojons |
| `gplay-gradle-build` | 4 | tamtom |
| `gplay-screenshot-automation` | 4 | tamtom |
| `android-playstore-setup` | 4 | hitoshura25 |
| `app-store-optimization` | 4 | sickn33 — 961 installs |
| `android-aso` | 4 | eronred |
| `privacy-policy-malik-taiar` | 4 | lawvable |
| `app-analytics` | 4–5 | eronred |
| `crash-analytics` | 4–5 | eronred |

### Global superpowers skills (always available via Skill tool)
`brainstorming` · `writing-plans` · `test-driven-development` · `subagent-driven-development` · `executing-plans` · `verification-before-completion` · `systematic-debugging` · `requesting-code-review` · `frontend-design` · `copywriting`

### Other global skills (always available)
`gh-cli` · `git-commit` · `git-guardrails-claude-code` · `prd-to-issues` · `grill-me` · `simplify` · `improve-codebase-architecture` · `critique` · `design-an-interface`

---

## Plugins Available (project context)

| Plugin | What it does | When |
|--------|-------------|------|
| `kotlin-lsp` | Kotlin language server — IDE-grade intelligence for `.kt` files | Always active |
| `swift-lsp` | Swift language server | Phase 5 |
| `feature-dev` | 3-agent pipeline: code-explorer → code-architect → code-reviewer | Every feature |
| `code-review` | Automated PR review via `gh` CLI + confidence scoring | Pre-merge |
| `pr-review-toolkit` | code-reviewer + code-simplifier agents | Post-implementation |
| `commit-commands` | Commit → push → open PR in one command | Every commit |
| `code-simplifier` | Simplify and refine code after writing | After every task |
| `hookify` | Create hooks to prevent bad patterns | Phase 1 setup |
| `claude-md-management` | Update CLAUDE.md with session learnings | Every /wrap |
| `security-guidance` | Security review guidelines | Phase 4 audit |
| `session-report` | HTML usage/token report from session transcripts | Any time |
| `frontend-design` | Production-grade UI generation | Phase 2–3 |

---

## OpenAI Codex CLI Integration

**Binary:** `codex` (v0.118.0, installed globally at `~/.npm/bin/codex`)

Codex is a headless coding agent that runs sandboxed tasks in parallel with Claude Code. Use it for **isolated, well-scoped generation tasks** where you want to parallelize work.

| Phase | Use Codex for | Command pattern |
|-------|--------------|-----------------|
| 1 | Generate Room entities, DAOs, migrations boilerplate | `codex "create WeightEntryEntity + DAO + migration for Room 2.6.1"` |
| 1 | Generate DataStore wrapper + repository stubs | `codex "create UserPrefsDataStore wrapper using DataStore 1.1.1"` |
| 2 | Scaffold all 6 ViewModels (shell only, no logic) | `codex "scaffold 6 Compose ViewModels with StateFlow UiState for WeightFlow"` |
| 2 | Generate Vico chart composables | `codex "create Vico 1.13.1 line chart composable for weight trend data"` |
| Any | Second-pass investigation when Claude is stuck | `codex:rescue` skill |
| Any | Refactor a specific file in isolation | `codex "refactor X to use Y pattern"` |

**When NOT to use Codex:** Architecture decisions, multi-file coordination, anything touching NavGraph wiring or DI root — those need Claude Code's full context.

---

## The Build Loop (Every Phase)

```
1. brainstorm (if design needed) — superpowers:brainstorming skill
2. writing-plans → task plan — superpowers:writing-plans skill
3. test-driven-development → write failing test FIRST — superpowers:test-driven-development
4. [Optional] codex CLI for boilerplate generation in parallel
5. subagent-driven-development OR executing-plans
6. verification-before-completion
7. requesting-code-review (kotlin-specialist + code-review plugin + security-auditor)
8. /wrap → update logs/_state.md + session log + this CLAUDE.md
9. phase-gate: all tests green + agents approve → advance
```

---

## GitHub Repo

- URL: `https://github.com/VaibhavAher100/WeightFlow` (private)
- Rename before launch (current name is temporary)
- PRD: issue #1 | Slice issues: #2-#23 | Architecture RFCs: #24-#29

---

## Architecture RFCs (implement during Phase 1-2)

These are locked design decisions from the improve-codebase-architecture session. Implement them when building the relevant modules -- do not skip.

| RFC | Issue | What it decides |
|-----|-------|----------------|
| BadgeObserver | #24 | Reactive combine() over Room Flows, not imperative on insert |
| CsvImporter | #25 | Single entry point, CsvFormat enum surfaced in ParseResult |
| GoalStateMachine | #26 | Sealed FSM with transition functions, rehydrated on cold start |
| StoredWeight + HomeUiStateMapper | #27 | @JvmInline value class for Room + single conversion point in ViewModel |
| SortedEntries + named Repository methods | #28 | Type-safe ordering contract, delete getAllEntries() |
| HomeDataAggregator | #29 | ViewModel gets one dependency instead of five |

---

## Tests (141 unit + 37 instrumented — all GREEN/compile-verified)

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

**ViewModel test pattern (locked — reuse for all future VMs):**
- `StandardTestDispatcher` + `Dispatchers.setMain/@Before`
- `awaitRealState()` turbine helper — skips the initial `Loading` from `stateIn(WhileSubscribed)`
- `every { repo.flowProp } returns MutableStateFlow(...)` for Flow properties (not `coEvery`)
- Synchronous VM actions (`onWeightInput`, `onNextStep`): no `advanceUntilIdle()` needed
- Coroutine-launching actions (`onSave`, `onComplete`): call `advanceUntilIdle()` after

**Instrumented tests** — `app/src/androidTest/java/com/weightflow/data/` (need device/emulator):

| File | Tests | Status |
|------|-------|--------|
| `WeightEntryDaoTest.kt` | 7 | Compile-verified |
| `UserProfileDaoTest.kt` | 5 | Compile-verified |
| `UserPrefsDataStoreTest.kt` | 7 | Compile-verified |
| `WeightRepositoryTest.kt` | 11 | Compile-verified |
| `UserProfileRepositoryTest.kt` | 6 | Compile-verified |

Note: `WeightEntry` and `UserProfile` are plain domain data classes. Room entities live in `data/` — domain stays pure. Data package is flat (`data/`) — no subdirs (intentional).

TDD execution order: `docs/plans/2026-04-12-tdd-order.md`

---

## Phases & Status

| Phase | Description | Status |
|-------|-------------|--------|
| 0 | Infrastructure (env, agents x 35, skills x 18, PRD, 29 GitHub issues) | Complete |
| 1 | Foundation (Android project + Room + DataStore + NavGraph) | **Complete** — all 11 TDD steps done; app launchable with 4-tab nav |
| 2 | All 6 screens + ViewModels + Vico charts | **In progress** — 6 VMs done (141 tests), Vico + OnboardingScreen + onboarding gate remain |
| 3 | Onboarding + polish + empty/error states | Needs brainstorm |
| 4 | Play Store launch (privacy policy, Crashlytics, ASO, signed build) | Needs planning |
| 5 | Firebase sync + AdMob + iOS via KMP | Needs planning |

---

## Guardrails (Active Hookify Rules)

Three rules in `.claude/hookify.*.local.md` — active immediately, no restart needed:

| Rule file | Event | What it enforces |
|-----------|-------|-----------------|
| `hookify.tdd-production-guard.local.md` | file | Writing `app/src/main/java/**/*.kt` → confirm failing test exists first |
| `hookify.completion-verification.local.md` | stop | Session stop → paste `BUILD SUCCESSFUL` output before any completion claim |
| `hookify.session-start-plan-check.local.md` | prompt | "continue/next step" → read `logs/_state.md` + TDD plan before coding |

**Session start protocol (enforced by hook):**
1. Read `logs/_state.md` — confirm current open items
2. Read `docs/plans/2026-04-12-tdd-order.md` — confirm which TDD step is next
3. Run `./gradlew testDebugUnitTest` — confirm all 141 unit tests still green
4. THEN start coding

---

## Critical Reminders (Do Not Forget)

1. **Keystore**: Back up the Android keystore to 3 places before first release build.
2. **Privacy policy**: Must be a live URL before Play Store submission (Phase 4). → `docs/privacy/`
3. **Room migrations**: Every schema change needs a Migration object. Never skip.
4. **COPPA**: Age gate (13+) required in onboarding.
5. **APK budget**: Keep final download under 15MB. Use AAB not APK for Play Store.
6. **Crashlytics**: Add in Phase 1, not later.
7. **GitHub Actions**: `.github/workflows/android.yml` is already set up. Tests run on every push.

---

## Conventions

- **Git commits**: Conventional Commits — `feat:`, `fix:`, `test:`, `docs:`, `chore:`
- **Versioning**: Semantic Versioning — `versionName "1.0.0"`, `versionCode 1` (increment every release)
- **Branch naming**: `main` (stable) · `develop` (integration) · `feature/name` · `fix/name`
- **PR titles**: `feat(screen): add dashboard ViewModel` format
- **Architecture Decision Records**: New decisions → `docs/adr/NNN-title.md`
- **Session end**: Always run `/wrap` before closing Claude Code
