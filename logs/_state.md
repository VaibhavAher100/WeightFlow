---
last_session: 2026-04-12-004
status: active
environment: isolated (WeightFlow/ only)
---

# WeightFlow Project State

_Single source of truth. Updated every session via `/wrap`._
_Always open `WeightFlow/` in Claude Code — never the root `102/`._

---

## App Identity
- **Name:** WeightFlow | **Package:** `com.weightflow`
- **Aesthetic:** "Athlete's Journal" — warm dark (#0F0E0B) + electric lime (#C8FF00)
- **Fonts:** Bebas Neue (numbers) + Outfit (UI)
- **Position:** Genuinely free weight tracker. No subscription. Data stays on device.

## Revenue Model (Locked)
- Core: always free (CSV import/export, all charts, all themes, history, badges)
- Pro: ~$2.99 one-time (body comp, widget, cloud sync, ad removal)
- Donations: Ko-fi + Liberapay + GitHub Sponsors (link in Settings → About)
- Ads: small AdMob banner on free tier, non-core screens only

## Tech Stack (Locked)
- Kotlin 2.x + Jetpack Compose BOM 2024.09
- Room 2.6.1 · DataStore 1.1.1 · Navigation Compose 2.7.7
- Vico 1.13.1 charts · Manual DI (no Hilt) · StateFlow
- CI: GitHub Actions (tests + lint + build on every push)

## Phase Status

| Phase | Description | Status |
|-------|-------------|--------|
| 0 | Infrastructure (environment, agents, skills, conventions, product strategy) | ✅ Complete |
| 1 | Foundation (Android project + Room + DataStore + NavGraph shell) | ⏳ Plan ready — needs Android Studio |
| 2 | All 6 screens + ViewModels + Vico | ⏳ Plan ready — blocked on Phase 1 |
| 3 | Onboarding + polish + empty/error states | 🔲 Needs brainstorm |
| 4 | Play Store launch (privacy policy, Crashlytics, signed build, ASO) | 🔲 Needs planning |
| 5 | Firebase sync + AdMob + iOS via KMP | 🔲 Needs planning |

## Implementation Plans

| Plan | File | Status |
|------|------|--------|
| Foundation (7 tasks) | `docs/plans/2026-04-11-weightflow-foundation.md` | Ready |
| All Screens (9 tasks) | `docs/plans/2026-04-11-weightflow-screens.md` | Ready (after Phase 1) |
| Phase 3–5 | TBD | Not yet written |

## Environment
- **Open from:** `C:\Users\vaibh\Desktop\102\WeightFlow\`
- **Agents:** 35 in `.claude/agents/` — curated from VoltAgent/awesome-claude-code-subagents (105 irrelevant removed)
- **Skills:** 18 total in `.claude/skills/`
  - 4 original: hook-development, writing-rules, claude-automation-recommender, claude-md-improver
  - 14 marketplace: mobile-android-design, android-kotlin, android-clean-architecture, android-jetpack-compose, android-expert, android-room-database, qa-testing-mobile, testing-accessibility, gplay-gradle-build, gplay-screenshot-automation, android-playstore-setup, app-store-optimization, android-aso, privacy-policy-malik-taiar, app-analytics, crash-analytics
- **Plugins (active):** kotlin-lsp · swift-lsp · feature-dev · code-review · pr-review-toolkit · commit-commands · code-simplifier · hookify · claude-md-management · security-guidance · session-report · frontend-design
- **Codex CLI:** `codex` v0.118.0 installed globally — use for parallel boilerplate gen in Phase 1–2
- **CLI Priority:** `gh` > github MCP · `codex` > agent for isolation · `npx skills` for skill mgmt · `./gradlew` for builds
- **CI:** `.github/workflows/android.yml` ready (activate after GitHub repo created)
- **Conventions:** Conventional Commits · SemVer · ADR in `docs/adr/`

## Open Items

- [ ] Write master spec doc → `docs/specs/2026-04-12-weightflow-master-plan.md`
- [ ] Set up GitHub repo + push WeightFlow/ (enables CI)
- [ ] Create Android Studio project at `C:\Users\vaibh\Desktop\102\WeightFlow\`
- [ ] Execute Plan 1 (Foundation)
- [ ] Execute Plan 2 (Screens)
- [ ] Run `compliance-auditor` agent before first build (GDPR checklist)
- [ ] Run `legal-advisor` agent to draft Privacy Policy (needed for Phase 4)

## Critical Before First Build
- [ ] Back up Android keystore to 3 locations (CRITICAL — losing it = can never update app)
- [ ] Add Firebase Crashlytics in Phase 1 (not later)
- [ ] Age gate (13+) in onboarding (COPPA)
- [ ] Privacy policy live URL before Phase 4 (GitHub Pages)

## Next Session Should
1. Write master spec doc → `docs/specs/2026-04-12-weightflow-master-plan.md`
2. User review spec → start Plan 1 execution
3. Use `codex` CLI for Room/DataStore boilerplate generation in Plan 1 Task 2–4
4. Activate `kotlin-lsp` plugin for Kotlin IDE intelligence once `app/` module exists
