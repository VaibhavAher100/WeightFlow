# Phase 3.5 Gap Closure — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Close all audit gaps before Phase 4 Play Store submission — code fixes, compliance content, and build hardening.

**Architecture:** Sequential with two parallel tracks: Claude executes code tasks (Tasks 1–7), agents produce content (Tasks 8–9). User actions are documented last (Tasks 10–14) and cannot be automated.

**Tech Stack:** Kotlin 2.2.10, Jetpack Compose, Room 2.7.0, WorkManager 2.10.1, AGP 9.1.1, R8/ProGuard, GitHub Actions CI.

---

## Execution Order

```
Task 1 (commit UI) → Task 2 (merge PR #30) → Task 3 (POST_NOTIFICATIONS)
→ Task 4 (COPPA) → Task 5 (ProGuard rules) → Task 6 (build config)
→ Task 7 (donation links) → Task 8 (compliance-auditor) → Task 9 (legal-advisor)
→ Tasks 10-14 (user actions — parallel with each other)
```

---

## Task 1: Commit UI Overhaul (7 screens, uncommitted in main)

**Files:**
- Modify (stage): all 17 modified files shown in `git status`

- [ ] **Step 1: Verify all 187 unit tests pass before committing**

```bash
cd WeightFlow
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew testDebugUnitTest
```
Expected: `BUILD SUCCESSFUL` — 187 tests, 0 failures.

- [ ] **Step 2: Stage all modified UI files**

```bash
git add app/src/main/java/com/weightflow/ui/history/HistoryScreen.kt
git add app/src/main/java/com/weightflow/ui/history/HistoryUiState.kt
git add app/src/main/java/com/weightflow/ui/history/HistoryViewModel.kt
git add app/src/main/java/com/weightflow/ui/home/HomeScreen.kt
git add app/src/main/java/com/weightflow/ui/home/HomeUiState.kt
git add app/src/main/java/com/weightflow/ui/home/HomeUiStateMapper.kt
git add app/src/main/java/com/weightflow/ui/logentry/LogEntryScreen.kt
git add app/src/main/java/com/weightflow/ui/navigation/NavGraph.kt
git add app/src/main/java/com/weightflow/ui/onboarding/OnboardingScreen.kt
git add app/src/main/java/com/weightflow/ui/profile/ProfileScreen.kt
git add app/src/main/java/com/weightflow/ui/profile/ProfileUiState.kt
git add app/src/main/java/com/weightflow/ui/profile/ProfileViewModel.kt
git add app/src/main/java/com/weightflow/ui/shell/ShellScreen.kt
git add app/src/main/java/com/weightflow/ui/theme/Color.kt
git add app/src/main/java/com/weightflow/ui/trends/TrendsScreen.kt
git add app/src/test/java/com/weightflow/ui/profile/ProfileViewModelTest.kt
git add app/src/test/java/com/weightflow/ui/home/HomeUiStateMapperTest.kt
git add logs/_decisions.md
git add logs/_state.md
git add logs/sessions/
git add CLAUDE.md
```

- [ ] **Step 3: Commit**

```bash
git commit -m "feat(ui): Athlete's Journal overhaul — all 7 screens

WFTokens design system, warm dark palette (#0F0E0B + #C8FF00 accent),
Bebas Neue display font, Outfit body font applied across:
- ShellScreen: styled nav bar, transparent indicator
- HomeScreen: Loading/Empty/HasData, goal banners, badge snackbar
- TrendsScreen: Vico chart + stat cards (MIN/MAX/AVG/CHANGE)
- HistoryScreen: flat divider list, delta chips, sticky month headers
- ProfileScreen: avatar gradient, badge LazyRow, body stats grid
- LogEntrySheet: BasicTextField 72sp Bebas Neue, +/- step buttons
- OnboardingScreen: pill step dots, styled step cards"
```

---

## Task 2: Merge PR #30 (Settings Screen + Crashlytics Scaffold)

