# Changelog

All notable changes to WeightFlow are documented here.
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).
Versioning follows [Semantic Versioning](https://semver.org/).

---

## [Unreleased]

### Added
- **German localization (de) + in-app language picker** — full EN/DE via AppCompat
  per-app locales (ADR-010). Language switch in Settings (System / English / Deutsch),
  applied live. Locale-aware weight (comma decimals), date, badge, and BMI-category
  text. New `ui/i18n/` package (LocaleManager, WeightFormatter, DateFormatters,
  BadgeStrings) keeping the domain layer Android-free. 230-key string catalog mirrored
  en/de with a parity test (keys + placeholder arity) gating CI.

### Planned
- Foundation: Room DB, DataStore, NavGraph shell (Plan 1)
- All 6 screens + ViewModels (Plan 2)
- Onboarding + first-launch flow (Plan 3)
- Play Store launch (Plan 4)
- Firebase sync + AdMob (Plan 5)
