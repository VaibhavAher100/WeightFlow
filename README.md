# WeightFlow

![Android CI](https://github.com/VaibhavAher100/WeightFlow/actions/workflows/android.yml/badge.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white)
![Min SDK](https://img.shields.io/badge/minSdk-26-3DDC84?logo=android&logoColor=white)
![License](https://img.shields.io/badge/license-source--available-lightgrey)

Privacy-focused Android weight tracker. Local-first storage, encrypted database, and
user-controlled data. **No data collection. No accounts. No ads. No subscriptions.**

Built with Kotlin, Jetpack Compose, Room + SQLCipher, and Jetpack DataStore.

> ⚠️ **Source-available, not open-source.** This code is published for reference and
> review only — see [License](#license). External contributions are not accepted.

## Screenshots

<!-- TODO: add Home / Trends / History / Profile screenshots before Play Store launch -->
_Screenshots coming with the first release._

## Features

- Weight logging in **kg, lbs, or stones**
- Trends with **Line / Bar / Area** charts, time-range filters (7D–All), and statistics
- Goal tracking with progress, projected ETA, and a maintenance mode
- Achievement badges and logging streaks
- 8 selectable color themes (dark-first "Athlete's Journal" aesthetic)
- CSV **import** (WeightFit, Happy Scale, Apple Health, generic) and password-protected **export**
- **Encrypted** local database (SQLCipher) — data never leaves the device
- GDPR Art. 17 "delete all data" and Art. 20 data export
- Optional daily reminder (opt-in)

## Tech Stack

| Area | Choice |
|------|--------|
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM — `ViewModel` + `StateFlow` + immutable `UiState`; manual DI (no Hilt) |
| Persistence | Room 2.7.0 + SQLCipher (encrypted); DataStore for preferences |
| Charts | Vico 1.13.1 |
| Background | WorkManager (opt-in daily reminder) |
| Build | AGP 9.1.1, Gradle 9.5.1, JDK 17; detekt + ktlint; GitHub Actions CI |

## Privacy

All data stays on your device in an encrypted database. The app collects nothing and
requires no account or network connection.

Privacy policy: <https://vaibhavaher100.github.io/WeightFlow/privacy-policy/>

## Status

**Version 1.0 — under active development. Not yet released on Google Play.**

## License

Copyright (c) 2026 Vaibhav Aher. All rights reserved.

Source is publicly viewable for **reference and review only** — this is **not** open-source
software, and external contributions are **not** accepted. You may not copy, modify,
distribute, or reuse this code without explicit written permission.
See [LICENSE](LICENSE) for full terms.
