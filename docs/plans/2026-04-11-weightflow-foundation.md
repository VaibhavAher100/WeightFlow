# WeightFlow — Plan 1: Foundation

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

> **CLI Priority:** Use `./gradlew` for all builds/tests. Use `gh` CLI for GitHub ops. Use `codex` CLI (v0.118.0) to accelerate Tasks 2–5 below — it can generate Room entities, DAOs, DataStore wrapper, and repository stubs in parallel. Use `android-room-database` and `android-kotlin` skills for patterns reference.

> **Codex Acceleration:** For Tasks 2–5, you can run `codex "…"` in a separate terminal for isolated file generation, then wire up in Claude Code. Example: `codex "create WeightEntryEntity.kt + WeightEntryDao.kt for Room 2.6.1 with suspend functions, package com.weightflow.data.db"`.

**Goal:** Create a working Android project skeleton with Room database, DataStore preferences, MVVM architecture, and navigable bottom-nav shell — no UI content yet, just the plumbing.

**Architecture:** MVVM with Repository pattern. Room is the single source of truth; DataStore holds user preferences (theme, unit, mode). Manual DI via Application class — no Hilt, keeps complexity down for a solo project.

**Tech Stack:** Kotlin 2.x · Jetpack Compose (BOM 2024.09+) · Room 2.6.1 · DataStore 1.1.1 · Navigation Compose 2.7.7 · Coroutines 1.7.3

---

## File Map

```
WeightFlow/
├── app/
│   ├── build.gradle.kts                    ← dependencies, plugins
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   └── java/com/weightflow/
│       │       ├── WeightFlowApp.kt         ← Application class, manual DI root
│       │       ├── MainActivity.kt          ← single activity, hosts NavHost
│       │       ├── data/
│       │       │   ├── db/
│       │       │   │   ├── AppDatabase.kt   ← Room database singleton
│       │       │   │   ├── WeightEntryEntity.kt
│       │       │   │   ├── WeightEntryDao.kt
│       │       │   │   ├── UserProfileEntity.kt
│       │       │   │   └── UserProfileDao.kt
│       │       │   ├── prefs/
│       │       │   │   └── UserPrefsDataStore.kt ← DataStore wrapper
│       │       │   └── repository/
│       │       │       ├── WeightRepository.kt
│       │       │       └── UserProfileRepository.kt
│       │       └── ui/
│       │           ├── navigation/
│       │           │   ├── Screen.kt        ← sealed class of routes
│       │           │   └── NavGraph.kt      ← NavHost with bottom nav
│       │           ├── theme/
│       │           │   ├── Color.kt         ← 8 accent palettes + neutrals
│       │           │   ├── Theme.kt         ← dynamic MaterialTheme wrapper
│       │           │   └── Type.kt          ← typography (Bebas Neue + Outfit)
│       │           └── shell/
│       │               └── ShellScreen.kt   ← Scaffold + BottomNavigation stub
│       ├── test/java/com/weightflow/
│       │   ├── data/WeightRepositoryTest.kt
│       │   └── data/UserProfileRepositoryTest.kt
│       └── androidTest/java/com/weightflow/
│           └── data/WeightEntryDaoTest.kt
├── build.gradle.kts                         ← project-level
└── settings.gradle.kts
```

---

## Task 1: Create Android Project + Configure Dependencies

