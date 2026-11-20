# ADR-013: Free Forever, On-Device Only

- **Status:** Accepted
- **Date:** 2026-11-20
- **Supersedes:** the "Athlete Pro ~$2.99 one-time unlock", the AdMob banner and the Firebase cloud backup in `docs/strategy/product-strategy.md`.

## Context

Zero and WeightFit both charge for features that run on the phone and cost the developer nothing per user (see `docs/strategy/competitor-gap-analysis.md`):

- WeightFit locks notes, progress photos, the goal forecast, plateau alerts, reports, the PDF report, the app lock and the widget behind Premium.
- Zero puts a subscription sales page in the main navigation. Reviews report a forced trial for new accounts.
- Both apps send data to third parties (Firebase Analytics in WeightFit; 10+ marketing and analytics SDKs in Zero).

The developer decided that people must get these features free, with no data leaving the device.

## Decision

1. **Every feature is free.** WeightFlow has no paid tier, no Pro unlock, no subscription, no trial and no ads.
2. **Data stays on the device.** The app has no INTERNET permission, no account, no cloud backup, no analytics and no crash-report SDK.
3. **The user controls every copy of the data.** Data leaves the app only when the user exports a file or turns on Health Connect sync. Health Connect is an on-device store.
4. **Voluntary donations are allowed.** A donation link in Settings is the only revenue. It never blocks or limits a feature.
5. **A privacy gate runs before every release.** Craft Anything reads the release APK and confirms: no `android.permission.INTERNET`, no `com.google.android.gms.permission.AD_ID`, and no analytics, ads or marketing SDK classes.

## Consequences

- Google Play Billing, RevenueCat, AdMob and Firebase are out of scope. The `payment-integration` agent is not needed for v1.
- Crash data comes only from Play Console vitals (Android system reports). The Crashlytics plan in `CLAUDE.md` is cancelled.
- Backup across phones depends on user export and import. The import path must be reliable and tested (see the plan `docs/plans/2026-11-20-free-feature-parity.md`).
- Downloadable Google Fonts use the Google Play services font provider, not the app's network access. This is allowed because the app itself sends no data.
- Phase 5 "Firebase sync" needs a new ADR before any work starts.