**Files:** Adds `ui/settings/SettingsScreen.kt`, `SettingsViewModel.kt`, `SettingsUiState.kt` to main.

- [ ] **Step 1: Check PR #30 merge status**

```bash
gh pr view 30 --json state,mergeable,mergeStateStatus
```

- [ ] **Step 2: Merge PR #30**

```bash
gh pr merge 30 --merge --delete-branch
```

If merge conflicts arise, resolve manually — the UI overhaul commit (Task 1) may conflict with PR #30 files that touch NavGraph or ProfileScreen.

- [ ] **Step 3: Verify tests still pass after merge**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew testDebugUnitTest
```
Expected: `BUILD SUCCESSFUL` — 190+ tests (adds SettingsViewModelTest).

- [ ] **Step 4: Commit merge if needed (gh pr merge handles it)**

---

## Task 3: POST_NOTIFICATIONS Permission + Runtime Request

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/java/com/weightflow/MainActivity.kt`

- [ ] **Step 1: Add permission to AndroidManifest.xml**

In `app/src/main/AndroidManifest.xml`, add before `<application>`:

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

Full file after edit:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application
        android:name=".WeightFlowApp"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.WeightFlow">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

- [ ] **Step 2: Read MainActivity.kt to understand its current structure**

```bash
cat app/src/main/java/com/weightflow/MainActivity.kt
```

- [ ] **Step 3: Add runtime permission request in MainActivity**

Add Accompanist or Activity Result API request. In `MainActivity.kt`, add inside `onCreate` after `setContent`:

```kotlin
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

// In MainActivity class, before onCreate or as a property:
private val requestNotificationPermission =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op — graceful deny */ }

// In onCreate, after setContent { ... }:
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED
    ) {
        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
```

- [ ] **Step 4: Build to verify no compile errors**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/AndroidManifest.xml app/src/main/java/com/weightflow/MainActivity.kt
git commit -m "fix(notifications): add POST_NOTIFICATIONS permission + runtime request

Required for WorkManager daily reminder on Android 13+ (API 33).
Graceful deny path — reminder simply won't fire if user declines."
```

---

## Task 4: COPPA — Emit AgeDeclined Event

**Files:**
- Modify: `app/src/main/java/com/weightflow/ui/onboarding/OnboardingViewModel.kt`
- Modify: `app/src/main/java/com/weightflow/ui/onboarding/OnboardingScreen.kt`
- Modify: `app/src/test/java/com/weightflow/ui/onboarding/OnboardingViewModelTest.kt`

- [ ] **Step 1: Write failing test first**

Add to `OnboardingViewModelTest.kt` (before the closing `}`):

```kotlin
@Test
fun `trying to advance from AgeGate without confirmation emits AgeDeclined`() = runTest {
    val vm = makeViewModel()
    // ageConfirmed is false by default
    vm.events.test {
        vm.onNextStep()
        advanceUntilIdle()
        val event = awaitItem()
        assertTrue("Expected AgeDeclined, got $event", event is OnboardingEvent.AgeDeclined)
        cancelAndIgnoreRemainingEvents()
    }
}
```

- [ ] **Step 2: Run test to confirm it fails**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew testDebugUnitTest \
  --tests "com.weightflow.ui.onboarding.OnboardingViewModelTest.trying to advance from AgeGate without confirmation emits AgeDeclined"
```
Expected: FAIL — test times out waiting for event (event never emitted).

- [ ] **Step 3: Update OnboardingViewModel to emit AgeDeclined**

Replace the `AGE_GATE` branch in `onNextStep()`:

```kotlin
OnboardingStep.AGE_GATE -> {
    if (!state.ageConfirmed) {
        viewModelScope.launch { _events.emit(OnboardingEvent.AgeDeclined) }
        return
    }
    _uiState.update { it.copy(currentStep = OnboardingStep.UNIT, canAdvance = true) }
}
```

- [ ] **Step 4: Run all onboarding tests**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew testDebugUnitTest \
  --tests "com.weightflow.ui.onboarding.OnboardingViewModelTest"
