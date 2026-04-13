---
name: session-start-plan-check
enabled: true
event: prompt
conditions:
  - field: user_prompt
    operator: regex_match
    pattern: continue|let's start|next step|what's next|pick up|resume|carry on|begin
---

**Session start detected — plan check required before coding**

Before writing any code this session, confirm you have read (in this conversation, not from memory):

1. **`logs/_state.md`** — current phase status, open items, what was last built
2. **`docs/plans/2026-04-12-tdd-order.md`** — current TDD step and definition of done
3. **CLAUDE.md Architecture RFCs** — locked decisions that override plan examples

Cross-check:
- Does the work you are about to do match the next open step in the TDD order?
- Have you checked which RFCs apply to this step?
- Are all previous steps actually GREEN (run `testDebugUnitTest` to confirm, don't rely on memory)?

**Do not start writing code until you have read these files in the current conversation.**
