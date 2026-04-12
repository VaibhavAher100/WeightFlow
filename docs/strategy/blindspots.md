# WeightFlow — Beginner Blindspots
_Things that kill apps or cause rework if not planned from Day 1_

---

## CRITICAL — Will break app if missed

### 🔑 Android Keystore Backup
**What**: The signing key used to publish your app. If lost, you cannot update your app. Ever.
**Action**: Create keystore in Phase 1. Back up to: (1) external hard drive, (2) cloud storage, (3) password manager.
**When**: Phase 1, Task 1. Before first build.

### 📋 Privacy Policy Webpage
**What**: Both Google Play and Apple App Store require a live public URL for your privacy policy before you can submit.
**Action**: Create `privacy-policy.md` → publish via GitHub Pages. Zero cost.
**When**: Phase 4 preparation.

---

## HIGH PRIORITY — Will cost you users or market access

### 🧒 COPPA — Age Gate (Under 13)
Health data + minors = significant legal risk.
**Action**: Add "I am 13 or older" confirmation in onboarding. One checkbox, major legal protection.

### 📦 APK Size Budget (<15MB)
Users in India, Southeast Asia, Africa have data limits and lower-end devices.
**Action**: Use Android App Bundle (AAB) instead of APK. Target <15MB download size. Audit Vico + font assets.

### 🏦 Room Database Migration Scripts
When you update the app with a new DB schema version, you MUST provide migration scripts or users lose all their data on update.
**Action**: Plan schema versions from Day 1. Add `Migration` objects before any schema change. Test migration in Phase 2.

### 🌐 GitHub Actions CI/CD from Day 1
Without automated test runs, you'll ship regressions. Free for public repos.
**Action**: Set up `.github/workflows/android.yml` in Phase 1. Run `./gradlew test` on every push.

### 🔥 Firebase Crashlytics from Day 1
You won't know what's crashing in the wild without this.
**Action**: Add Crashlytics dependency in Phase 1. Free, unlimited crash reporting.

---

## MEDIUM PRIORITY — Will limit your market

### ♿ Accessibility Labels
Every `@Composable` needs a `contentDescription`. Required for screen readers and legally mandated in some regions.
**Action**: Add content descriptions to all interactive elements during Phase 2.

### 🌍 RTL Language Support
Arabic, Hebrew, Farsi: right-to-left layout.
**Action**: Test all Compose layouts with `layoutDirection = Rtl`. Plan RTL-safe layouts in Phase 2. Reaches UAE, Saudi Arabia, Israel markets.

### 📱 Tablet + Foldable Support
Google Play features tablet-optimized apps prominently. Large screen = free discoverability.
**Action**: Design adaptive layouts in Phase 2. Test on 7" + 10" emulator.

### 🔔 Notification Permission (Android 13+)
`POST_NOTIFICATIONS` requires runtime permission request.
**Action**: Build permission request flow in Phase 3 (onboarding). Don't assume it's granted.

---

## OPERATIONAL — Won't affect launch but will bite you later

### 📸 Store Screenshot Requirements
- Google Play: 2+ screenshots per device type, feature graphic (1024×500)
- Apple App Store: Screenshots for iPhone SE, iPhone 14, iPad (mandatory)
**Action**: Schedule screenshot session before Phase 4. Use emulators + screengrab tools.

### 🔢 Version Code Management
Android `versionCode` must always increase. Never reuse or decrement. Plan a versioning scheme.
**Action**: Use semver: `versionName "1.0.0"`, `versionCode 1`. Automate in CI.

### 📝 Release Notes Strategy
Each Play Store update needs release notes (500 chars max, multiple languages).
**Action**: Write release notes for every update. Keep a `CHANGELOG.md` in the repo.

### 🇨🇳 China Firebase Limitation
Firebase services are blocked in China. If CloudSync is active, it will fail silently for Chinese users.
**Action**: Detect CN locale → disable Firebase features → show local-only mode. Or exclude CN from Phase 5 initially.

### 🔒 Network Security Config
Android blocks cleartext HTTP by default. Ensure all network calls use HTTPS. Firebase handles this, but any future API must comply.
**Action**: Add `network_security_config.xml` in Phase 1 explicitly blocking cleartext.

### 💳 Google Play Small Business Program
After your first year, if you earn <$1M, you qualify for 15% store cut instead of 30%.
**Action**: Enroll at launch. Cuts your transaction cost in half.