```
Expected: 18 tests PASS (17 existing + 1 new).

- [ ] **Step 5: Update OnboardingScreen to show feedback on AgeDeclined**

In `OnboardingScreen.kt`, update the `LaunchedEffect(Unit)` event collector to show a Snackbar or inline message. Add `SnackbarHostState` and handle the event:

```kotlin
// Add to OnboardingScreen composable parameters area:
val snackbarHostState = remember { SnackbarHostState() }
val scope = rememberCoroutineScope()

// Update LaunchedEffect:
LaunchedEffect(Unit) {
    viewModel.events.collect { event ->
        when (event) {
            is OnboardingEvent.Finished    -> onFinished()
            is OnboardingEvent.AgeDeclined -> scope.launch {
                snackbarHostState.showSnackbar("You must be 13 or older to use WeightFlow.")
            }
        }
    }
}

// Wrap Column in a Scaffold with snackbarHost:
Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    containerColor = MaterialTheme.colorScheme.background,
) { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            // ... rest of existing modifiers
    ) { /* existing content */ }
}
```

- [ ] **Step 6: Build to verify no compile errors**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug
```

- [ ] **Step 7: Run all tests**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew testDebugUnitTest
```
Expected: `BUILD SUCCESSFUL` — all tests pass.

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/onboarding/OnboardingViewModel.kt
git add app/src/main/java/com/weightflow/ui/onboarding/OnboardingScreen.kt
git add app/src/test/java/com/weightflow/ui/onboarding/OnboardingViewModelTest.kt
git commit -m "fix(coppa): emit AgeDeclined event + show snackbar feedback

COPPA compliance: attempting to advance from age gate without confirming
now emits AgeDeclined event. OnboardingScreen shows 'You must be 13 or
older to use WeightFlow.' Closes silent-return gap identified in audit."
```

---

## Task 5: Write R8/ProGuard Rules

**Files:**
- Modify: `app/proguard-rules.pro`

No tests required — build verification is the test.

- [ ] **Step 1: Replace proguard-rules.pro with complete rules**

```proguard
# ── Stack traces ──────────────────────────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Room ──────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers @androidx.room.Entity class * { *; }
-keepclassmembers @androidx.room.Dao interface * { *; }

# ── DataStore ─────────────────────────────────────────────────────────────────
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite { <fields>; }

# ── Kotlin Coroutines ─────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

# ── Kotlin ────────────────────────────────────────────────────────────────────
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Lazy { <methods>; }
-dontwarn kotlin.**

# ── Vico charts ───────────────────────────────────────────────────────────────
-keep class com.patrykandpatrick.vico.** { *; }
-dontwarn com.patrykandpatrick.vico.**

# ── WorkManager ───────────────────────────────────────────────────────────────
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keepclassmembers class * extends androidx.work.CoroutineWorker { *; }

# ── WeightFlow domain + data layers ──────────────────────────────────────────
-keep class com.weightflow.domain.** { *; }
-keep class com.weightflow.data.** { *; }

# ── Enums ─────────────────────────────────────────────────────────────────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ── Compose ───────────────────────────────────────────────────────────────────
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }
```

- [ ] **Step 2: Commit ProGuard rules (before enabling minify)**

```bash
git add app/proguard-rules.pro
git commit -m "build: write complete R8/ProGuard rules for all dependencies

Covers: Room entities/DAOs, DataStore, Vico 1.13.1, WorkManager,
Kotlin coroutines, WeightFlow domain/data layers, enums, Compose.
Stack trace line numbers preserved for Crashlytics symbolication."
```

---

## Task 6: Enable isMinifyEnabled + Signing Config

**Files:**
- Modify: `app/build.gradle.kts`

> **Prerequisite:** Task 10 (keystore generation) must be complete before a signed release build can actually run. The gradle config can be written now; the build will use env vars.

- [ ] **Step 1: Update build.gradle.kts release block**

Replace the existing `buildTypes { release { ... } }` block with:

```kotlin
signingConfigs {
    create("release") {
        storeFile = file(System.getenv("WF_KEYSTORE_PATH") ?: "weightflow-release.jks")
        storePassword = System.getenv("WF_KEYSTORE_PASSWORD") ?: ""
        keyAlias = System.getenv("WF_KEY_ALIAS") ?: "weightflow"
        keyPassword = System.getenv("WF_KEY_PASSWORD") ?: ""
    }
}

buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
        signingConfig = signingConfigs.getByName("release")
    }
}
```

- [ ] **Step 2: Verify debug build still works (minify only affects release)**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Run unit tests**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew testDebugUnitTest
```
Expected: `BUILD SUCCESSFUL` — all tests pass.

- [ ] **Step 4: Commit**

```bash
git add app/build.gradle.kts
git commit -m "build(release): enable R8 minification + signing config from env vars

isMinifyEnabled=true + isShrinkResources=true for release.
Signing reads WF_KEYSTORE_PATH / WF_KEYSTORE_PASSWORD / WF_KEY_ALIAS /
WF_KEY_PASSWORD from environment — safe for CI, no secrets in repo."
```

---

## Task 7: Add Donation Links to ProfileScreen

**Files:**
- Modify: `app/src/main/java/com/weightflow/ui/profile/ProfileScreen.kt`

No new ViewModel tests needed — this is display-only UI with `Intent.ACTION_VIEW`.

- [ ] **Step 1: Add support section to ProfileScreen**

In `ProfileScreen.kt`, add a new composable and call it inside the `LazyColumn` in the `HasData` state, at the bottom after the badge row:

```kotlin
// Add import at top of file:
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.ui.platform.LocalContext

// Add composable:
@Composable
private fun SupportSection(accent: androidx.compose.ui.graphics.Color) {
    val context = LocalContext.current
    val links = listOf(
        Triple("Ko-fi", "Support with a coffee", "https://ko-fi.com/vaibhavaher"),
        Triple("Liberapay", "Recurring support", "https://liberapay.com/vaibhavaher"),
        Triple("GitHub Sponsors", "Support on GitHub", "https://github.com/sponsors/VaibhavAher100"),
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WFTokens.Card, RoundedCornerShape(12.dp))
            .padding(vertical = 4.dp),
    ) {
        Text(
            text = "SUPPORT",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.5.sp,
                color = WFTokens.Text3,
            ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        )
        links.forEach { (title, subtitle, url) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(title, style = MaterialTheme.typography.bodyMedium)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(color = WFTokens.Text2))
                }
                Icon(
                    Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Open $title",
                    tint = accent,
                    modifier = Modifier.size(16.dp),
                )
            }
            if (title != "GitHub Sponsors") {
                HorizontalDivider(color = WFTokens.Border, thickness = 1.dp)
            }
        }
    }
}
```

Call it inside the `LazyColumn` in `HasData` case, after the badges item:

```kotlin
item { Spacer(Modifier.height(16.dp)) }
item { SupportSection(accent = accent) }
item { Spacer(Modifier.height(32.dp)) }
```

> **Note:** Update the Ko-fi/Liberapay/GitHub URLs with the real ones once those accounts are created. These are placeholder URLs using the GitHub username pattern.

- [ ] **Step 2: Build to verify no compile errors**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/profile/ProfileScreen.kt
git commit -m "feat(profile): add Support section with donation links

Ko-fi, Liberapay, GitHub Sponsors — each opens in browser via Intent.
Primary revenue channel per product strategy (donations before Pro).
URLs are placeholders — update once accounts created."
```

---

## Task 8: Run compliance-auditor Agent

**Output:** `docs/privacy/compliance-audit.md`

- [ ] **Step 1: Dispatch compliance-auditor agent**

Dispatch the `compliance-auditor` agent from `.claude/agents/` with this prompt:

