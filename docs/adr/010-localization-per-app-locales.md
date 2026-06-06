# 10. Localization via AppCompat per-app locales

Date: 2026-06-06
Status: Accepted

## Context
WeightFlow ships in English and German (Stream 2). PRD story 81 (German at launch)
was dropped between plan and implementation; this restores it as a full EN/DE
localization with an in-app language picker, locale-correct number/date formatting,
and translated domain-derived text (badges, BMI categories, coaching lines).

Two approaches were considered:
- **A. AppCompat per-app locales** (`AppCompatDelegate.setApplicationLocales` +
  `android:localeConfig` + `AppLocalesMetadataHolderService`). androidx persists the
  choice; the framework owns the picker on API 33+; the Activity is recreated on change.
- **B. Custom locale**: store a language pref in DataStore and wrap `attachBaseContext`
  with an overridden `Configuration`. More code, fights the framework, and must
  re-implement persistence and system-settings integration.

## Decision
Use **Approach A**. The selected language is owned by `AppCompatDelegate`
(no DataStore pref). A thin `ui/i18n/LocaleManager` wraps set/get; the Settings
picker calls it directly. Locales are restricted to `en`, `de` via
`androidResources.localeFilters`.

All user-facing text lives in `res/values/strings.xml` (en, default) and
`res/values-de/strings.xml` (de). A `StringCatalogParityTest` asserts identical key
sets so a missing translation fails CI. The domain layer stays Android-free:
`Badge`/`GoalState` already return stable enums, and `ui/i18n/BadgeStrings` maps
`Badge -> @StringRes` via an exhaustive `when` (compiler guards new badges).
`ui/i18n/WeightFormatter` + `ui/i18n/DateFormatters` do locale-aware number/date
formatting, taking `Locale` explicitly so they remain pure and unit-testable.

## Consequences
- **`MainActivity` must extend `AppCompatActivity`** (was `ComponentActivity`) for
  `setApplicationLocales` to apply without a manual restart. The app theme is already
  AppCompat-based (`Theme.AppCompat.DayNight.NoActionBar`), so this is compatible.
- **ViewModels that pre-format locale-dependent strings into their UiState must receive
  resolved strings reactively** (a `MutableStateFlow<XStrings?>` + `setStrings(...)`
  combined with the data flow), because ViewModels survive the Activity recreation that
  a locale change triggers — strings captured at construction would go stale. Home,
  History, and Profile use this `XStrings` pattern; Trends/Onboarding/LogEntry format in
  the Composable layer (or expose raw values) and don't need it.
- Number formatting uses `String.format(locale, "%.1f", x)` so German renders comma
  decimals (`72,6 kg`); dates use `DateTimeFormatter.ofPattern(pattern, locale)`.
- CSV export headers (`date,weight_kg`) stay English — they are data-interchange
  identifiers, not UI.
- Adding a new language is: add a `values-<lang>/strings.xml`, add the locale to
  `locales_config.xml` + `localeFilters` + the `LocaleManager.AppLanguage` enum + the
  Settings picker. The parity test enforces completeness.

## Notes / follow-ups
- A pre-existing age-gate inconsistency was surfaced (not introduced) during onboarding
  localization: the advance-gate logic enforces ≥13, the inline error text says "18",
  and a snackbar says "13". Localization preserved the current wording faithfully; the
  real minimum age needs a separate product/legal decision (`_state.md` notes India DPDP
  18+ is handled by Play Store region exclusion, not code).
