# Issue 11: NavGraph shell + bottom nav scaffold

## Parent PRD
docs/specs/2026-04-12-weightflow-master-prd.md

## What to build
The single-activity navigation skeleton: NavHost with 4 bottom-nav destinations, a persistent FAB, and the onboarding gate. Placeholder composables for each destination.

End-to-end: app launches, checks `onboardingComplete` from DataStore — if false, shows OnboardingNavGraph; if true, shows main ShellScreen with 4 tabs and a lime FAB. Tapping any tab navigates correctly.

## Acceptance criteria
- [ ] `Screen` sealed class: Home, Trends, History, Profile (each with route string + icon + label)
- [ ] `ShellScreen`: `Scaffold` with `NavigationBar` (4 items) + persistent `FloatingActionButton` (accent colour, "+" icon)
- [ ] `NavGraph`: `NavHost` with destinations for all 4 screens + Settings + Onboarding sub-graph
- [ ] Onboarding gate: if `onboardingComplete=false`, NavHost starts at onboarding; else at Home
- [ ] FAB visible on all 4 main tabs; hidden on Settings and Onboarding screens
- [ ] Active tab highlighted with accent colour; inactive tabs in text-secondary
- [ ] `MainActivity` is single activity, hosts NavHost, applies `WeightFlowTheme`
- [ ] RTL-safe layout throughout
- [ ] All placeholder destinations show correct screen title text only (content added in later issues)

## Blocked by
- Blocked by #10 (theme system)

## User stories addressed
Foundation for all screen navigation. Directly: onboarding gate (story 7), tab navigation implied throughout.
