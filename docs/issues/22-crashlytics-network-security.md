# Issue 22: Firebase Crashlytics + network security config

## Parent PRD
docs/specs/2026-04-12-weightflow-master-prd.md

## What to build
Firebase project setup, Crashlytics integration, and network security hardening. Must be done in Phase 1 — retrofitting later is painful.

End-to-end: app crashes in the wild → Crashlytics dashboard shows crash report with stack trace within 5 minutes. All network calls blocked from using cleartext HTTP by config.

## Acceptance criteria
- [ ] Firebase project created and `google-services.json` added to `app/`
- [ ] Firebase Crashlytics dependency added; `FirebaseCrashlytics.getInstance()` initialised in `WeightFlowApp`
- [ ] Crashlytics reports non-fatal exceptions via `recordException()` in Repository error handling
- [ ] `network_security_config.xml` explicitly sets `cleartextTrafficPermitted="false"` for all domains
- [ ] `AndroidManifest.xml` references network security config via `android:networkSecurityConfig`
- [ ] `google-services.json` added to `.gitignore` (contains API keys — never commit)
- [ ] Crashlytics verified working: test crash in debug build appears in Firebase console

## Blocked by
- Blocked by #1 (project setup)

## User stories addressed
No direct user story — operational requirement. Enables crash visibility for all user stories in production.
