# Issue 20: Daily reminder notifications (WorkManager + deep-link)

## Parent PRD
docs/specs/2026-04-12-weightflow-master-prd.md

## What to build
WorkManager-based daily reminder system. Fires at user's chosen time, shows a notification, deep-links directly to the log bottom sheet on tap.

End-to-end: user has reminder enabled at 08:00 → at 08:00 a notification appears: "Time to weigh in 💪" → user taps notification → app opens directly to log bottom sheet.

## Acceptance criteria
- [ ] `ReminderWorker` (WorkManager `CoroutineWorker`): posts notification at scheduled time
- [ ] `ReminderScheduler`: schedules/cancels `PeriodicWorkRequest` based on DataStore `reminderEnabled` + time; rescheduled when time changes in Settings
- [ ] Notification: title "WeightFlow", body "Time to weigh in", taps deep-link to `MainActivity` with `OPEN_LOG_SHEET` intent extra
- [ ] `MainActivity` detects `OPEN_LOG_SHEET` intent extra → auto-opens log bottom sheet on launch
- [ ] `POST_NOTIFICATIONS` runtime permission: requested only when user enables reminder (in onboarding or Settings); graceful handling if denied
- [ ] Reminder cancelled immediately when user disables in Settings
- [ ] Works correctly after device reboot (WorkManager handles this natively)
- [ ] Notification channel created on app init (required for Android 8+)

## Blocked by
- Blocked by #3 (DataStore — reads reminder prefs)
- Blocked by #12 (Onboarding — first reminder setup path)

## User stories addressed
75–77 (all notification stories).
