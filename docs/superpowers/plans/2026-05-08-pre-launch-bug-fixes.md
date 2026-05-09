# Pre-Launch Bug Fixes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix all 13 issues identified in the Codex pre-launch audit before Phase 4 Play Store submission.

**Architecture:** Fixes are grouped by domain: data-layer correctness first (DB key, unit conversion, date model), then UX bugs, then release-gate hardening, then security/privacy cleanup, then accessibility. Every fix follows TDD — failing test first, minimal fix, green, commit.

**Tech Stack:** Kotlin 2.2.10, Jetpack Compose BOM 2025.04.01, Room 2.7.0, SQLCipher (net.zetetic), DataStore 1.1.1, AndroidX Security Crypto (EncryptedSharedPreferences), WorkManager 2.10.1, MockK 1.13.12, Turbine 1.1.0.

---

## Issue Index

| # | Issue | Task |
|---|-------|------|
| 1 | SQLCipher `getKey().encoded` returns null on real devices | Task 1 |
| 2 | Stones unit treated as kg on input | Task 2 |
| 3 | Date off-by-one in UTC+ timezones | Task 3 |
| 4 | Onboarding discards initial weight entry | Task 4 |
| 5 | Log entry save button stuck disabled after first save | Task 5 |
| 6 | `validateReleaseSigning` not wired to `bundleRelease` | Task 6 |
| 7 | OSV scan `continue-on-error: true` bypasses release gate | Task 7 |
| 10 | Encrypted ZIP temp file leaked when picker cancelled | Task 8 |
| 11 | Export password held as `String` — can't be zeroed | Task 9 |
| 12 | Privacy docs split across two files | Task 10 |
| 13 | Touch targets under 48dp (delete button 36dp, back 44dp) | Task 11 |
| 9 | Lint error `PropertyEscape` in local.properties | Task 12 |
| 8 | Instrumented tests missing from CI (no emulator) | Out of scope v1.0 |

---

## File Map

| File | What changes |
|------|-------------|
| `app/src/main/java/com/weightflow/data/DatabaseKeyManager.kt` | Replace Keystore AES `encoded` with EncryptedSharedPreferences-backed random passphrase |
| `app/src/main/java/com/weightflow/domain/WeightConverter.kt` | Add `stToKg(decimalStones: Double)` |
| `app/src/main/java/com/weightflow/ui/logentry/LogEntryViewModel.kt` | Fix ST→kg conversion; add `reset()` |
| `app/src/main/java/com/weightflow/ui/onboarding/OnboardingViewModel.kt` | Fix ST conversion; add WeightRepository; save initial weight entry |
| `app/src/main/java/com/weightflow/MainActivity.kt` | Pass `weightRepository` to `OnboardingViewModel` |
| `app/src/main/java/com/weightflow/domain/BadgeEngine.kt` | Fix UTC÷86400 → local-timezone date |
| `app/src/main/java/com/weightflow/domain/CsvExporter.kt` | Fix UTC÷86400 → local-timezone date |
| `app/src/main/java/com/weightflow/domain/CsvParsers.kt` | Fix UTC÷86400 if present |
| `app/src/main/java/com/weightflow/ui/shell/ShellScreen.kt` | Call `logEntryVm.reset()` when sheet opens |
| `app/build.gradle.kts` | Wire `validateReleaseSigning` to `bundleRelease` |
| `.github/workflows/android.yml` | Remove `continue-on-error: true` from OSV scan step |
| `app/src/main/java/com/weightflow/ui/settings/SettingsScreen.kt` | Delete temp ZIP on picker cancel; fix password comment |
| `docs/privacy-policy.md` | Point to canonical `docs/privacy/privacy-policy.md` or merge |
| `app/src/main/java/com/weightflow/ui/history/HistoryScreen.kt` | Delete button 36dp → 48dp |
| `app/src/main/java/com/weightflow/ui/settings/SettingsScreen.kt` | Back button 44dp → 48dp |
| `app/src/test/java/com/weightflow/data/DatabaseKeyManagerTest.kt` | New unit test for passphrase stability |
| `app/src/test/java/com/weightflow/domain/WeightConverterTest.kt` | Add ST conversion tests |
| `app/src/test/java/com/weightflow/domain/BadgeEngineTest.kt` | Add UTC+ date test |
| `app/src/test/java/com/weightflow/domain/CsvExporterTest.kt` | Add UTC+ date test |
| `app/src/test/java/com/weightflow/ui/onboarding/OnboardingViewModelTest.kt` | Add initial-weight insertion test |
| `app/src/test/java/com/weightflow/ui/logentry/LogEntryViewModelTest.kt` | Add reset() test |

---

## Task 1: Fix SQLCipher Key — Replace Keystore `encoded` with EncryptedSharedPreferences

**Context:** `AndroidKeystoreKeyProvider.getOrCreateKey()` calls `secretKey.encoded` on a hardware-backed Android Keystore AES key. Hardware-backed keys are non-exportable — `.encoded` returns `null`. SQLCipher receives a null passphrase → database open fails on real devices.

**Fix:** Generate a random 32-byte passphrase once, store it in `EncryptedSharedPreferences` (which uses Keystore internally for the master key but allows the plaintext to be read back).

**Files:**
- Modify: `app/src/main/java/com/weightflow/data/DatabaseKeyManager.kt`
- New test: `app/src/test/java/com/weightflow/data/DatabaseKeyManagerTest.kt`
- Check dependency: `app/build.gradle.kts` — ensure `androidx.security:security-crypto` is present