**Files:**
- Create: `app/build.gradle.kts`
- Create: `build.gradle.kts`
- Create: `settings.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Create project in Android Studio**

  In Android Studio → New Project → Empty Activity.
  - Name: `WeightFlow`
  - Package: `com.weightflow`
  - Save location: `C:\Users\vaibh\Desktop\102\WeightFlow`
  - Language: Kotlin
  - Min SDK: API 26 (Android 8.0)
  - Build config: Gradle Kotlin DSL

  Click Finish and wait for initial sync.

- [ ] **Step 2: Replace `app/build.gradle.kts` with full dependency set**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.weightflow"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.weightflow"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
    buildFeatures { compose = true }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.5")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Vico charts
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")

    // Core
    implementation("androidx.core:core-ktx:1.13.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.room:room-testing:2.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

- [ ] **Step 3: Add KSP plugin to `build.gradle.kts` (project level)**

```kotlin
// build.gradle.kts (project level)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
}
```

- [ ] **Step 4: Sync Gradle**

  In Android Studio: File → Sync Project with Gradle Files.
  Expected: BUILD SUCCESSFUL with no red errors in the Problems pane.

- [ ] **Step 5: Commit**

```bash
cd C:\Users\vaibh\Desktop\102\WeightFlow
git add .
git commit -m "chore: initial project setup with all dependencies"
```

---

## Task 2: Room Entities + DAO

**Files:**
- Create: `app/src/main/java/com/weightflow/data/db/WeightEntryEntity.kt`
- Create: `app/src/main/java/com/weightflow/data/db/WeightEntryDao.kt`
- Create: `app/src/main/java/com/weightflow/data/db/UserProfileEntity.kt`
- Create: `app/src/main/java/com/weightflow/data/db/UserProfileDao.kt`
- Test: `app/src/androidTest/java/com/weightflow/data/WeightEntryDaoTest.kt`

- [ ] **Step 1: Write the failing DAO test**

```kotlin
// app/src/androidTest/java/com/weightflow/data/WeightEntryDaoTest.kt
package com.weightflow.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.weightflow.data.db.AppDatabase
import com.weightflow.data.db.WeightEntryDao
import com.weightflow.data.db.WeightEntryEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WeightEntryDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: WeightEntryDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.weightEntryDao()
    }

    @After
    fun tearDown() { db.close() }

    @Test
    fun insertAndRetrieveEntry() = runTest {
        val entry = WeightEntryEntity(weightKg = 82.4, timestampMs = 1000L)
        dao.insertEntry(entry)

        dao.getAllEntries().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals(82.4, list[0].weightKg, 0.01)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getLatestEntry_returnsNullWhenEmpty() = runTest {
        assertNull(dao.getLatestEntry())
    }

    @Test
    fun getLatestEntry_returnsMostRecent() = runTest {
        dao.insertEntry(WeightEntryEntity(weightKg = 85.0, timestampMs = 1000L))
        dao.insertEntry(WeightEntryEntity(weightKg = 82.4, timestampMs = 2000L))
        val latest = dao.getLatestEntry()
        assertEquals(82.4, latest!!.weightKg, 0.01)
    }

    @Test
    fun deleteEntry_removesFromDb() = runTest {
        val entry = WeightEntryEntity(weightKg = 82.4, timestampMs = 1000L)
        val id = dao.insertEntry(entry)
        val inserted = WeightEntryEntity(id = id, weightKg = 82.4, timestampMs = 1000L)
        dao.deleteEntry(inserted)

        dao.getAllEntries().test {
            assertEquals(0, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

  In Android Studio: right-click `WeightEntryDaoTest` → Run.
  Expected: FAIL — `AppDatabase`, `WeightEntryEntity`, `WeightEntryDao` don't exist yet.

- [ ] **Step 3: Create `WeightEntryEntity`**

```kotlin
// app/src/main/java/com/weightflow/data/db/WeightEntryEntity.kt
package com.weightflow.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_entries")
data class WeightEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weightKg: Double,
    val bodyFatPercent: Double? = null,
    val muscleMassKg: Double? = null,
    val notes: String? = null,
    val timestampMs: Long = System.currentTimeMillis()
)
```

- [ ] **Step 4: Create `WeightEntryDao`**

```kotlin
// app/src/main/java/com/weightflow/data/db/WeightEntryDao.kt
package com.weightflow.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightEntryDao {

    @Query("SELECT * FROM weight_entries ORDER BY timestampMs DESC")
    fun getAllEntries(): Flow<List<WeightEntryEntity>>

    @Query("SELECT * FROM weight_entries WHERE timestampMs >= :fromMs ORDER BY timestampMs ASC")
    fun getEntriesFrom(fromMs: Long): Flow<List<WeightEntryEntity>>

    @Query("SELECT * FROM weight_entries ORDER BY timestampMs DESC LIMIT 1")
    suspend fun getLatestEntry(): WeightEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: WeightEntryEntity): Long

    @Update
    suspend fun updateEntry(entry: WeightEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: WeightEntryEntity)
}
```

- [ ] **Step 5: Create `UserProfileEntity`**

```kotlin
// app/src/main/java/com/weightflow/data/db/UserProfileEntity.kt
package com.weightflow.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,     // single-row table
    val name: String = "You",
    val heightCm: Double = 170.0,
    val birthYear: Int = 1998,
    val startWeightKg: Double = 0.0,
    val goalWeightKg: Double = 0.0,
    val goalType: String = "lose"    // "lose" | "maintain" | "gain"
)
```

- [ ] **Step 6: Create `UserProfileDao`**

```kotlin
// app/src/main/java/com/weightflow/data/db/UserProfileDao.kt
package com.weightflow.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfileEntity)
}
```

- [ ] **Step 7: Create `AppDatabase`**

```kotlin
// app/src/main/java/com/weightflow/data/db/AppDatabase.kt
package com.weightflow.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [WeightEntryEntity::class, UserProfileEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun weightEntryDao(): WeightEntryDao
    abstract fun userProfileDao(): UserProfileDao
}
```

- [ ] **Step 8: Run test to verify it passes**

  In Android Studio: right-click `WeightEntryDaoTest` → Run.
  Expected: All 4 tests GREEN.

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/weightflow/data/db/ \
        app/src/androidTest/java/com/weightflow/data/WeightEntryDaoTest.kt
git commit -m "feat: Room entities and DAOs for weight entries and user profile"
```

---

## Task 3: Repository Layer

**Files:**
- Create: `app/src/main/java/com/weightflow/data/repository/WeightRepository.kt`
- Create: `app/src/main/java/com/weightflow/data/repository/UserProfileRepository.kt`
- Test: `app/src/test/java/com/weightflow/data/WeightRepositoryTest.kt`

- [ ] **Step 1: Write the failing unit test**

```kotlin
// app/src/test/java/com/weightflow/data/WeightRepositoryTest.kt
package com.weightflow.data

import app.cash.turbine.test
import com.weightflow.data.db.WeightEntryDao
import com.weightflow.data.db.WeightEntryEntity
import com.weightflow.data.repository.WeightRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class WeightRepositoryTest {

    private val dao = mockk<WeightEntryDao>()
    private val repo = WeightRepository(dao)

    @Test
    fun getAllEntries_delegatesToDao() = runTest {
        val entries = listOf(
            WeightEntryEntity(id = 1, weightKg = 82.4, timestampMs = 2000L),
            WeightEntryEntity(id = 2, weightKg = 82.7, timestampMs = 1000L)
        )
        every { dao.getAllEntries() } returns flowOf(entries)

        repo.getAllEntries().test {
            assertEquals(2, awaitItem().size)
            awaitComplete()
        }
    }

    @Test
    fun insertEntry_callsDaoInsert() = runTest {
        coEvery { dao.insertEntry(any()) } returns 1L
        repo.insertEntry(weightKg = 82.4)
        coVerify { dao.insertEntry(match { it.weightKg == 82.4 }) }
    }

    @Test
    fun deleteEntry_callsDaoDelete() = runTest {
        val entity = WeightEntryEntity(id = 1, weightKg = 82.4, timestampMs = 1000L)
        coEvery { dao.deleteEntry(entity) } returns Unit
        repo.deleteEntry(entity)
        coVerify { dao.deleteEntry(entity) }
    }

    @Test
    fun getLatestEntry_returnsNullWhenEmpty() = runTest {
        coEvery { dao.getLatestEntry() } returns null
        assertEquals(null, repo.getLatestEntry())
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

  In Android Studio: right-click `WeightRepositoryTest` → Run.
  Expected: FAIL — `WeightRepository` doesn't exist yet.

- [ ] **Step 3: Create `WeightRepository`**

```kotlin
// app/src/main/java/com/weightflow/data/repository/WeightRepository.kt
package com.weightflow.data.repository

import com.weightflow.data.db.WeightEntryDao
import com.weightflow.data.db.WeightEntryEntity
import kotlinx.coroutines.flow.Flow

class WeightRepository(private val dao: WeightEntryDao) {

    fun getAllEntries(): Flow<List<WeightEntryEntity>> = dao.getAllEntries()

    fun getEntriesFrom(fromMs: Long): Flow<List<WeightEntryEntity>> =
        dao.getEntriesFrom(fromMs)

    suspend fun getLatestEntry(): WeightEntryEntity? = dao.getLatestEntry()

    suspend fun insertEntry(
        weightKg: Double,
        bodyFatPercent: Double? = null,
        muscleMassKg: Double? = null,
        notes: String? = null,
        timestampMs: Long = System.currentTimeMillis()
    ) {
        dao.insertEntry(
            WeightEntryEntity(
                weightKg = weightKg,
                bodyFatPercent = bodyFatPercent,
                muscleMassKg = muscleMassKg,
                notes = notes,
                timestampMs = timestampMs
            )
        )
    }

    suspend fun updateEntry(entry: WeightEntryEntity) = dao.updateEntry(entry)

    suspend fun deleteEntry(entry: WeightEntryEntity) = dao.deleteEntry(entry)
}
```

- [ ] **Step 4: Create `UserProfileRepository`**

```kotlin
// app/src/main/java/com/weightflow/data/repository/UserProfileRepository.kt
package com.weightflow.data.repository

import com.weightflow.data.db.UserProfileDao
import com.weightflow.data.db.UserProfileEntity
import kotlinx.coroutines.flow.Flow

class UserProfileRepository(private val dao: UserProfileDao) {

    fun getProfile(): Flow<UserProfileEntity?> = dao.getProfile()

    suspend fun getProfileOnce(): UserProfileEntity? = dao.getProfileOnce()

    suspend fun upsertProfile(profile: UserProfileEntity) = dao.upsertProfile(profile)

    /** Creates a default profile on first launch if none exists. */
    suspend fun initDefaultProfile() {
        if (dao.getProfileOnce() == null) {
            dao.upsertProfile(UserProfileEntity())
        }
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

  Expected: All 4 tests GREEN.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/weightflow/data/repository/ \
        app/src/test/java/com/weightflow/data/WeightRepositoryTest.kt
git commit -m "feat: repository layer wrapping Room DAOs"
```

---

## Task 4: DataStore Preferences

**Files:**
- Create: `app/src/main/java/com/weightflow/data/prefs/UserPrefsDataStore.kt`
- Test: `app/src/test/java/com/weightflow/data/UserPrefsDataStoreTest.kt`

- [ ] **Step 1: Write failing test**

```kotlin
// app/src/test/java/com/weightflow/data/UserPrefsDataStoreTest.kt
package com.weightflow.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import app.cash.turbine.test
import com.weightflow.data.prefs.AccentColor
import com.weightflow.data.prefs.UserPrefsDataStore
import com.weightflow.data.prefs.WeightUnit
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class UserPrefsDataStoreTest {

    @get:Rule val tmpFolder = TemporaryFolder()

    private fun makeDataStore(scope: TestScope): UserPrefsDataStore {
        val store = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { tmpFolder.newFile("test_prefs.preferences_pb") }
        )
        return UserPrefsDataStore(store)
    }

    @Test
    fun defaultWeightUnit_isKg() = runTest {
        val prefs = makeDataStore(this)
        prefs.weightUnit.test {
            assertEquals(WeightUnit.KG, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setAccentColor_persists() = runTest {
        val prefs = makeDataStore(this)
        prefs.setAccentColor(AccentColor.CORAL)
        prefs.accentColor.test {
            assertEquals(AccentColor.CORAL, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun darkMode_defaultsToTrue() = runTest {
        val prefs = makeDataStore(this)
        prefs.isDarkMode.test {
            assertEquals(true, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun proMode_defaultsToFalse() = runTest {
        val prefs = makeDataStore(this)
        prefs.isProMode.test {
            assertEquals(false, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

  Expected: FAIL — `UserPrefsDataStore`, `AccentColor`, `WeightUnit` don't exist.

- [ ] **Step 3: Create `UserPrefsDataStore`**

```kotlin
// app/src/main/java/com/weightflow/data/prefs/UserPrefsDataStore.kt
package com.weightflow.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class AccentColor { LIME, CORAL, CYAN, PINK, VIOLET, YELLOW, MINT, RED }
enum class WeightUnit  { KG, LBS, STONE }

class UserPrefsDataStore(private val store: DataStore<Preferences>) {

    companion object {
        private val KEY_ACCENT    = stringPreferencesKey("accent_color")
        private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        private val KEY_PRO_MODE  = booleanPreferencesKey("pro_mode")
        private val KEY_UNIT      = stringPreferencesKey("weight_unit")
    }

    val accentColor: Flow<AccentColor> = store.data.map { prefs ->
        prefs[KEY_ACCENT]?.let { runCatching { AccentColor.valueOf(it) }.getOrNull() }
            ?: AccentColor.LIME
    }

    val isDarkMode: Flow<Boolean> = store.data.map { prefs ->
        prefs[KEY_DARK_MODE] ?: true
    }

    val isProMode: Flow<Boolean> = store.data.map { prefs ->
        prefs[KEY_PRO_MODE] ?: false
    }

    val weightUnit: Flow<WeightUnit> = store.data.map { prefs ->
        prefs[KEY_UNIT]?.let { runCatching { WeightUnit.valueOf(it) }.getOrNull() }
            ?: WeightUnit.KG
    }

    suspend fun setAccentColor(color: AccentColor) {
        store.edit { it[KEY_ACCENT] = color.name }
    }

    suspend fun setDarkMode(dark: Boolean) {
        store.edit { it[KEY_DARK_MODE] = dark }
    }

    suspend fun setProMode(pro: Boolean) {
        store.edit { it[KEY_PRO_MODE] = pro }
    }

    suspend fun setWeightUnit(unit: WeightUnit) {
        store.edit { it[KEY_UNIT] = unit.name }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

  Expected: All 4 tests GREEN.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/weightflow/data/prefs/ \
        app/src/test/java/com/weightflow/data/UserPrefsDataStoreTest.kt
git commit -m "feat: DataStore preferences for accent color, dark mode, units"
```

---

## Task 5: Application Class + Manual DI Root

**Files:**
- Create: `app/src/main/java/com/weightflow/WeightFlowApp.kt`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Create `WeightFlowApp`**

```kotlin
// app/src/main/java/com/weightflow/WeightFlowApp.kt
package com.weightflow

import android.app.Application
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.weightflow.data.db.AppDatabase
import com.weightflow.data.prefs.UserPrefsDataStore
import com.weightflow.data.repository.UserProfileRepository
import com.weightflow.data.repository.WeightRepository

private val Application.dataStore by preferencesDataStore(name = "user_prefs")

class WeightFlowApp : Application() {

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "weightflow.db"
        ).build()
    }

    val weightRepository: WeightRepository by lazy {
        WeightRepository(database.weightEntryDao())
    }

    val userProfileRepository: UserProfileRepository by lazy {
        UserProfileRepository(database.userProfileDao())
    }

    val userPrefs: UserPrefsDataStore by lazy {
        UserPrefsDataStore(dataStore)
    }
}
```

- [ ] **Step 2: Register in `AndroidManifest.xml`**

  Add `android:name=".WeightFlowApp"` to the `<application>` tag:

```xml
<application
    android:name=".WeightFlowApp"
    android:allowBackup="true"
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
```

- [ ] **Step 3: Build project**

  In Android Studio: Build → Make Project.
  Expected: BUILD SUCCESSFUL, no errors.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/weightflow/WeightFlowApp.kt \
        app/src/main/AndroidManifest.xml
git commit -m "feat: Application class with manual DI (database, repositories, prefs)"
```

---

## Task 6: Theme System — Colors + Typography

**Files:**
- Create: `app/src/main/java/com/weightflow/ui/theme/Color.kt`
- Create: `app/src/main/java/com/weightflow/ui/theme/Type.kt`
- Create: `app/src/main/java/com/weightflow/ui/theme/Theme.kt`

No unit tests for theme — verified visually in Task 8.

- [ ] **Step 1: Add Google Fonts dependency**

  In `app/build.gradle.kts`, add inside `dependencies {}`:

```kotlin
implementation("androidx.compose.ui:ui-text-google-fonts")
```

  Sync Gradle.

- [ ] **Step 2: Create `Color.kt`**

```kotlin
// app/src/main/java/com/weightflow/ui/theme/Color.kt
package com.weightflow.ui.theme

import androidx.compose.ui.graphics.Color
import com.weightflow.data.prefs.AccentColor

// Neutral dark palette
val BgBase     = Color(0xFF0F0E0B)
val BgCard     = Color(0xFF1C1B18)
val BgElevated = Color(0xFF252420)
val BgSurface  = Color(0xFF2E2D28)

val TextPrimary   = Color(0xFFF2F0E8)
val TextSecondary = Color(0xFF8C8A80)
val TextTertiary  = Color(0xFF504E47)

val SuccessGreen = Color(0xFF4ED98A)
val DangerRed    = Color(0xFFFF6B6B)
val BorderSubtle = Color(0x0FFFFFFF)   // ~6% white

// Light palette (for light mode toggle — future)
val LightBg    = Color(0xFFF7F6F2)
val LightCard  = Color(0xFFFFFFFF)
val LightText  = Color(0xFF1A1916)

// 8 accent colors
fun AccentColor.toColor(): Color = when (this) {
    AccentColor.LIME   -> Color(0xFFC8FF00)
    AccentColor.CORAL  -> Color(0xFFFF6B35)
    AccentColor.CYAN   -> Color(0xFF00D4FF)
    AccentColor.PINK   -> Color(0xFFFF3CAC)
    AccentColor.VIOLET -> Color(0xFF7B61FF)
    AccentColor.YELLOW -> Color(0xFFFFD600)
    AccentColor.MINT   -> Color(0xFF00FF88)
    AccentColor.RED    -> Color(0xFFFF4757)
}

// On-accent text color (dark text reads well on all 8 accents)
val OnAccent = Color(0xFF0F0E0B)
```

- [ ] **Step 3: Create `Type.kt`**

```kotlin
// app/src/main/java/com/weightflow/ui/theme/Type.kt
package com.weightflow.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp

private val googleFontsProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = com.weightflow.R.array.com_google_android_gms_fonts_certs
)

val BebasNeue = FontFamily(
    Font(GoogleFont("Bebas Neue"), googleFontsProvider)
)

val Outfit = FontFamily(
    Font(GoogleFont("Outfit"), googleFontsProvider, weight = FontWeight.Normal),
    Font(GoogleFont("Outfit"), googleFontsProvider, weight = FontWeight.Medium),
    Font(GoogleFont("Outfit"), googleFontsProvider, weight = FontWeight.SemiBold),
    Font(GoogleFont("Outfit"), googleFontsProvider, weight = FontWeight.Bold),
    Font(GoogleFont("Outfit"), googleFontsProvider, weight = FontWeight.ExtraBold),
)

val WeightFlowTypography = Typography(
    displayLarge  = TextStyle(fontFamily = BebasNeue, fontSize = 72.sp, letterSpacing = 2.sp),
    displayMedium = TextStyle(fontFamily = BebasNeue, fontSize = 48.sp, letterSpacing = 1.sp),
    displaySmall  = TextStyle(fontFamily = BebasNeue, fontSize = 32.sp),
    headlineLarge = TextStyle(fontFamily = Outfit, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp),
    headlineMedium= TextStyle(fontFamily = Outfit, fontWeight = FontWeight.Bold, fontSize = 20.sp),
    titleLarge    = TextStyle(fontFamily = Outfit, fontWeight = FontWeight.Bold, fontSize = 18.sp),
    titleMedium   = TextStyle(fontFamily = Outfit, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
    bodyLarge     = TextStyle(fontFamily = Outfit, fontWeight = FontWeight.Normal, fontSize = 15.sp),
    bodyMedium    = TextStyle(fontFamily = Outfit, fontWeight = FontWeight.Normal, fontSize = 13.sp),
    labelLarge    = TextStyle(fontFamily = Outfit, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp),
    labelSmall    = TextStyle(fontFamily = Outfit, fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = 1.5.sp),
)
```

- [ ] **Step 4: Add `fonts_certs` resource** (required for Google Fonts)

  Create `app/src/main/res/values/font_certs.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <array name="com_google_android_gms_fonts_certs">
        <item>@array/com_google_android_gms_fonts_certs_dev</item>
        <item>@array/com_google_android_gms_fonts_certs_prod</item>
    </array>
    <string-array name="com_google_android_gms_fonts_certs_dev">
        <item>MIIEqDCCA5CgAwIBAgIJANWFuGx90071MA0GCSqGSIb3DQEBBAUAMIGUMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNTW91bnRhaW4gVmlldzEQMA4GA1UEChMHQW5kcm9pZDEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDEiMCAGCSqGSIb3DQEJARYTYW5kcm9pZEBhbmRyb2lkLmNvbTAeFw0wODA0MTUyMzM2NTZaFw0zNTA5MDEyMzM2NTZaMIGUMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNTW91bnRhaW4gVmlldzEQMA4GA1UEChMHQW5kcm9pZDEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDEiMCAGCSqGSIb3DQEJARYTYW5kcm9pZEBhbmRyb2lkLmNvbTCCASAwDQYJKoZIhvcNAQEBBQADggENADCCAQgCggEBANbOLggKv+IxTdGNs8/TGFy0PTP6DHThvttMlikosqbtZ/hFQ3yDeAT3d6MjhFYgGqFAXtjX3+HM7Vwl8yJXqWNxLDknBiIFCLLJIWBL5q8tqAT5rEbqFjBJCJVDCnhxKR43OTHHyNchJAKBQ4Oix5NTaIbSTbqhXZ4IKLLqBqZDOILRNT1VRlqNKllVbpAp9JfpyGNfCmMIHJEyp0PmqNxB5UNXmXOFLT87d+h2NNLS1F0YVVBZ34PFQzAlcTCUEBATqOYL+w/lU6ADLkTHOiEqEAIlSTjHw3DaNn2FwpCiA2/fK3IJqJKQEVp0KZBqZdAbQ1lGphfluqwIDAQABo4HoMIHlMB0GA1UdDgQWBBSt6MR4CLV2JIJH3tFluMoBQ2NIZTAfBgNVHSMEGDAWgBSt6MR4CLV2JIJH3tFluMoBQ2NIZTAPBgNVHRMBAf8EBTADAQH/MB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAgAwbAYDVR0RAQH/BGIwYIIJbG9jYWxob3N0ggkxMjcuMC4wLjGCBSoubG9jggsqLmFuZHJvaWQuY29tgg0qLmdvb2dsZS5jb22CDSoud2lkZXZpbmUuY29tgg0qLmdvb2dsZWFwaXMuY29tMA0GCSqGSIb3DQEBBAUAA4IBAQBbzMVfUKpGQITNeBi/7aDFSl3dTYkKSwDfGOuqoIqdJIFtXRCcjwFPISGgWCBCNyPAbYyuZHLjX+m2LFTizSfGS3TJ4tNpHMWOjcHH9J8ZRfJaOsOD7O6FmRJuT2EMoqF4hJ+MoRE94H4iyFBX4iU1rj8pFt6oeO7G5p3ZI+/cxQCz23xFVCRKkSmz7ZPRKYXyCz+1jNUj5pPfVB9K3YLMV4+gL2qTN/TqM7LNS91xrKDkDf3HbAP/vf0=</item>
    </string-array>
    <string-array name="com_google_android_gms_fonts_certs_prod">
        <item>MIIEQzCCAyugAwIBAgIJAMLgh0ZkSjCNMA0GCSqGSIb3DQEBBAUAMHQxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFBYDVQQHEw1Nb3VudGFpbiBWaWV3MRQwEgYDVQQKEwtHb29nbGUgSW5jLjEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDAeFw0wODA4MjEyMzEzMzRaFw0zNjAxMDcyMzEzMzRaMHQxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFBYDVQQHEw1Nb3VudGFpbiBWaWV3MRQwEgYDVQQKEwtHb29nbGUgSW5jLjEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDCCASAwDQYJKoZIhvcNAQEBBQADggENADCCAQgCggEBAKtWLgDYO6IIrgqWbxJOKdoR8qtW0uXM9RNWRKFNXxEDtdFUAimPJiw4qNKjHQFNalVjCY0K2O8NqbZYa1SKh1rF0J5F5MHQFHBNkJAkSS3K5KMiMKMmLiPi4BZFzlMGlT3RwKTJ3mMKEPrT0Rl1K6xOiRmrKOUe45KTBvLVPDdHB57bOklI3GOq0rFRzAEgfPKv5wgQXqcECEBfvTqF1Af6RxLAHsODFj7c5CuV3lz1xBDNQ8Iz5AgMBAAGjgdAwgc0wHQYDVR0OBBYEFMd9jMIhF1Pm3QkaELzLKXL34LpEMF8GA1UdIwRYMFaAFMd9jMIhF1Pm3QkaELzLKXL34LpEoTikNjA0MQswCQYDVQQGEwJVUzEQMA4GA1UEChMHQW5kcm9pZDETMBEGA1UEAxMKYW5kcm9pZC5jYYIJAMLgh0ZkSjCNMAwGA1UdEwQFMAMBAf8wDgYDVR0PAQH/BAQDAgKEMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQEEBQADggEBABR6gxfWCDk8VjDuA5A5hv2KK2x7O3DUioWRoLWoS7uRyScT2SFlHHSMUvJlYwcL5i5JvWCOsFqGmKOJ/j3GDQT1nMKkfE1mI3IB6zMzNYkOg6HqFNkXjJhQQOhB8UOIC4YRHyc/IMzqp3oNwAhRe+p+K9eUVJ1MOAqFXMGCm4L6eMJP6GhMaSzRpn2ydQ+dN/zz5QzQLqFn0q3+CBVRL1KFpN/CGM0fvIMMVMexvzBsLF7BMDQ3YMnCqbdJtLGtCgCaJkPsmP1P/8uGRTvv6ZMO2KzVR/oZ1lQ=</item>
    </string-array>
</resources>
```

- [ ] **Step 5: Create `Theme.kt`**

```kotlin
// app/src/main/java/com/weightflow/ui/theme/Theme.kt
package com.weightflow.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.weightflow.data.prefs.AccentColor

@Composable
fun WeightFlowTheme(
    accent: AccentColor = AccentColor.LIME,
    darkMode: Boolean = true,
    content: @Composable () -> Unit
) {
    val accentColor = accent.toColor()

    val colorScheme = darkColorScheme(
        primary          = accentColor,
        onPrimary        = OnAccent,
        background       = BgBase,
        surface          = BgCard,
        surfaceVariant   = BgElevated,
        onBackground     = TextPrimary,
        onSurface        = TextPrimary,
        onSurfaceVariant = TextSecondary,
        outline          = Color(0x0FFFFFFF),
        secondary        = SuccessGreen,
        error            = DangerRed,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = WeightFlowTypography,
        content     = content
    )
}
```

- [ ] **Step 6: Build to verify no compile errors**

  Build → Make Project. Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/theme/ \
        app/src/main/res/values/font_certs.xml
git commit -m "feat: theme system — 8 accent colors, Bebas Neue + Outfit fonts"
```

---

## Task 7: Navigation + Shell Screen

**Files:**
- Create: `app/src/main/java/com/weightflow/ui/navigation/Screen.kt`
- Create: `app/src/main/java/com/weightflow/ui/navigation/NavGraph.kt`
- Create: `app/src/main/java/com/weightflow/ui/shell/ShellScreen.kt`
- Modify: `app/src/main/java/com/weightflow/MainActivity.kt`

- [ ] **Step 1: Create `Screen.kt`**

```kotlin
// app/src/main/java/com/weightflow/ui/navigation/Screen.kt
package com.weightflow.ui.navigation

sealed class Screen(val route: String) {
    object Home     : Screen("home")
    object Trends   : Screen("trends")
    object Log      : Screen("log")
    object History  : Screen("history")
    object Profile  : Screen("profile")
    object Settings : Screen("settings")
}
```

- [ ] **Step 2: Create `NavGraph.kt`**

```kotlin
// app/src/main/java/com/weightflow/ui/navigation/NavGraph.kt
package com.weightflow.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.weightflow.ui.shell.PlaceholderScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route)     { PlaceholderScreen("Home") }
        composable(Screen.Trends.route)   { PlaceholderScreen("Trends") }
        composable(Screen.Log.route)      { PlaceholderScreen("Log") }
        composable(Screen.History.route)  { PlaceholderScreen("History") }
        composable(Screen.Profile.route)  { PlaceholderScreen("Profile") }
        composable(Screen.Settings.route) { PlaceholderScreen("Settings") }
    }
}
```

- [ ] **Step 3: Create `ShellScreen.kt`** (bottom nav + scaffold)

```kotlin
// app/src/main/java/com/weightflow/ui/shell/ShellScreen.kt
package com.weightflow.ui.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.weightflow.ui.navigation.NavGraph
import com.weightflow.ui.navigation.Screen

data class NavItem(val screen: Screen, val label: String, val icon: ImageVector)

val navItems = listOf(
    NavItem(Screen.Home,    "Home",    Icons.Filled.Home),
    NavItem(Screen.Trends,  "Trends",  Icons.Filled.ShowChart),
    NavItem(Screen.Log,     "Log",     Icons.Filled.AddCircle),
    NavItem(Screen.Profile, "Profile", Icons.Filled.Person),
    NavItem(Screen.Settings,"Settings",Icons.Filled.Settings),
)

@Composable
fun ShellScreen() {
    val navController = rememberNavController()
    val backstackEntry by navController.currentBackStackEntryAsState()
    val currentDest = backstackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = androidx.compose.ui.unit.Dp.Unspecified,
            ) {
                navItems.forEach { item ->
                    val selected = currentDest?.hierarchy
                        ?.any { it.route == item.screen.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavGraph(navController = navController)
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(name, style = MaterialTheme.typography.headlineLarge)
    }
}
```

- [ ] **Step 4: Update `MainActivity.kt`**

```kotlin
// app/src/main/java/com/weightflow/MainActivity.kt
package com.weightflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.weightflow.data.prefs.AccentColor
import com.weightflow.ui.shell.ShellScreen
import com.weightflow.ui.theme.WeightFlowTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as WeightFlowApp

        setContent {
            val accent by app.userPrefs.accentColor.collectAsState(initial = AccentColor.LIME)
            val darkMode by app.userPrefs.isDarkMode.collectAsState(initial = true)

            WeightFlowTheme(accent = accent, darkMode = darkMode) {
                ShellScreen()
            }
        }
    }
}
```

- [ ] **Step 5: Run on emulator**

  Run → Run 'app' (or Shift+F10). Choose an API 34 emulator.
  Expected:
  - App launches to a dark screen with "Home" text in the center
  - Bottom navigation shows 5 tabs: Home, Trends, Log, Profile, Settings
  - Tapping each tab shows the correct placeholder label
  - Active tab icon turns lime green

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/
git commit -m "feat: navigation shell with 5-tab bottom nav and placeholder screens"
```

---

## Foundation Complete ✅

At this point you have:
- Android project with all dependencies configured
- Room database with `WeightEntry` and `UserProfile` tables
- Repository layer with unit tests passing
- DataStore preferences with tests passing
- Theme system with 8 accent colors + Bebas Neue/Outfit fonts
- Navigable app shell with 5 working tabs

**Verify before moving to Plan 2:**
- `./gradlew test` passes (WeightRepositoryTest + UserPrefsDataStoreTest)
- `./gradlew connectedAndroidTest` passes (WeightEntryDaoTest)
- App runs on emulator — 5 tabs navigate, correct placeholder text, lime accent active

**Next:** `2026-04-11-weightflow-screens.md`
