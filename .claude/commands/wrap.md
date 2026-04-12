# /wrap — WeightFlow Session Wrap

Close a Claude Code session for the WeightFlow app project. Execute all steps in order.
All paths are relative to the `WeightFlow/` project root.

---

## Step 1 — Determine Session ID

Scan `logs/sessions/`. Find the highest existing session ID (format: `YYYY-MM-DD-NNN`).
Use today's date + next available number. Example: if `2026-04-12-001.md` exists, use `2026-04-12-002`.

Output the session ID before continuing.

---

## Step 2 — Write Session Log

Create `logs/sessions/[SESSION_ID].md`:

```markdown
---
session_date: YYYY-MM-DD
session_id: [SESSION_ID]
summary: [one-line summary of what was accomplished]
status: complete
phase: N
phase_status: [in-progress | completed | started]
tags: [phase-N, kotlin, compose, planning, etc.]
files_changed: N
open_items: N
---

# Session [SESSION_ID] Log

## What Was Asked
[What the user asked for this session]

## Actions Taken
| Action | Output | Notes |
|--------|--------|-------|
| ... | ... | ... |

## Decisions Made
[Any architecture, product, or process decisions locked this session]

## Files Created / Modified
[List key files]

## Open Items for Next Session
- [ ] item 1
- [ ] item 2

## Reasoning
[Why things were done this way]
```

---

## Step 3 — Update `logs/_state.md`

Overwrite the file with the current accurate state. Include:
- Current phase status (Phase 0–5, which is complete/in-progress/blocked)
- Open items (carry forward any unfinished items)
- Next session priorities
- Keep the full structure from the existing file — update values, don't simplify

---

## Step 4 — Append to `logs/_decisions.md`

Only if architecture, product, or process decisions were made this session.
Append in format:

```markdown
## Session [SESSION_ID] — [date]

**Decision:** [what was decided]
**Rationale:** [why]
**Context:** [what prompted it]
```

Skip if no new decisions.

---

## Step 5 — Update `CLAUDE.md` if needed

If this session changed the architecture, added agents/skills, updated phase status, or surfaced new critical reminders — update the relevant section of `WeightFlow/CLAUDE.md`. Keep it accurate, not historical.

Use the `claude-md-improver` skill if available.

---

## Step 6 — Report Completion

Output:
```
✅ Session logged as [SESSION_ID]
📁 WeightFlow/logs/sessions/[SESSION_ID].md
📋 State updated: WeightFlow/logs/_state.md
🔒 Safe to /clear
```