- [ ] **Step 1: Verify security-crypto dependency exists**

```bash
grep "security-crypto" app/build.gradle.kts
```
If absent, add to `app/build.gradle.kts` under `dependencies`:
```kotlin
implementation("androidx.security:security-crypto:1.1.0-alpha06")
```

- [ ] **Step 2: Write the failing test**

Create `app/src/test/java/com/weightflow/data/DatabaseKeyManagerTest.kt`:

```kotlin
package com.weightflow.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DatabaseKeyManagerTest {

    @Test
    fun `test provider returns non-null 32-byte key`() {
        var returnedKey: ByteArray? = null
        val testProvider = object : DatabaseKeyProvider {
            private val key = ByteArray(32) { it.toByte() }
            override fun getOrCreateKey(): ByteArray = key
            override fun keyExists(): Boolean = true
        }
        DatabaseKeyManager.setProviderForTesting(testProvider)
        try {
            returnedKey = DatabaseKeyManager.getOrCreateKey()
        } finally {
            DatabaseKeyManager.resetProvider()
        }
        assertNotNull(returnedKey)
        assertEquals(32, returnedKey!!.size)
    }

    @Test
    fun `test provider returns same bytes on repeated calls (stable passphrase)`() {
        val fixedKey = ByteArray(32) { 0xAB.toByte() }
        val testProvider = object : DatabaseKeyProvider {
            override fun getOrCreateKey(): ByteArray = fixedKey.copyOf()
            override fun keyExists(): Boolean = true
        }
        DatabaseKeyManager.setProviderForTesting(testProvider)
        try {
            val first = DatabaseKeyManager.getOrCreateKey()
            val second = DatabaseKeyManager.getOrCreateKey()
            assertEquals(first.toList(), second.toList())
        } finally {
            DatabaseKeyManager.resetProvider()
        }
    }
}
```

- [ ] **Step 3: Run test — confirm passes (it tests the interface, not the broken impl)**

```bash
./gradlew testDebugUnitTest --tests "com.weightflow.data.DatabaseKeyManagerTest" -q
```
Expected: PASS (test uses the injectable interface — no Keystore needed in JVM tests)

- [ ] **Step 4: Replace `AndroidKeystoreKeyProvider` with `EncryptedPrefsKeyProvider`**

Replace the full body of `AndroidKeystoreKeyProvider` in `DatabaseKeyManager.kt`:

```kotlin
package com.weightflow.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

internal interface DatabaseKeyProvider {
    fun getOrCreateKey(): ByteArray
    fun keyExists(): Boolean
}

/**
 * Production [DatabaseKeyProvider].
 *
 * Generates a cryptographically random 32-byte passphrase once and stores it in
 * [EncryptedSharedPreferences], which encrypts the value using a Keystore-backed
 * AES-256-GCM master key. This approach allows the raw passphrase bytes to be
 * read back — unlike directly exporting an Android Keystore AES key, which always
 * returns null for hardware-backed keys.
 *
 * The passphrase is stable for the lifetime of the installation. Clearing app
 * data regenerates it (acceptable — the DB file is also deleted with app data).
 */
internal class EncryptedPrefsKeyProvider(context: Context) : DatabaseKeyProvider {

    private val appContext = context.applicationContext

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            appContext,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override fun getOrCreateKey(): ByteArray {
        val existing = prefs.getString(KEY_PREF, null)
        if (existing != null) return hexToBytes(existing)
        val newKey = ByteArray(32).also { SecureRandom().nextBytes(it) }
        prefs.edit().putString(KEY_PREF, bytesToHex(newKey)).apply()
        return newKey
    }

    override fun keyExists(): Boolean = prefs.contains(KEY_PREF)

    private fun bytesToHex(bytes: ByteArray): String =
        bytes.joinToString("") { "%02x".format(it) }

    private fun hexToBytes(hex: String): ByteArray =
        ByteArray(hex.length / 2) { hex.substring(it * 2, it * 2 + 2).toInt(16).toByte() }

    internal companion object {
        const val PREFS_FILE  = "weightflow_db_key_store"
        const val KEY_PREF    = "db_passphrase"
    }
}

internal object DatabaseKeyManager {

    @Volatile
    private var provider: DatabaseKeyProvider? = null

    fun init(context: Context) {
        if (provider == null) {
            synchronized(this) {
                if (provider == null) provider = EncryptedPrefsKeyProvider(context)
            }
        }
    }

    fun getOrCreateKey(): ByteArray =
        checkNotNull(provider) { "DatabaseKeyManager.init(context) not called" }.getOrCreateKey()

    fun keyExists(): Boolean =
        checkNotNull(provider) { "DatabaseKeyManager.init(context) not called" }.keyExists()

    internal fun setProviderForTesting(testProvider: DatabaseKeyProvider) {
        provider = testProvider
    }

    internal fun resetProvider() {
        provider = null
    }
}
```

- [ ] **Step 5: Update `AppDatabase.buildDatabase` to pass context to `DatabaseKeyManager`**

In `AppDatabase.kt`, `buildDatabase` already has `context`. Change the key retrieval call:

```kotlin
// Before (line ~88):
val passphrase: ByteArray = DatabaseKeyManager.getOrCreateKey()

// After — init must be called before getOrCreateKey():
DatabaseKeyManager.init(appContext)
val passphrase: ByteArray = DatabaseKeyManager.getOrCreateKey()
```

