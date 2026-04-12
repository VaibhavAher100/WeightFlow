# Issue 17: Profile screen + Settings (all items)

## Parent PRD
docs/specs/2026-04-12-weightflow-master-prd.md

## What to build
Profile screen showing personal dashboard + badge collection placeholder. Settings screen (pushed from Profile) with all preference items, data management, and About section.

End-to-end: user opens Profile → sees name, current weight, goal, badge grid (locked badges greyed out). Taps Settings → sees all sections. Changes unit to lbs → all weights throughout app update immediately.

## Acceptance criteria

**Profile screen:**
- [ ] Displays: display name (editable inline), current weight in user's unit, goal weight, badge grid (12 badges — earned highlighted, unearned greyed)
- [ ] "Settings" button navigates to Settings screen

**Settings screen:**
- [ ] Preferences section: unit picker (kg/lbs/st), reminder toggle + time picker
- [ ] Display section: theme picker (8 colour swatches), dark/light/system toggle
- [ ] Data section: "Export CSV" (triggers export flow — file save dialog), "Import CSV" (navigates to import flow — stubbed, full UI in issue #21), "Delete all data" (double-confirmation dialog)
- [ ] About section: app version, "What's new" (links to CHANGELOG), Privacy Policy (opens URL), Open Source Licences, Donation links (Ko-fi, Liberapay, GitHub Sponsors), "Rate this app" (opens Play Store listing)
- [ ] Unit change: all existing displayed weights update immediately via DataStore Flow
- [ ] Delete all data: wipes Room + resets DataStore (except unit preference) + navigates to onboarding
- [ ] `ProfileViewModel` + `SettingsViewModel`: StateFlow + event handlers
- [ ] RTL-safe; all interactive elements have content descriptions

## Blocked by
- Blocked by #6 (BadgeEngine — needed for badge grid)
- Blocked by #7 (CsvParser — needed for import stub)
- Blocked by #8 (CsvExporter — needed for export)
- Blocked by #9 (repository layer)
- Blocked by #11 (NavGraph shell)

## User stories addressed
56–68 (all Profile + Settings stories), 83–85 (Pro/donation links).
