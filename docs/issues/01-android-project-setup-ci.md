# Issue 1: Android project setup + CI pipeline

## Parent PRD
docs/specs/2026-04-12-weightflow-master-prd.md (will be #1 on GitHub)

## What to build
Create the Android Studio project with all dependencies configured, Gradle KTS build files, and GitHub Actions CI pipeline running on every push. This is the foundation everything else builds on.

End-to-end: a developer can clone the repo, run `./gradlew assembleDebug`, and get a green build. CI runs `testDebugUnitTest` + `lintDebug` + `assembleDebug` on every push.

## Acceptance criteria
- [ ] Android Studio project created: package `com.weightflow`, min SDK 26, Kotlin DSL Gradle
- [ ] `app/build.gradle.kts` includes all dependencies: Compose BOM 2024.09+, Room 2.6.1, DataStore 1.1.1, Navigation Compose 2.7.7, Vico 1.13.1, Coroutines 1.7.3, KSP plugin
- [ ] `build.gradle.kts` (project-level) and `settings.gradle.kts` configured correctly
- [ ] `.github/workflows/android.yml` runs on every push: `testDebugUnitTest` + `lintDebug` + `assembleDebug`
- [ ] `network_security_config.xml` added, explicitly blocking cleartext HTTP
- [ ] `AndroidManifest.xml` references network security config
- [ ] `./gradlew assembleDebug` passes locally
- [ ] CI pipeline passes on first push

## Blocked by
None — can start immediately.

## User stories addressed
Foundation for all 85 user stories. No direct user-facing story.