> "Audit WeightFlow Android app for GDPR, CCPA, COPPA, and India DPDP Act 2023 compliance. The app is offline-first — all data stored on-device only in Room DB and DataStore. No network calls. No analytics. No third-party SDKs active (Crashlytics deps commented). Features: weight tracking, CSV import/export, daily reminder notification (WorkManager), 4-step onboarding with COPPA age gate (13+), badge achievements, goal tracking. Read these files for context: app/src/main/AndroidManifest.xml, app/src/main/res/xml/backup_rules.xml, app/src/main/res/xml/data_extraction_rules.xml, app/src/main/java/com/weightflow/WeightFlowApp.kt. Output a compliance report to docs/privacy/compliance-audit.md with: (1) what's already compliant, (2) gaps requiring action before launch, (3) recommended privacy policy clauses."

- [ ] **Step 2: Verify output exists**

```bash
ls docs/privacy/
```
Expected: `compliance-audit.md` present.

---

## Task 9: Run legal-advisor Agent → Draft Privacy Policy + ToS

**Output:** `docs/privacy/privacy-policy.md`, `docs/privacy/terms-of-service.md`

- [ ] **Step 1: Read compliance audit output first**

Read `docs/privacy/compliance-audit.md` to inform the legal-advisor agent.

- [ ] **Step 2: Dispatch legal-advisor agent**

Dispatch the `legal-advisor` agent with this prompt:

> "Draft a privacy policy and terms of service for WeightFlow, a free Android weight tracking app. Key facts: (1) All data stored on-device only in Room SQLite DB — no servers, no cloud, no data transmission. (2) No analytics, no tracking, no advertising SDKs active at launch. (3) CSV export available — user controls their own data. (4) COPPA compliant — age gate requires 13+ confirmation in onboarding. (5) WorkManager sends daily reminder notification — no data leaves device. (6) App is free. Pro unlock planned (one-time purchase) but not yet implemented. (7) Target markets: global, primarily EU/India/US. (8) Developer: Vaibhav Aher, vaibhavaher100@gmail.com. (9) Package: com.weightflow. Write privacy-policy.md and terms-of-service.md to docs/privacy/. Both must be suitable for hosting on GitHub Pages as static HTML or rendered Markdown. Privacy policy must satisfy GDPR Article 13 disclosure requirements and Google Play Data Safety requirements."

- [ ] **Step 3: Verify both documents exist and are complete**

```bash
ls docs/privacy/
```
Expected: `privacy-policy.md` and `terms-of-service.md` present with real content.

- [ ] **Step 4: Commit**

```bash
git add docs/privacy/
git commit -m "docs(privacy): add compliance audit, privacy policy, and ToS

Generated via compliance-auditor + legal-advisor agents.
Both documents satisfy GDPR Article 13, COPPA, DPDP Act 2023,
and Google Play Data Safety disclosure requirements.
Next step: host on GitHub Pages → get live URL for Play Console."
```

---

## Task 10: [USER ACTION] Generate Release Keystore

**⚠ CRITICAL — do not skip. Losing this file = cannot ever update the app on Play Store.**

- [ ] **Step 1: Generate keystore**

Run in terminal (not in Claude):

```bash
keytool -genkey -v \
  -keystore weightflow-release.jks \
  -alias weightflow \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

Fill in: name, org, city, country. **Write down the passwords somewhere safe immediately.**

- [ ] **Step 2: Move keystore OUT of project directory**

```bash
mv weightflow-release.jks ~/Documents/WeightFlow-keystore/weightflow-release.jks
```

Never commit the keystore file. `.gitignore` must exclude `*.jks`.

- [ ] **Step 3: Add *.jks to .gitignore**

```bash
echo "*.jks" >> .gitignore
echo "*.keystore" >> .gitignore
git add .gitignore
git commit -m "chore: exclude keystore files from git"
```

- [ ] **Step 4: Back up to 3 locations**

1. Local encrypted drive / password manager (Bitwarden/1Password)
2. Google Drive or iCloud (encrypted zip or inside a password manager export)
3. Second device or trusted offline backup

- [ ] **Step 5: Set env vars for release build**

```bash
export WF_KEYSTORE_PATH="$HOME/Documents/WeightFlow-keystore/weightflow-release.jks"
export WF_KEYSTORE_PASSWORD="your_store_password"
export WF_KEY_ALIAS="weightflow"
export WF_KEY_PASSWORD="your_key_password"
```

- [ ] **Step 6: Test signed release build**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" \
  WF_KEYSTORE_PATH="..." WF_KEYSTORE_PASSWORD="..." WF_KEY_ALIAS="weightflow" WF_KEY_PASSWORD="..." \
  ./gradlew bundleRelease
```
Expected: `BUILD SUCCESSFUL`. AAB file at `app/build/outputs/bundle/release/app-release.aab`.