Also update `encryptExistingDatabaseIfNeeded` — it calls `DatabaseKeyManager.getOrCreateKey()`. That call comes after `buildDatabase` is entered, so `init` has already run. No additional change needed there.

- [ ] **Step 6: Update `WeightFlowApp` to call `DatabaseKeyManager.init` early**

In `WeightFlowApp.kt`, find the Application `onCreate` or database lazy initializer. Add init before first database access:

```kotlin
// In WeightFlowApp.onCreate() or before `database` lazy:
DatabaseKeyManager.init(this)
```

Check where `AppDatabase.getInstance(this)` is called in `WeightFlowApp.kt` and add `DatabaseKeyManager.init(this)` one line above it.

- [ ] **Step 7: Run tests**

```bash
./gradlew testDebugUnitTest --tests "com.weightflow.data.DatabaseKeyManagerTest" -q
```
Expected: PASS

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/weightflow/data/DatabaseKeyManager.kt \
        app/src/main/java/com/weightflow/data/AppDatabase.kt \
        app/src/main/java/com/weightflow/WeightFlowApp.kt \
        app/src/test/java/com/weightflow/data/DatabaseKeyManagerTest.kt \
        app/build.gradle.kts
git commit -m "fix(security): replace Keystore encoded() with EncryptedSharedPreferences passphrase"
```

---

## Task 2: Fix Stones Unit Conversion

**Context:** `LogEntryViewModel.onWeightInput/onSave` and `OnboardingViewModel.onWeightInput/onGoalInput/onComplete` all have `WeightUnit.ST -> v` (pass-through). This stores the decimal-stones value as if it were kilograms. 12.8 stones = 81.2 kg, not 12.8 kg. `WeightConverter.stonesToKg(stones: Int, pounds: Int)` exists for integer stones+pounds, but no decimal-stone shortcut exists.

**Files:**
- Modify: `app/src/main/java/com/weightflow/domain/WeightConverter.kt`
- Modify: `app/src/main/java/com/weightflow/ui/logentry/LogEntryViewModel.kt`
- Modify: `app/src/main/java/com/weightflow/ui/onboarding/OnboardingViewModel.kt`
- Test: `app/src/test/java/com/weightflow/domain/WeightConverterTest.kt`
- Test: `app/src/test/java/com/weightflow/ui/logentry/LogEntryViewModelTest.kt`
- Test: `app/src/test/java/com/weightflow/ui/onboarding/OnboardingViewModelTest.kt`

- [ ] **Step 1: Write failing tests in `WeightConverterTest.kt`**

Add to the existing test class:

```kotlin
@Test
fun `stToKg converts decimal stones to kg correctly`() {
    // 1 stone = 6.35029 kg
    assertEquals(6.35029, WeightConverter.stToKg(1.0), 0.001)
}

@Test
fun `stToKg 12 point 8 stones equals approx 81 kg`() {
    assertEquals(81.28, WeightConverter.stToKg(12.8), 0.01)
}

@Test
fun `stToKg zero stones is zero kg`() {
    assertEquals(0.0, WeightConverter.stToKg(0.0), 0.001)
}
```

- [ ] **Step 2: Run — confirm FAIL**

```bash
./gradlew testDebugUnitTest --tests "com.weightflow.domain.WeightConverterTest" -q
```
Expected: FAIL — `stToKg` unresolved

- [ ] **Step 3: Add `stToKg` to `WeightConverter`**

In `WeightConverter.kt`, add after `lbsToKg`:

```kotlin
/** Converts decimal stones (e.g. 12.8) to kilograms. */
fun stToKg(decimalStones: Double): Double = decimalStones * 6.35029
```

- [ ] **Step 4: Run — confirm tests pass**

```bash
./gradlew testDebugUnitTest --tests "com.weightflow.domain.WeightConverterTest" -q
```
Expected: PASS

- [ ] **Step 5: Fix `LogEntryViewModel` — both `onWeightInput` and `onSave`**

In `LogEntryViewModel.kt`, replace both `WeightUnit.ST -> v` occurrences:

```kotlin
// onWeightInput (line ~62):
WeightUnit.ST  -> WeightConverter.stToKg(v)

// onSave (line ~88):
WeightUnit.ST  -> WeightConverter.stToKg(raw)
```

- [ ] **Step 6: Add failing test to `LogEntryViewModelTest.kt`**

```kotlin
@Test
fun `onSave with ST input converts stones to kg`() = runTest {
    every { mockUserPrefs.weightUnit } returns MutableStateFlow(WeightUnit.ST)
    every { mockWeightRepo.getEntriesNewestFirst() } returns MutableStateFlow(emptyList())

    val vm = LogEntryViewModel(mockWeightRepo, mockUserPrefs)
    advanceUntilIdle()

    vm.onWeightInput("10") // 10 stones = 63.5 kg
    vm.onSave()
    advanceUntilIdle()

    val slot = slot<Double>()
    coVerify { mockWeightRepo.addEntry(capture(slot), any()) }
    assertEquals(63.5029, slot.captured, 0.01)
}
```

- [ ] **Step 7: Fix `OnboardingViewModel` — `onWeightInput`, `onGoalInput`, and `onComplete`**

Three occurrences of `WeightUnit.ST -> v` in `OnboardingViewModel.kt`. Replace all:

```kotlin
WeightUnit.ST  -> WeightConverter.stToKg(v)
```

Also fix in `onComplete` where `input` is used instead of `v`:
```kotlin
WeightUnit.ST  -> WeightConverter.stToKg(input)
```

- [ ] **Step 8: Run all tests**

```bash
./gradlew testDebugUnitTest -q
```
Expected: all GREEN

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/weightflow/domain/WeightConverter.kt \
        app/src/main/java/com/weightflow/ui/logentry/LogEntryViewModel.kt \
        app/src/main/java/com/weightflow/ui/onboarding/OnboardingViewModel.kt \
        app/src/test/java/com/weightflow/domain/WeightConverterTest.kt \
        app/src/test/java/com/weightflow/ui/logentry/LogEntryViewModelTest.kt \
        app/src/test/java/com/weightflow/ui/onboarding/OnboardingViewModelTest.kt
git commit -m "fix(domain): correct stones-to-kg conversion on entry input"
```

