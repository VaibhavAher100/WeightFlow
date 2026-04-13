---
name: completion-verification
enabled: true
event: stop
pattern: .*
---

**Completion check — do not stop without evidence**

Before marking any step complete or ending your response with a success claim, verify all of the following:

**Tests:**
- [ ] Run `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew testDebugUnitTest` and paste the `BUILD SUCCESSFUL` line + task count
- [ ] If data-layer code changed, also run `compileDebugAndroidTestKotlin` (instrumented compile check)

**Plan alignment:**
- [ ] The work done matches the current TDD step in `docs/plans/2026-04-12-tdd-order.md`
- [ ] No RFC has been violated (check `logs/_decisions.md` or CLAUDE.md Architecture RFCs section)

**State:**
- [ ] If this is session-end, `logs/_state.md` is up to date with what was built and what is next

**Do not say "done", "complete", "finished", "all tests pass", or express satisfaction without pasting BUILD SUCCESSFUL output from a fresh run in this session.**
