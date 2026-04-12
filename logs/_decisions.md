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

**Decision:** OpenAI Codex CLI for Phase 1–2 boilerplate
**Rationale:** Codex runs headless in a sandboxed context — ideal for isolated, pattern-fill tasks (Room entities, DAOs, ViewModel shells). Claude Code retains ownership of wiring, DI, architecture decisions.
**Context:** `codex-cli 0.118.0` already installed globally. User referenced Codex intro video.