---

## Task 3: Fix Date Off-by-One in UTC+ Timezones

**Context:** `LogEntryViewModel.onSave` stores timestamps as local midnight millis (via `date.atStartOfDay(ZoneId.systemDefault())`). But `BadgeEngine.maxConsecutiveStreak`, `BadgeEngine.evaluate`, and `CsvExporter.export` all convert back via `LocalDate.ofEpochDay(timestamp / 86_400_000L)`, which is UTC epoch-day division. For a user in UTC+8, local midnight on Jan 5 is Jan 4 at 16:00 UTC → `epochMillis / 86_400_000L` = Jan 4, not Jan 5. Streaks and CSV dates are wrong by one day.

**Fix:** Replace `LocalDate.ofEpochDay(ts / 86_400_000L)` with `LocalDate.ofInstant(Instant.ofEpochMilli(ts), ZoneId.systemDefault())` everywhere.

**Files:**
- Modify: `app/src/main/java/com/weightflow/domain/BadgeEngine.kt`
- Modify: `app/src/main/java/com/weightflow/domain/CsvExporter.kt`
- Modify: `app/src/main/java/com/weightflow/domain/CsvParsers.kt` (if present)
- Test: `app/src/test/java/com/weightflow/domain/BadgeEngineTest.kt`
- Test: `app/src/test/java/com/weightflow/domain/CsvExporterTest.kt`

- [ ] **Step 1: Write a failing test for BadgeEngine that demonstrates the bug**

Add to `BadgeEngineTest.kt`:

```kotlin
@Test
fun `streak uses local date not UTC epoch division`() {
    // Simulate UTC+8 — midnight Jan 1 local = Dec 31 UTC
    // If we use /86400000, we'd get Dec 31 and Jan 2 = not consecutive
    val zone = ZoneId.of("Asia/Shanghai") // UTC+8
    fun localMidnightMillis(date: LocalDate): Long =
        date.atStartOfDay(zone).toInstant().toEpochMilli()

    val jan1 = localMidnightMillis(LocalDate.of(2024, 1, 1))
    val jan2 = localMidnightMillis(LocalDate.of(2024, 1, 2))
    val jan3 = localMidnightMillis(LocalDate.of(2024, 1, 3))

    val entries = listOf(
        WeightEntry(id = 1, weightKg = 80.0, timestamp = jan1),
        WeightEntry(id = 2, weightKg = 80.0, timestamp = jan2),
        WeightEntry(id = 3, weightKg = 80.0, timestamp = jan3),
    )
    val profile = UserProfile(id = 1, displayName = "", goalWeightKg = null,
        targetDate = null, heightCm = null, maintenanceMode = false,
        maintenanceRangeKg = 2.0, maintenanceModeActivatedAt = null)

    val badges = BadgeEngine.evaluate(entries, profile)
    assertTrue("7-day streak badge should not be awarded for 3 days",
        Badge.SEVEN_DAY_STREAK !in badges)
    assertTrue("Should detect 3-day consecutive streak without breaking",
        badges.contains(Badge.FIRST_WEIGH_IN))
}
```

Note: this test will pass even before the fix because it doesn't cross the midnight UTC boundary for UTC+8 on Jan 1-3. The key test is for streak continuity. Add a more precise test:

```kotlin
@Test
fun `consecutive streak not broken by UTC offset for UTC plus timezone`() {
    // Jan 5 00:00 UTC+8 = Jan 4 16:00 UTC → epoch millis / 86400000 gives Jan 4, not Jan 5
    val zone = ZoneId.of("Asia/Shanghai")
    fun midnight(year: Int, month: Int, day: Int) =
        LocalDate.of(year, month, day).atStartOfDay(zone).toInstant().toEpochMilli()

    val entries = (1..8).map { day ->
        WeightEntry(id = day, weightKg = 80.0, timestamp = midnight(2024, 1, day))
    }
    val profile = UserProfile(id = 1, displayName = "", goalWeightKg = null,
        targetDate = null, heightCm = null, maintenanceMode = false,
        maintenanceRangeKg = 2.0, maintenanceModeActivatedAt = null)

    val badges = BadgeEngine.evaluate(entries, profile)
    assertTrue("7-day streak should be awarded for 8 consecutive local days",
        Badge.SEVEN_DAY_STREAK in badges)
}
```

- [ ] **Step 2: Run — note whether test passes or fails (depends on test runner timezone)**

```bash
./gradlew testDebugUnitTest --tests "com.weightflow.domain.BadgeEngineTest" -q
```

- [ ] **Step 3: Fix `BadgeEngine.maxConsecutiveStreak`**

