---
name: tdd-production-guard
enabled: true
event: file
conditions:
  - field: file_path
    operator: regex_match
    pattern: app/src/main/java/.*\.kt$
  - field: file_path
    operator: not_contains
    pattern: Test
---

**TDD Guard — production Kotlin file write detected**

Before writing this file, confirm:

1. A failing test already exists for this class/function
2. You have run the test and seen it fail with the expected error (not a compile error — a real assertion failure or "class not found")
3. You are writing the **minimum** code to make that test pass

If you haven't done this yet:
- STOP and write the test first
- Run `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew testDebugUnitTest` or `compileDebugAndroidTestKotlin` to confirm it fails
- THEN write production code

**Iron Law:** No production code without a failing test first. See `docs/plans/2026-04-12-tdd-order.md`.
