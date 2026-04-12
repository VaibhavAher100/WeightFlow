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
| Language | Kotlin 2.x |
| UI | Jetpack Compose (BOM 2024.09+) |
| DI | Manual via `WeightFlowApp.kt` Application class — no Hilt |
| State | `StateFlow` + `collectAsStateWithLifecycle()` |
| DB | Room 2.6.1 (offline-first, single source of truth) |
| Prefs | DataStore 1.1.1 via `UserPrefsDataStore` wrapper |
| Charts | Vico 1.13.1 (`compose-m3`) |
| Navigation | 5-tab bottom nav: Home · Trends · Log · History · Profile |
| Settings | Accessed from Profile — not a 6th tab |
| Min SDK | API 26 (Android 8.0) |
| Tests | Unit (JVM) + Instrumented (Room DAOs) |

---

## Build Commands

```bash
# Run from WeightFlow/ after Android Studio project is created
./gradlew assembleDebug               # build debug APK
./gradlew testDebugUnitTest           # all unit tests
./gradlew connectedAndroidTest        # instrumented tests (needs device/emulator)
./gradlew testDebugUnitTest --tests "com.weightflow.ui.util.WeightConverterTest"
./gradlew lintDebug                   # lint
./gradlew assembleRelease             # signed release build (Phase 4)
```

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

## Phases & Status

| Phase | Description | Status |
|-------|-------------|--------|
| 0 | Infrastructure (env, agents × 140, skills × 18, conventions, strategy) | ✅ Complete |
| 1 | Foundation (Android project + Room + DataStore + NavGraph) | ⏳ Plan ready — needs Android Studio |
| 2 | All 6 screens + ViewModels + Vico charts | ⏳ Plan ready — blocked on Phase 1 |
| 3 | Onboarding + polish + empty/error states | 🔲 Needs brainstorm |
| 4 | Play Store launch (privacy policy, Crashlytics, ASO, signed build) | 🔲 Needs planning |
| 5 | Firebase sync + AdMob + iOS via KMP | 🔲 Needs planning |

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