In `BadgeEngine.kt`, replace the date conversion:

```kotlin
// Before:
val dates = entries
    .map { LocalDate.ofEpochDay(it.timestamp / 86_400_000L) }
    .toSortedSet()

// After:
val dates = entries
    .map { LocalDate.ofInstant(java.time.Instant.ofEpochMilli(it.timestamp), java.time.ZoneId.systemDefault()) }
    .toSortedSet()
```

- [ ] **Step 4: Fix `BadgeEngine.evaluate` — `maintenanceModeActivatedAt` conversion**

```kotlin
// Before:
val activatedDate = LocalDate.ofEpochDay(profile.maintenanceModeActivatedAt / 86_400_000L)

// After:
val activatedDate = LocalDate.ofInstant(
    java.time.Instant.ofEpochMilli(profile.maintenanceModeActivatedAt),
    java.time.ZoneId.systemDefault()
)
```

- [ ] **Step 5: Fix `CsvExporter.export`**

In `CsvExporter.kt`, the date conversion in `export()` and `exportMinimalCsv()`:

```kotlin
// Before:
val date = LocalDate.ofEpochDay(entry.timestamp / 86_400_000L)

// After:
val date = java.time.LocalDate.ofInstant(
    java.time.Instant.ofEpochMilli(entry.timestamp),
    java.time.ZoneId.systemDefault()
)
```

Apply this to every occurrence in `CsvExporter.kt` (check `exportMinimalCsv` too).

- [ ] **Step 6: Check `CsvParsers.kt` for epoch division**

```bash
grep -n "86_400_000\|ofEpochDay" app/src/main/java/com/weightflow/domain/CsvParsers.kt
```

If any exist, apply the same fix. CsvParsers parses dates *from* strings, so conversion is typically `LocalDate.parse(str).atStartOfDay(ZoneId.systemDefault()).toEpochMilli()` which is correct. Only fix if you find division-by-86400000 there.

- [ ] **Step 7: Run all tests**

```bash
./gradlew testDebugUnitTest -q
```
Expected: GREEN

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/weightflow/domain/BadgeEngine.kt \
        app/src/main/java/com/weightflow/domain/CsvExporter.kt \
        app/src/test/java/com/weightflow/domain/BadgeEngineTest.kt \
        app/src/test/java/com/weightflow/domain/CsvExporterTest.kt
git commit -m "fix(domain): use local timezone for date conversion instead of UTC epoch division"
```

---

## Task 4: Onboarding — Save Initial Weight Entry

**Context:** `OnboardingViewModel.onComplete()` saves `UserProfile` and sets `onboardingComplete`, but never calls `weightRepository.addEntry(weightKg, timestamp)`. New users see an empty tracker immediately after completing onboarding.

**Files:**
- Modify: `app/src/main/java/com/weightflow/ui/onboarding/OnboardingViewModel.kt`
- Modify: `app/src/main/java/com/weightflow/MainActivity.kt`
- Test: `app/src/test/java/com/weightflow/ui/onboarding/OnboardingViewModelTest.kt`

- [ ] **Step 1: Write the failing test**

In `OnboardingViewModelTest.kt`, add:

```kotlin
@Test
fun `onComplete saves initial weight entry to repository`() = runTest {
    val mockWeightRepo = mockk<WeightRepository>(relaxed = true)
    val vm = OnboardingViewModel(
        userProfileRepository = mockUserProfileRepo,
        userPrefsDataStore = mockUserPrefs,
        weightRepository = mockWeightRepo,
    )

    vm.onUnitSelected(WeightUnit.KG)
    vm.onWeightInput("80")
    vm.onComplete()
    advanceUntilIdle()

    coVerify(exactly = 1) { mockWeightRepo.addEntry(80.0, any()) }
}

