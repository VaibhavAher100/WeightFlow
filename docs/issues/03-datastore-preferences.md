# Issue 3: DataStore preferences wrapper

## Parent PRD
docs/specs/2026-04-12-weightflow-master-prd.md

## What to build
A `UserPrefsDataStore` wrapper around DataStore Preferences that exposes all user settings as typed Flows. This is the source of truth for unit preference, theme, reminder config, and onboarding state.

End-to-end: a ViewModel can collect `unitPref` as a Flow and display weights in the user's chosen unit. Changing the unit in Settings immediately updates all observing screens.

## Acceptance criteria
- [ ] `UserPrefsDataStore` wraps DataStore Preferences (not Proto, for simplicity)
- [ ] Exposes as `Flow`: `unitPref` (WeightUnit enum: KG/LBS/ST), `themePref` (ThemeAccent enum, 8 values), `displayMode` (DARK/LIGHT/SYSTEM), `reminderEnabled` (Boolean), `reminderHour` (Int), `reminderMinute` (Int), `onboardingComplete` (Boolean)
- [ ] Suspend write functions: `setUnit`, `setTheme`, `setDisplayMode`, `setReminder`, `setOnboardingComplete`
- [ ] Default values: unit=KG, theme=LIME, displayMode=SYSTEM, reminderEnabled=false, reminderHour=8, reminderMinute=0, onboardingComplete=false
- [ ] Injected via `WeightFlowApp` manual DI root

## Blocked by
- Blocked by #1 (project setup)

## User stories addressed
60 (unit change), 61 (theme), 62 (dark/light/system), 63 (reminder toggle), and onboarding flow (stories 1–7).
