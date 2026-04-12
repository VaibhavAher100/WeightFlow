# Issue 12: Onboarding flow (age gate → unit → weight → goal)

## Parent PRD
docs/specs/2026-04-12-weightflow-master-prd.md

## What to build
The 4-screen onboarding flow shown only on first launch. Writes unit + current weight + goal to DataStore/Room, sets `onboardingComplete=true`, and optionally configures a daily reminder.

End-to-end: new user opens app → sees age gate → picks unit → enters current weight → sets goal weight → optionally enables reminder with time picker → arrives at Home screen with their data already populated.

## Acceptance criteria
- [ ] Screen 1 — Age gate: "I confirm I am 13 or older" checkbox + Continue button; Continue disabled until checked
- [ ] Screen 2 — Unit picker: kg / lbs / st selector; default kg; large touch targets
- [ ] Screen 3 — Current weight: number pad entry in selected unit; validates > 0
- [ ] Screen 4 — Goal weight (optional): number pad + "Skip for now" option; optional reminder toggle with time picker (default off, 08:00)
- [ ] On completion: current weight written to Room as first `WeightEntry`; goal written to `UserProfile`; unit written to DataStore; `onboardingComplete=true` written to DataStore
- [ ] Back navigation works between screens 2–4; Screen 1 has no back (exit app)
- [ ] `OnboardingViewModel`: `StateFlow<OnboardingUiState>` + event handler
- [ ] RTL-safe, all interactive elements have content descriptions
- [ ] `POST_NOTIFICATIONS` permission requested only if user enables reminder on Screen 4

## Blocked by
- Blocked by #9 (repository layer)
- Blocked by #11 (NavGraph shell)

## User stories addressed
1–7 (all onboarding stories), 6 (reminder opt-in), 75–77 (notification setup path).