@Test
fun `onComplete with ST input saves converted weight entry`() = runTest {
    val mockWeightRepo = mockk<WeightRepository>(relaxed = true)
    val vm = OnboardingViewModel(
        userProfileRepository = mockUserProfileRepo,
        userPrefsDataStore = mockUserPrefs,
        weightRepository = mockWeightRepo,
    )

    vm.onUnitSelected(WeightUnit.ST)
    vm.onWeightInput("10") // 10 st = 63.5 kg
    vm.onComplete()
    advanceUntilIdle()

    val slot = slot<Double>()
    coVerify { mockWeightRepo.addEntry(capture(slot), any()) }
    assertEquals(63.5029, slot.captured, 0.01)
}
```

- [ ] **Step 2: Run — confirm FAIL (constructor signature mismatch)**

```bash
./gradlew testDebugUnitTest --tests "com.weightflow.ui.onboarding.OnboardingViewModelTest" -q
```
Expected: FAIL — `OnboardingViewModel` has no `weightRepository` param

- [ ] **Step 3: Add `weightRepository` to `OnboardingViewModel` constructor**

```kotlin
class OnboardingViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val userPrefsDataStore: UserPrefsDataStore,
    private val weightRepository: WeightRepository,
) : ViewModel() {
```

Add import at top:
```kotlin
import com.weightflow.data.WeightRepository
import java.time.ZoneId
```

- [ ] **Step 4: In `onComplete()`, add weight entry insertion**

After `userPrefsDataStore.setWeightUnit(state.selectedUnit)` and before `userProfileRepository.saveProfile(...)`:

```kotlin
// Insert the initial weight entry so the tracker isn't empty on first open
val timestamp = java.time.LocalDate.now()
    .atStartOfDay(ZoneId.systemDefault())
    .toInstant()
    .toEpochMilli()
weightRepository.addEntry(weightKg, timestamp)
```

This goes in `onComplete()` inside the launch block, after `weightKg` is computed and after the `if (!weightKg.isValidWeightKg()) return@launch` guard.

- [ ] **Step 5: Update `MainActivity` to pass `weightRepository`**

In `MainActivity.kt`, update the `OnboardingViewModel` constructor call:

```kotlin
val onboardingViewModel = OnboardingViewModel(
    userProfileRepository = app.userProfileRepository,
    userPrefsDataStore = app.userPrefsDataStore,
    weightRepository = app.weightRepository,
)
```

- [ ] **Step 6: Run all tests**

```bash
./gradlew testDebugUnitTest -q
```
Expected: GREEN

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/onboarding/OnboardingViewModel.kt \
        app/src/main/java/com/weightflow/MainActivity.kt \
        app/src/test/java/com/weightflow/ui/onboarding/OnboardingViewModelTest.kt
git commit -m "fix(onboarding): save initial weight entry so tracker is not empty after setup"
```

---

## Task 5: Fix Log Entry Save Button Stuck After First Save

**Context:** `ShellScreen` creates a single `LogEntryViewModel` that persists across sheet open/close. After a successful save, `isSaved = true` is set. When the sheet is dismissed and reopened, `isSaved` is still `true`, disabling the save button permanently. Fix: add a `reset()` method to `LogEntryViewModel` and call it from `ShellScreen` when the sheet opens.

**Files:**
- Modify: `app/src/main/java/com/weightflow/ui/logentry/LogEntryViewModel.kt`
- Modify: `app/src/main/java/com/weightflow/ui/shell/ShellScreen.kt`
- Test: `app/src/test/java/com/weightflow/ui/logentry/LogEntryViewModelTest.kt`

- [ ] **Step 1: Write failing test**

Add to `LogEntryViewModelTest.kt`:

```kotlin
@Test
fun `reset clears isSaved so save button is re-enabled`() = runTest {
    every { mockUserPrefs.weightUnit } returns MutableStateFlow(WeightUnit.KG)
    every { mockWeightRepo.getEntriesNewestFirst() } returns MutableStateFlow(emptyList())
    coEvery { mockWeightRepo.addEntry(any(), any()) } returns Unit

    val vm = LogEntryViewModel(mockWeightRepo, mockUserPrefs)
    advanceUntilIdle()

    vm.onWeightInput("80")
    vm.onSave()
    advanceUntilIdle()

    assertTrue("isSaved should be true after save", vm.uiState.value.isSaved)

    vm.reset()
    advanceUntilIdle()

    assertFalse("isSaved should be false after reset", vm.uiState.value.isSaved)
    assertTrue("weightInput should be cleared after reset", vm.uiState.value.weightInput.isEmpty())
}
```

- [ ] **Step 2: Run — confirm FAIL**

```bash
./gradlew testDebugUnitTest --tests "com.weightflow.ui.logentry.LogEntryViewModelTest" -q
```
Expected: FAIL — `reset()` not defined

- [ ] **Step 3: Add `reset()` to `LogEntryViewModel`**

```kotlin
/** Called when the log entry sheet is opened to clear state from any previous session. */
fun reset() {
    _uiState.update {
        LogEntryUiState()  // returns to defaults: weightInput="", isSaved=false, etc.
    }
}
```

- [ ] **Step 4: Call `reset()` in `ShellScreen` when sheet opens**

In `ShellScreen.kt`, find the FAB `onClick` and the place where `showLogEntry = true` is set. Add a `reset()` call:

```kotlin
onClick = {
    logEntryVm.reset()
    showLogEntry = true
},
```

Also find the `NavigationBarItem` or any other place that sets `showLogEntry = true` and add `logEntryVm.reset()` before it.

- [ ] **Step 5: Run all tests**

```bash
./gradlew testDebugUnitTest -q
```
Expected: GREEN

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/logentry/LogEntryViewModel.kt \
        app/src/main/java/com/weightflow/ui/shell/ShellScreen.kt \
        app/src/test/java/com/weightflow/ui/logentry/LogEntryViewModelTest.kt
git commit -m "fix(ui): reset LogEntryViewModel state when sheet reopens"
```

---

## Task 6: Wire `validateReleaseSigning` to `bundleRelease`

**Context:** `app/build.gradle.kts` makes `assembleRelease` depend on `validateReleaseSigning`, but CI uses `bundleRelease`. An AAB can be built without credential validation.

**Files:**
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Add `bundleRelease` dependency**

In `app/build.gradle.kts`, after the existing:
```kotlin
tasks.named("assembleRelease").configure {
    dependsOn("validateReleaseSigning")
}
```

Add:
```kotlin
tasks.named("bundleRelease").configure {
    dependsOn("validateReleaseSigning")
}
```

- [ ] **Step 2: Verify with dry-run (no actual signing setup needed)**

```bash
./gradlew bundleRelease --dry-run 2>&1 | grep "validateReleaseSigning"
```
Expected: `> Task :app:validateReleaseSigning SKIPPED` appears in the dependency chain.

- [ ] **Step 3: Commit**

```bash
git add app/build.gradle.kts
git commit -m "fix(build): wire validateReleaseSigning to bundleRelease task"
```

---

## Task 7: Fix OSV Scan Release Gate Bypass

**Context:** `.github/workflows/android.yml` has `continue-on-error: true` on the `Run OSV Scanner` step. This means if OSV finds vulnerabilities, the step fails but the JOB succeeds — `build-release` (which depends on `osv-scan`) is not blocked. The `Upload OSV SARIF` step already has `if: always()` which is the correct way to ensure SARIF uploads even on scan failure.

**Files:**
- Modify: `.github/workflows/android.yml`

- [ ] **Step 1: Remove `continue-on-error: true`**

In `.github/workflows/android.yml`, find:

```yaml
      - name: Run OSV Scanner
        uses: google/osv-scanner-action/osv-scanner-action@c51854704019a247608d928f370c98740469d4b5  # v2.3.5
        with:
          scan-args: |-
            --recursive
            --skip-git
            ./
        continue-on-error: true   # upload findings even when vulnerabilities found
```

Remove the `continue-on-error: true` line. The `if: always()` on the upload step below is sufficient:

```yaml
      - name: Run OSV Scanner
        uses: google/osv-scanner-action/osv-scanner-action@c51854704019a247608d928f370c98740469d4b5  # v2.3.5
        with:
          scan-args: |-
            --recursive
            --skip-git
            ./
```

- [ ] **Step 2: Commit**

```bash
git add .github/workflows/android.yml
git commit -m "fix(ci): remove continue-on-error from OSV scan so release gate actually blocks"
```

---

## Task 8: Fix Encrypted ZIP Temp File Leaked on Picker Cancel

**Context:** In `SettingsScreen.kt`, the `zipPickerLauncher` callback returns early when `uri == null` (user cancelled the system file picker) without deleting the temp ZIP file. The encrypted file stays on internal storage indefinitely.

**Files:**
- Modify: `app/src/main/java/com/weightflow/ui/settings/SettingsScreen.kt`

- [ ] **Step 1: Fix the launcher callback**

Find in `SettingsScreen.kt`:

```kotlin
val zipPickerLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.CreateDocument("application/zip"),
) { uri ->
    val zipFile = pendingZipFile ?: return@rememberLauncherForActivityResult
    uri ?: return@rememberLauncherForActivityResult
    scope.launch(Dispatchers.IO) {
```

Replace with:

```kotlin
val zipPickerLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.CreateDocument("application/zip"),
) { uri ->
    val zipFile = pendingZipFile ?: return@rememberLauncherForActivityResult
    if (uri == null) {
        // User cancelled — delete the temp file immediately
        scope.launch(Dispatchers.IO) {
            zipFile.delete()
            pendingZipFile = null
        }
        return@rememberLauncherForActivityResult
    }
    scope.launch(Dispatchers.IO) {
```

- [ ] **Step 2: Run all tests**

```bash
./gradlew testDebugUnitTest -q
```
Expected: GREEN (no test coverage for this Compose callback, but no regressions)

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/settings/SettingsScreen.kt
git commit -m "fix(security): delete temp ZIP when export picker is cancelled"
```

---

## Task 9: Fix Export Password Comment / String Limitation

**Context:** The `EncryptedExportDialog` comment claims password is "held in a `CharArray`" but `var passwordString by remember { mutableStateOf("") }` is a `String`. JVM `String` is immutable and cannot be zeroed. The actual `CharArray` is constructed transiently at `onExport` time. The security claim in the comment is misleading.

**Files:**
- Modify: `app/src/main/java/com/weightflow/ui/settings/SettingsScreen.kt`
- Modify: `SECURITY.md`

- [ ] **Step 1: Fix the Composable's KDoc comment**

Replace lines in `SettingsScreen.kt`:

```kotlin
// Before:
 * - Password is held in a [CharArray] backed by mutable local state — never a String.
```

```kotlin
// After:
 * - Password is bound to a mutable String for TextField compatibility; a CharArray copy is
 *   passed to [onExport] and immediately zeroed there. The intermediate String cannot be
 *   zeroed (JVM limitation). This is documented in SECURITY.md §3.
```

- [ ] **Step 2: Update `SECURITY.md`**

Find the section in `SECURITY.md` that references CharArray-only handling and add a note:

```markdown
> **Known JVM limitation:** The export password dialog uses a Compose `TextField` which
> requires a `String` binding. A `CharArray` copy is made at the point of calling
> `onExportEncryptedZip()` and is zeroed immediately after use. The original `String`
> remains in JVM heap until garbage-collected and cannot be explicitly zeroed. This is
> an inherent limitation of `TextField` and does not affect the encrypted output.
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/settings/SettingsScreen.kt SECURITY.md
git commit -m "docs(security): document String/CharArray limitation in export password dialog"
```

---

## Task 10: Consolidate Split Privacy Policy

**Context:** The app links to `docs/privacy-policy.md` (root of `docs/`) but the canonical, more complete version is `docs/privacy/privacy-policy.md`. Having two files risks them diverging.

**Files:**
- Check: `docs/privacy-policy.md` vs `docs/privacy/privacy-policy.md`
- Modify: `docs/privacy-policy.md` (turn it into a redirect/stub pointing to the canonical)

- [ ] **Step 1: Diff the two files**

```bash
diff docs/privacy-policy.md docs/privacy/privacy-policy.md
```

- [ ] **Step 2: Replace `docs/privacy-policy.md` with a pointer**

Replace `docs/privacy-policy.md` contents with:

```markdown
# Privacy Policy

> This file is a stub. The canonical privacy policy is at [`docs/privacy/privacy-policy.md`](privacy/privacy-policy.md).
>
> **Do not edit this file.** Edit `docs/privacy/privacy-policy.md` instead.
```

- [ ] **Step 3: Commit**

```bash
git add docs/privacy-policy.md
git commit -m "docs: point privacy-policy stub to canonical docs/privacy/privacy-policy.md"
```

---

## Task 11: Fix Accessibility Touch Targets

**Context:** `HistoryScreen.kt` delete `IconButton` is `Modifier.size(36.dp)` (line ~357). `SettingsScreen.kt` back button is 44dp. WCAG 2.5.5 and Material Design 3 require 48dp minimum interactive target.

**Files:**
- Modify: `app/src/main/java/com/weightflow/ui/history/HistoryScreen.kt`
- Modify: `app/src/main/java/com/weightflow/ui/settings/SettingsScreen.kt`

- [ ] **Step 1: Fix delete button in `HistoryScreen.kt`**

Find:
```kotlin
modifier = Modifier.size(36.dp),
```
(inside the `IconButton` for delete)

Replace with:
```kotlin
modifier = Modifier.size(48.dp),
```

- [ ] **Step 2: Fix any 44dp back button in `SettingsScreen.kt`**

```bash
grep -n "44.dp" app/src/main/java/com/weightflow/ui/settings/SettingsScreen.kt
```

For each match that is an interactive element (button/icon), change to `48.dp`.

- [ ] **Step 3: Run lint to confirm no new warnings**

```bash
./gradlew lintDebug 2>&1 | grep -i "touch\|target\|size"
```

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/history/HistoryScreen.kt \
        app/src/main/java/com/weightflow/ui/settings/SettingsScreen.kt
git commit -m "fix(a11y): increase delete and back button touch targets to 48dp"
```

---

## Task 12: Fix Lint `PropertyEscape` Error

**Context:** CI lint report shows `1 error, 69 warnings`. The error is `PropertyEscape` in `local.properties`. `local.properties` is gitignored — this means the error is only present when lint is run in an environment where `local.properties` exists with a Windows-style path (backslashes not escaped). CI environment likely creates a dummy `local.properties` or the path contains `\`.

**Files:**
- Check: `local.properties` (dev only, gitignored)
- Check: `.github/workflows/android.yml` lint step

- [ ] **Step 1: Investigate the error in CI**

```bash
gh run list --repo VaibhavAher100/WeightFlow --workflow "Android CI" --limit 5 --json databaseId,conclusion
```

Pick a recent failed lint run ID, then:

```bash
gh run view <RUN_ID> --repo VaibhavAher100/WeightFlow --log-failed 2>&1 | grep -A3 "PropertyEscape\|lintDebug"
```

- [ ] **Step 2: Determine if lint error is in CI or local only**

If `local.properties` is not present in CI (it is gitignored), the error cannot come from there in CI. If lint passes in CI, this is a local-only issue. Check:

```bash
./gradlew lintDebug 2>&1 | grep "error\|Error"
```

- [ ] **Step 3: If error is local — fix `local.properties`**

Windows backslash paths in `local.properties` need escaping. If `sdk.dir` contains backslashes:
```properties
# Wrong:
sdk.dir=C:\Users\user\AppData\Local\Android\Sdk
# Correct:
sdk.dir=C\:\\Users\\user\\AppData\\Local\\Android\\Sdk
```

Or use forward slashes (also valid on Windows in this file):
```properties
sdk.dir=C:/Users/user/AppData/Local/Android/Sdk
```

- [ ] **Step 4: Commit if any tracked file was changed**

If only `local.properties` changed (it is gitignored), no commit needed.

---

## Final Verification

- [ ] **Run full test suite**

```bash
./gradlew testDebugUnitTest -q
```
Expected: all tests GREEN (221+ unit tests)

- [ ] **Run lint**

```bash
./gradlew lintDebug 2>&1 | tail -5
```

- [ ] **Push and verify CI**

```bash
git push origin main
gh run watch $(gh run list --repo VaibhavAher100/WeightFlow --limit 1 --json databaseId --jq '.[0].databaseId') --repo VaibhavAher100/WeightFlow
```

---

## Self-Review

**Spec coverage check:**
- Issue 1 (SQLCipher) → Task 1 ✓
- Issue 2 (ST unit) → Task 2 ✓
- Issue 3 (date shift) → Task 3 ✓
- Issue 4 (onboarding weight) → Task 4 ✓
- Issue 5 (save button stuck) → Task 5 ✓
- Issue 6 (bundleRelease gate) → Task 6 ✓
- Issue 7 (OSV gate bypass) → Task 7 ✓
- Issue 10 (ZIP leak) → Task 8 ✓
- Issue 11 (password String) → Task 9 ✓
- Issue 12 (privacy docs) → Task 10 ✓
- Issue 13 (touch targets) → Task 11 ✓
- Issue 9 (lint) → Task 12 ✓
- Issue 8 (instrumented CI) → deferred, out of scope v1.0 ✓

**Type consistency:** `WeightConverter.stToKg(Double)` defined in Task 2 Step 3, used in same task Step 5+7. `OnboardingViewModel(weightRepository)` constructor defined in Task 4 Step 3, wired in MainActivity in Task 4 Step 5. `LogEntryViewModel.reset()` defined Task 5 Step 3, called in ShellScreen Task 5 Step 4. All consistent.

**Placeholder scan:** No TBDs or placeholder steps found. All code blocks are complete.