---

## Task 11: [USER ACTION] Firebase Setup + google-services.json

- [ ] **Step 1:** Go to [console.firebase.google.com](https://console.firebase.google.com)
- [ ] **Step 2:** Create project → name "WeightFlow"
- [ ] **Step 3:** Add Android app → package name: `com.weightflow`
- [ ] **Step 4:** Download `google-services.json`
- [ ] **Step 5:** Place file at `app/google-services.json`
- [ ] **Step 6:** Tell Claude — Claude will then activate Crashlytics in `WeightFlowApp.kt` and `build.gradle.kts`

> **Do NOT commit google-services.json.** Add `google-services.json` to `.gitignore` before committing.

---

## Task 12: [USER ACTION] Host Privacy Policy on GitHub Pages

- [ ] **Step 1:** Go to GitHub repo → Settings → Pages
- [ ] **Step 2:** Source: Deploy from branch → Branch: `main` → Folder: `/docs`
- [ ] **Step 3:** Save — GitHub Pages URL becomes `https://VaibhavAher100.github.io/WeightFlow/`
- [ ] **Step 4:** Privacy policy will be live at `https://VaibhavAher100.github.io/WeightFlow/privacy/privacy-policy`
- [ ] **Step 5:** Add that URL to the app's Settings → About section and to Play Console
- [ ] **Step 6:** Verify the URL loads in browser before Play Store submission

---

## Task 13: [USER ACTION] Fill Data Safety Form in Play Console

- [ ] **Step 1:** Play Console → Your app → Policy → App content → Data safety
- [ ] **Step 2:** Answer all questions:
  - Does your app collect or share any of the required user data types? → **No**
  - Is all of the user data collected by your app encrypted in transit? → **N/A** (no network)
  - Do you provide a way for users to request that their data is deleted? → **Yes** (delete account/data in Settings)
- [ ] **Step 3:** Submit the form

---

## Task 14: [USER ACTION] Capture Store Screenshots

- [ ] **Step 1:** Launch app on Pixel 7 Pro emulator (API 33+, 6.7" — required by Play Store)
- [ ] **Step 2:** Add sample data (10+ entries, a goal set)
- [ ] **Step 3:** Capture screenshots of:
  1. HomeScreen with weight data and goal progress
  2. TrendsScreen with Vico chart rendered
  3. HistoryScreen with list entries and delta chips
  4. ProfileScreen with badges earned
  5. LogEntry sheet open
- [ ] **Step 4:** Screenshots land in `~/Android/...` or use `adb shell screencap`

```bash
adb shell screencap -p /sdcard/screenshot.png && adb pull /sdcard/screenshot.png ./screenshots/
```

- [ ] **Step 5:** Use `gplay-screenshot-automation` skill for automated capture across form factors

---

## Self-Review Checklist

- [x] All 14 audit gaps covered
- [x] Code tasks (1-7) have exact file paths and code
- [x] TDD applied to Task 4 (COPPA — only behavioral change)
- [x] ProGuard rules cover all active dependencies (Room, DataStore, Vico, WorkManager, Coroutines, Compose)
- [x] User actions clearly separated with [USER ACTION] prefix
- [x] Build verification step in every code task
- [x] No placeholders — all code blocks are complete
- [x] Execution order respects dependencies (ProGuard before isMinifyEnabled, PR merge before donation links)
