# WeightFlow — Agents & Skills Master Map
_Last updated: 2026-04-12 | Reference for what to install, when, and why_

---

## Agents to Install (from VoltAgent catalog)

Install to `.claude/agents/` (project-local) so they don't affect global Claude.

### Phase 0 — Install Now (Before Any Code)

| Agent | Source | Why |
|-------|--------|-----|
| `compliance-auditor` | voltagent-qa-sec | GDPR/CCPA/PIPL checklist. Privacy policy review. Data handling audit before coding starts. |
| `legal-advisor` | voltagent-biz | Draft Privacy Policy + Terms of Service. Required by both app stores. |
| `kotlin-specialist` | voltagent-lang | Primary coding agent for all Kotlin/Compose/Coroutines work. |

### Phase 1–2 — Install Before Building

| Agent | Source | Why |
|-------|--------|-----|
| `qa-expert` | voltagent-qa-sec | Android test strategy: unit + instrumented + UI. Works alongside TDD skill. |
| `accessibility-tester` | voltagent-qa-sec | WCAG + Android a11y compliance. Content descriptions, font scaling, color contrast. |
| `security-auditor` | voltagent-qa-sec | Reviews data storage, network calls, keystore handling. No PII leaks. |
| `devops-engineer` | voltagent-infra | GitHub Actions CI/CD. Signed release builds. Firebase App Distribution beta. |
| `performance-engineer` | voltagent-qa-sec | APK size budget, startup time, scroll jank, battery drain. Keep APK <15MB. |

### Phase 3–4 — Install Before Polish + Launch

| Agent | Source | Why |
|-------|--------|-----|
| `content-marketer` | voltagent-biz | ASO: app title, keywords, descriptions for Google Play + App Store. |
| `seo-specialist` | voltagent-domains | App Store Optimization. 65%+ of downloads are organic search. |
| `payment-integration` | voltagent-domains | Google Play Billing API + Apple IAP. One-time Pro unlock. Local currency pricing. |

### Phase 5 — iOS Expansion

| Agent | Source | Why |
|-------|--------|-----|
| `swift-expert` | voltagent-lang | iOS native polish (when KMP doesn't cover everything). App Store submission. |
| `mobile-app-developer` | voltagent-domains | Cross-platform mobile specialist. TestFlight + KMP migration. |
| `competitive-analyst` | voltagent-research | Ongoing competitive intelligence as the market evolves. |

---

## Skills Map

### Global Skills (already installed — use as-is)

| Skill | When to Use |
|-------|-------------|
| `superpowers:brainstorming` | Start of every phase needing design decisions (Phases 0, 3, 5) |
| `superpowers:writing-plans` | After brainstorming. Converts design to task-by-task plan. |
| `superpowers:test-driven-development` | Before every feature/screen. Red → green → refactor. No exceptions. |
| `superpowers:subagent-driven-development` | Execute Plans 1 and 2. Parallel subagents for independent components. |
| `superpowers:executing-plans` | Alternative to subagent-driven for sequential execution. |
| `superpowers:verification-before-completion` | Before marking any task complete. |
| `superpowers:systematic-debugging` | When a bug or test failure occurs. |
| `superpowers:requesting-code-review` | After completing each plan. |
| `frontend-design` | Phase 3: onboarding, empty states, error states. |
| `copywriting` | Phase 4: Play Store listing, screenshots copy, release notes. |

### Meta Skills → Pull to Project-Local

These live in `~/.claude/meta-skills-archive/`. Copy to `.claude/skills/` (project-local).

| Skill | Pull Command | Why |
|-------|-------------|-----|
| `hook-development` | Copy from meta-skills-archive | Build project-local hooks: TDD enforcer, session-wrap trigger, test-gate |
| `writing-rules` | Copy from meta-skills-archive | Create hookify rules: no untested commits, no silent phase skips |
| `claude-automation-recommender` | Copy from meta-skills-archive | After Phase 1: audit what else should be automated |
| `claude-md-improver` | Copy from meta-skills-archive | Keep CLAUDE.md accurate as architecture evolves |

### Project-Local Skills (to create in `.claude/skills/`)

These don't exist yet — create them as we learn:

| Skill | When to Create | Purpose |
|-------|---------------|---------|
| `weightflow-tdd` | Phase 0 | Android-specific TDD patterns: ViewModel tests, Room tests, Compose UI tests |
| `weightflow-phase-gate` | Phase 0 | Checklist that must pass before advancing to next phase |
| `weightflow-session-wrap` | Phase 0 | Expanded wrap that also updates CLAUDE.md + memory files |

---

## Install Commands

```bash
# Install agents to project-local .claude/agents/
# Run from project root: /c/Users/vaibh/Desktop/102/

# Phase 0 agents
claude plugin install voltagent-qa-sec    # compliance-auditor, security-auditor, qa-expert, accessibility-tester, performance-engineer
claude plugin install voltagent-biz       # legal-advisor, content-marketer
claude plugin install voltagent-lang      # kotlin-specialist, swift-expert

# Phase 1-2 agents  
claude plugin install voltagent-infra     # devops-engineer, deployment-engineer

# Phase 3-4 agents
claude plugin install voltagent-domains   # payment-integration, seo-specialist, mobile-app-developer

# Research agents (useful throughout)
claude plugin install voltagent-research  # competitive-analyst, market-researcher
```

---

## The Build Loop (Every Phase)

```
brainstorm (if design needed)
    ↓
writing-plans → task-by-task plan
    ↓
test-driven-development → red test first
    ↓
subagent-driven-development OR executing-plans
    ↓
verification-before-completion
    ↓
requesting-code-review (kotlin-specialist + security-auditor)
    ↓
wrap-session → _state.md + session log + CLAUDE.md update
    ↓
phase-gate checklist (tests pass + agent reviews pass)
    ↓
next phase
```
