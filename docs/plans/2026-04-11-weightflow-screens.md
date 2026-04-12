# WeightFlow — Plan 2: All Screens

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

> **CLI Priority:** Use `./gradlew` for builds. Use `gh` CLI for PRs/CI. Use `codex` CLI to scaffold ViewModel shells and Vico chart composables in parallel. Reference `android-jetpack-compose`, `android-clean-architecture`, and `mobile-android-design` skills throughout.

> **Codex Acceleration:** Run `codex "scaffold DashboardViewModel.kt with StateFlow<DashboardUiState> sealed class, package com.weightflow.ui.dashboard"` for each ViewModel. Run `codex "create Vico 1.13.1 CartesianChartHost composable for List<WeightEntry> line chart"` for chart components. Wire up and add business logic in Claude Code.

**Goal:** Implement all 6 screens (Dashboard, Trends, Log, History, Profile, Settings) with full ViewModels, wired to the Room repositories from Plan 1.

**Architecture:** Each screen owns one ViewModel. ViewModels take repositories from `WeightFlowApp` via a `ViewModelFactory`. UI state is a single sealed `UiState` per screen, exposed as `StateFlow`. Screens observe with `collectAsStateWithLifecycle()`.

**Tech Stack:** Jetpack Compose · ViewModel · StateFlow · Vico 1.13.1 charts · Room (from Plan 1)

**Prerequisite:** Plan 1 complete. App builds, tests pass, shell navigates.

---

## File Map

```
app/src/main/java/com/weightflow/
├── ui/
│   ├── components/
│   │   ├── WeightCard.kt          ← hero weight display card (reused on Dashboard + Log)
│   │   ├── ProgressSection.kt     ← goal progress bar component
│   │   ├── SparklineChart.kt      ← 7/30-day mini line chart (Vico)
│   │   └── DeltaPill.kt           ← green/red +/- badge
│   ├── dashboard/
│   │   ├── DashboardScreen.kt
│   │   └── DashboardViewModel.kt
│   ├── trends/
│   │   ├── TrendsScreen.kt
│   │   └── TrendsViewModel.kt
│   ├── log/
│   │   ├── LogScreen.kt
│   │   └── LogViewModel.kt
│   ├── history/
│   │   ├── HistoryScreen.kt
│   │   └── HistoryViewModel.kt
│   ├── profile/
│   │   ├── ProfileScreen.kt
│   │   └── ProfileViewModel.kt
│   └── settings/
│       ├── SettingsScreen.kt
│       └── SettingsViewModel.kt
└── ui/util/
    ├── ViewModelFactory.kt        ← generic factory for manual DI
    ├── DateFormatter.kt           ← shared date/time formatting helpers
    └── WeightConverter.kt         ← kg ↔ lbs ↔ stone conversion
app/src/test/java/com/weightflow/
├── ui/DashboardViewModelTest.kt
├── ui/LogViewModelTest.kt
├── ui/TrendsViewModelTest.kt
└── ui/SettingsViewModelTest.kt
```

---

## Task 1: Shared Utilities + ViewModelFactory

**Files:**
- Create: `app/src/main/java/com/weightflow/ui/util/ViewModelFactory.kt`
- Create: `app/src/main/java/com/weightflow/ui/util/DateFormatter.kt`
- Create: `app/src/main/java/com/weightflow/ui/util/WeightConverter.kt`
- Test: `app/src/test/java/com/weightflow/ui/util/WeightConverterTest.kt`

- [ ] **Step 1: Write failing test for WeightConverter**

```kotlin
// app/src/test/java/com/weightflow/ui/util/WeightConverterTest.kt
package com.weightflow.ui.util

import com.weightflow.data.prefs.WeightUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class WeightConverterTest {

    @Test
    fun kgToKg_returnsSame() {
        assertEquals(82.4, WeightConverter.display(82.4, WeightUnit.KG), 0.01)
    }

    @Test
    fun kgToLbs_convertsCorrectly() {
        assertEquals(181.66, WeightConverter.display(82.4, WeightUnit.LBS), 0.01)
    }

    @Test
    fun kgToStone_convertsCorrectly() {
        // 82.4 kg ÷ 6.35029 = 12.97 stone
        assertEquals(12.97, WeightConverter.display(82.4, WeightUnit.STONE), 0.01)
    }

    @Test
    fun unitLabel_returnsCorrectString() {
        assertEquals("kg",   WeightConverter.label(WeightUnit.KG))
        assertEquals("lbs",  WeightConverter.label(WeightUnit.LBS))
        assertEquals("st",   WeightConverter.label(WeightUnit.STONE))
    }

    @Test
    fun format_showsOneDecimal() {
        assertEquals("82.4", WeightConverter.format(82.4, WeightUnit.KG))
        assertEquals("181.7", WeightConverter.format(82.4, WeightUnit.LBS))
    }
}
```

- [ ] **Step 2: Run test — verify it fails**

  Expected: FAIL — `WeightConverter` doesn't exist.

- [ ] **Step 3: Create `WeightConverter`**

```kotlin
// app/src/main/java/com/weightflow/ui/util/WeightConverter.kt
package com.weightflow.ui.util

import com.weightflow.data.prefs.WeightUnit

object WeightConverter {

    fun display(kg: Double, unit: WeightUnit): Double = when (unit) {
        WeightUnit.KG    -> kg
        WeightUnit.LBS   -> kg * 2.20462
        WeightUnit.STONE -> kg / 6.35029
    }

    fun label(unit: WeightUnit): String = when (unit) {
        WeightUnit.KG    -> "kg"
        WeightUnit.LBS   -> "lbs"
        WeightUnit.STONE -> "st"
    }

    fun format(kg: Double, unit: WeightUnit): String =
        "%.1f".format(display(kg, unit))

    /** Convert user-entered value back to kg for storage. */
    fun toKg(value: Double, unit: WeightUnit): Double = when (unit) {
        WeightUnit.KG    -> value
        WeightUnit.LBS   -> value / 2.20462
        WeightUnit.STONE -> value * 6.35029
    }
}
```

- [ ] **Step 4: Create `DateFormatter`**

```kotlin
// app/src/main/java/com/weightflow/ui/util/DateFormatter.kt
package com.weightflow.ui.util

import java.text.SimpleDateFormat
import java.util.*

object DateFormatter {

    private val dayOfWeek  = SimpleDateFormat("EEE", Locale.getDefault())
    private val dayNum     = SimpleDateFormat("d",   Locale.getDefault())
    private val monthYear  = SimpleDateFormat("MMM d", Locale.getDefault())
    private val fullDate   = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    private val monthLabel = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    fun dayOfWeek(ms: Long): String  = dayOfWeek.format(Date(ms)).uppercase()
    fun dayNum(ms: Long): String     = dayNum.format(Date(ms))
    fun monthYear(ms: Long): String  = monthYear.format(Date(ms))
    fun fullDate(ms: Long): String   = fullDate.format(Date(ms))
    fun monthLabel(ms: Long): String = monthLabel.format(Date(ms))

    fun isToday(ms: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = ms }
        val cal2 = Calendar.getInstance()
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun startOfDayMs(offsetDays: Int = 0): Long {
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -offsetDays)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
```

- [ ] **Step 5: Create `ViewModelFactory`**

```kotlin
// app/src/main/java/com/weightflow/ui/util/ViewModelFactory.kt
package com.weightflow.ui.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory<T : ViewModel>(
    private val creator: () -> T
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = creator() as VM
}
```

- [ ] **Step 6: Run test — verify it passes**

  Expected: All 5 tests GREEN.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/util/ \
        app/src/test/java/com/weightflow/ui/util/
git commit -m "feat: WeightConverter, DateFormatter, ViewModelFactory utilities"
```

---

## Task 2: Shared UI Components

**Files:**
- Create: `app/src/main/java/com/weightflow/ui/components/DeltaPill.kt`
- Create: `app/src/main/java/com/weightflow/ui/components/ProgressSection.kt`
- Create: `app/src/main/java/com/weightflow/ui/components/WeightCard.kt`

No unit tests — these are pure composables verified visually.

- [ ] **Step 1: Create `DeltaPill`**

```kotlin
// app/src/main/java/com/weightflow/ui/components/DeltaPill.kt
package com.weightflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.weightflow.ui.theme.DangerRed
import com.weightflow.ui.theme.SuccessGreen

@Composable
fun DeltaPill(deltaKg: Double, modifier: Modifier = Modifier) {
    val isGain = deltaKg > 0
    val color  = if (isGain) DangerRed else SuccessGreen
    val bg     = color.copy(alpha = 0.12f)
    val arrow  = if (isGain) "▲" else "▼"
    val text   = "$arrow ${"%.1f".format(kotlin.math.abs(deltaKg))} kg"

    Row(
        modifier = modifier
            .background(bg, shape = RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = color,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    }
}
```

- [ ] **Step 2: Create `ProgressSection`**

```kotlin
// app/src/main/java/com/weightflow/ui/components/ProgressSection.kt
package com.weightflow.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * @param progressFraction 0.0 – 1.0
 * @param remainingKg formatted string like "4.4 kg"
 */
@Composable
fun ProgressSection(
    progressFraction: Float,
    goalLabel: String,
    remainingLabel: String,
    modifier: Modifier = Modifier
) {
    var triggered by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (triggered) progressFraction.coerceIn(0f, 1f) else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "progress"
    )
    LaunchedEffect(Unit) { triggered = true }

    Column(modifier) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$goalLabel · $remainingLabel remaining",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${(progressFraction * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
```

- [ ] **Step 3: Create `WeightCard`**

```kotlin
// app/src/main/java/com/weightflow/ui/components/WeightCard.kt
package com.weightflow.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.weightflow.ui.theme.TextSecondary

/**
 * The large weight hero card used on Dashboard.
 * Shows the weight in Bebas Neue, a delta pill, and the goal progress bar.
 */
@Composable
fun WeightCard(
    weightFormatted: String,    // e.g. "82.4"
    unitLabel: String,          // e.g. "kg"
    deltaKg: Double?,           // null if no previous entry
    goalLabel: String,          // e.g. "Goal: 78.0 kg"
    remainingLabel: String,     // e.g. "4.4 kg"
    progressFraction: Float,    // 0.0 – 1.0
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                "Today's Weight",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    weightFormatted,
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    unitLabel,
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            if (deltaKg != null) {
                DeltaPill(deltaKg = deltaKg, modifier = Modifier.padding(top = 4.dp))
            }
            Spacer(Modifier.height(18.dp))
            ProgressSection(
                progressFraction = progressFraction,
                goalLabel = goalLabel,
                remainingLabel = remainingLabel
            )
        }
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/components/
git commit -m "feat: shared UI components — DeltaPill, ProgressSection, WeightCard"
```

---

## Task 3: Dashboard Screen

**Files:**
- Create: `app/src/main/java/com/weightflow/ui/dashboard/DashboardViewModel.kt`
- Create: `app/src/main/java/com/weightflow/ui/dashboard/DashboardScreen.kt`
- Test: `app/src/test/java/com/weightflow/ui/DashboardViewModelTest.kt`

- [ ] **Step 1: Write failing ViewModel test**

```kotlin
// app/src/test/java/com/weightflow/ui/DashboardViewModelTest.kt
package com.weightflow.ui

import app.cash.turbine.test
import com.weightflow.data.db.UserProfileEntity
import com.weightflow.data.db.WeightEntryEntity
import com.weightflow.data.prefs.AccentColor
import com.weightflow.data.prefs.UserPrefsDataStore
import com.weightflow.data.prefs.WeightUnit
import com.weightflow.data.repository.UserProfileRepository
import com.weightflow.data.repository.WeightRepository
import com.weightflow.ui.dashboard.DashboardUiState
import com.weightflow.ui.dashboard.DashboardViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val weightRepo     = mockk<WeightRepository>()
    private val profileRepo    = mockk<UserProfileRepository>()
    private val prefs          = mockk<UserPrefsDataStore>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { prefs.weightUnit  } returns flowOf(WeightUnit.KG)
        every { prefs.accentColor } returns flowOf(AccentColor.LIME)
        every { prefs.isProMode   } returns flowOf(false)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun state_isLoadingInitially() = runTest {
        every { weightRepo.getAllEntries() } returns flowOf(emptyList())
        every { profileRepo.getProfile()  } returns flowOf(null)

        val vm = DashboardViewModel(weightRepo, profileRepo, prefs)
        val state = vm.uiState.value
        assertTrue(state is DashboardUiState.Loading)
    }

    @Test
    fun state_showsCurrentWeightFromLatestEntry() = runTest {
        val entries = listOf(
            WeightEntryEntity(id = 2, weightKg = 82.4, timestampMs = 2000L),
            WeightEntryEntity(id = 1, weightKg = 82.7, timestampMs = 1000L),
        )
        every { weightRepo.getAllEntries() } returns flowOf(entries)
        every { profileRepo.getProfile()  } returns flowOf(
            UserProfileEntity(goalWeightKg = 78.0, startWeightKg = 95.2)
        )

        val vm = DashboardViewModel(weightRepo, profileRepo, prefs)
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertTrue(state is DashboardUiState.Ready)
            val ready = state as DashboardUiState.Ready
            assertEquals(82.4, ready.latestWeightKg, 0.01)
            assertEquals(-0.3, ready.deltaKg!!, 0.01)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun progressFraction_calculatedCorrectly() = runTest {
        // Start 95.2, goal 78.0, current 82.4
        // lost = 95.2 - 82.4 = 12.8; total = 95.2 - 78.0 = 17.2; fraction = 12.8/17.2 = 0.744
        val entries = listOf(WeightEntryEntity(id = 1, weightKg = 82.4, timestampMs = 1000L))
        every { weightRepo.getAllEntries() } returns flowOf(entries)
        every { profileRepo.getProfile()  } returns flowOf(
            UserProfileEntity(startWeightKg = 95.2, goalWeightKg = 78.0)
        )

        val vm = DashboardViewModel(weightRepo, profileRepo, prefs)
        advanceUntilIdle()

        vm.uiState.test {
            val ready = awaitItem() as DashboardUiState.Ready
            assertEquals(0.744f, ready.progressFraction, 0.01f)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

- [ ] **Step 2: Run test — verify it fails**

  Expected: FAIL — `DashboardViewModel`, `DashboardUiState` don't exist.

- [ ] **Step 3: Create `DashboardViewModel`**

```kotlin
// app/src/main/java/com/weightflow/ui/dashboard/DashboardViewModel.kt
package com.weightflow.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weightflow.data.db.WeightEntryEntity
import com.weightflow.data.prefs.UserPrefsDataStore
import com.weightflow.data.prefs.WeightUnit
import com.weightflow.data.repository.UserProfileRepository
import com.weightflow.data.repository.WeightRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Ready(
        val latestWeightKg: Double,
        val deltaKg: Double?,             // null if first ever entry
        val progressFraction: Float,      // 0..1
        val goalWeightKg: Double,
        val startWeightKg: Double,
        val recentEntries: List<WeightEntryEntity>,
        val sparklineEntries: List<WeightEntryEntity>, // last 30 days
        val weightUnit: WeightUnit,
        val isProMode: Boolean
    ) : DashboardUiState()
}

class DashboardViewModel(
    private val weightRepo: WeightRepository,
    private val profileRepo: UserProfileRepository,
    private val prefs: UserPrefsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                weightRepo.getAllEntries(),
                profileRepo.getProfile(),
                prefs.weightUnit,
                prefs.isProMode
            ) { entries, profile, unit, proMode ->
                if (entries.isEmpty()) {
                    DashboardUiState.Loading
                } else {
                    val sorted   = entries.sortedByDescending { it.timestampMs }
                    val latest   = sorted.first()
                    val previous = sorted.getOrNull(1)
                    val delta    = previous?.let { latest.weightKg - it.weightKg }

                    val startKg = profile?.startWeightKg?.takeIf { it > 0 }
                        ?: sorted.last().weightKg
                    val goalKg  = profile?.goalWeightKg ?: latest.weightKg
                    val lost    = startKg - latest.weightKg
                    val total   = startKg - goalKg
                    val progress = if (total > 0) (lost / total).toFloat().coerceIn(0f, 1f) else 0f

                    val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
                    val sparkline = sorted.filter { it.timestampMs >= thirtyDaysAgo }
                        .sortedBy { it.timestampMs }

                    DashboardUiState.Ready(
                        latestWeightKg   = latest.weightKg,
                        deltaKg          = delta,
                        progressFraction = progress,
                        goalWeightKg     = goalKg,
                        startWeightKg    = startKg,
                        recentEntries    = sorted.take(5),
                        sparklineEntries = sparkline,
                        weightUnit       = unit,
                        isProMode        = proMode
                    )
                }
            }.collect { _uiState.value = it }
        }
    }
}
```

- [ ] **Step 4: Create `DashboardScreen`**

```kotlin
// app/src/main/java/com/weightflow/ui/dashboard/DashboardScreen.kt
package com.weightflow.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weightflow.data.db.WeightEntryEntity
import com.weightflow.ui.components.DeltaPill
import com.weightflow.ui.components.WeightCard
import com.weightflow.ui.theme.TextTertiary
import com.weightflow.ui.util.DateFormatter
import com.weightflow.ui.util.WeightConverter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onLogClick: () -> Unit,
    onViewAllTrends: () -> Unit,
    onViewAllHistory: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (val s = state) {
        is DashboardUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        is DashboardUiState.Ready -> ReadyContent(
            state = s,
            onLogClick = onLogClick,
            onViewAllTrends = onViewAllTrends,
            onViewAllHistory = onViewAllHistory
        )
    }
}

@Composable
private fun ReadyContent(
    state: DashboardUiState.Ready,
    onLogClick: () -> Unit,
    onViewAllTrends: () -> Unit,
    onViewAllHistory: () -> Unit,
) {
    val unit = state.weightUnit
    val goalLabel = "Goal: ${WeightConverter.format(state.goalWeightKg, unit)} ${WeightConverter.label(unit)}"
    val remaining = "${WeightConverter.format(
        maxOf(0.0, state.goalWeightKg - state.latestWeightKg).let {
            if (state.goalWeightKg < state.latestWeightKg) state.latestWeightKg - state.goalWeightKg else it
        }, unit)} ${WeightConverter.label(unit)}"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Greeting
        item {
            val dayFmt = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
            Column(Modifier.padding(start = 18.dp, end = 18.dp, top = 16.dp)) {
                Text(
                    dayFmt.format(Date()).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Good morning 👋",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Weight hero card
        item {
            WeightCard(
                weightFormatted  = WeightConverter.format(state.latestWeightKg, unit),
                unitLabel        = WeightConverter.label(unit),
                deltaKg          = state.deltaKg,
                goalLabel        = goalLabel,
                remainingLabel   = remaining,
                progressFraction = state.progressFraction,
                modifier         = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
            )
        }

        // Sparkline section header
        item {
            SectionHeader(
                title = "30-Day Trend",
                linkText = "View All",
                onLinkClick = onViewAllTrends,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
            )
        }

        // Sparkline card
        item {
            SparklineCard(
                entries = state.sparklineEntries,
                modifier = Modifier.padding(horizontal = 14.dp)
            )
        }

        // Quick stats
        item {
            SectionHeader(
                title = "Quick Stats",
                modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 8.dp)
            )
        }
        item {
            QuickStatsRow(
                bmi = state.latestWeightKg / ((1.75 * 1.75)),  // placeholder height
                startKg = state.startWeightKg,
                lostKg = state.startWeightKg - state.latestWeightKg,
                unit = unit,
                modifier = Modifier.padding(horizontal = 14.dp)
            )
        }

        // Recent entries header
        item {
            SectionHeader(
                title = "Recent",
                linkText = "History",
                onLinkClick = onViewAllHistory,
                modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 4.dp)
            )
        }

        // Recent rows
        items(state.recentEntries) { entry ->
            RecentEntryRow(entry = entry, unit = unit)
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    linkText: String? = null,
    onLinkClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (linkText != null && onLinkClick != null) {
            TextButton(onClick = onLinkClick, contentPadding = PaddingValues(0.dp)) {
                Text(linkText, style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun SparklineCard(
    entries: List<WeightEntryEntity>,
    modifier: Modifier = Modifier
) {
    // Placeholder until Vico integrated in Task 5
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
            Text(
                if (entries.isEmpty()) "No data yet" else "${entries.size} entries",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickStatsRow(
    bmi: Double,
    startKg: Double,
    lostKg: Double,
    unit: com.weightflow.data.prefs.WeightUnit,
    modifier: Modifier = Modifier
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatChip("%.1f".format(bmi), "BMI", Modifier.weight(1f))
        StatChip(WeightConverter.format(startKg, unit), "Start", Modifier.weight(1f))
        StatChip(
            "−" + WeightConverter.format(lostKg, unit),
            "Lost",
            Modifier.weight(1f),
            valueColor = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun StatChip(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.displaySmall, color = valueColor)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RecentEntryRow(entry: WeightEntryEntity, unit: com.weightflow.data.prefs.WeightUnit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.width(44.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(DateFormatter.dayNum(entry.timestampMs),
                style = MaterialTheme.typography.displaySmall)
            Text(DateFormatter.dayOfWeek(entry.timestampMs),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(12.dp))
        Text(
            "${WeightConverter.format(entry.weightKg, unit)} ${WeightConverter.label(unit)}",
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.weight(1f)
        )
    }
    HorizontalDivider(
        Modifier.padding(horizontal = 18.dp),
        color = MaterialTheme.colorScheme.outline
    )
}
```

- [ ] **Step 5: Wire Dashboard into `NavGraph.kt`**

  Open `app/src/main/java/com/weightflow/ui/navigation/NavGraph.kt` and replace the Home composable:

```kotlin
// Replace only the NavGraph.kt file content:
package com.weightflow.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.weightflow.WeightFlowApp
import com.weightflow.ui.dashboard.DashboardScreen
import com.weightflow.ui.dashboard.DashboardViewModel
import com.weightflow.ui.shell.PlaceholderScreen
import com.weightflow.ui.util.ViewModelFactory

@Composable
fun NavGraph(navController: NavHostController, app: WeightFlowApp) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            val vm: DashboardViewModel = viewModel(
                factory = ViewModelFactory {
                    DashboardViewModel(app.weightRepository, app.userProfileRepository, app.userPrefs)
                }
            )
            DashboardScreen(
                viewModel       = vm,
                onLogClick      = { navController.navigate(Screen.Log.route) },
                onViewAllTrends = { navController.navigate(Screen.Trends.route) },
                onViewAllHistory= { navController.navigate(Screen.History.route) }
            )
        }

        composable(Screen.Trends.route)   { PlaceholderScreen("Trends") }
        composable(Screen.Log.route)      { PlaceholderScreen("Log") }
        composable(Screen.History.route)  { PlaceholderScreen("History") }
        composable(Screen.Profile.route)  { PlaceholderScreen("Profile") }
        composable(Screen.Settings.route) { PlaceholderScreen("Settings") }
    }
}
```

- [ ] **Step 6: Update `ShellScreen.kt` to pass `app` to `NavGraph`**

  In `ShellScreen.kt`, update the NavGraph call:

```kotlin
// Inside ShellScreen() composable, replace the NavGraph call:
val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as com.weightflow.WeightFlowApp

// ... inside Scaffold content:
Box(modifier = Modifier.padding(innerPadding)) {
    NavGraph(navController = navController, app = app)
}
```

- [ ] **Step 7: Run tests then run on emulator**

```bash
./gradlew test
```
  Expected: All tests GREEN.

  Then run on emulator. Dashboard should show CircularProgressIndicator (no data yet). Tab to Trends/Log/etc — placeholder text. No crashes.

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/dashboard/ \
        app/src/main/java/com/weightflow/ui/navigation/ \
        app/src/main/java/com/weightflow/ui/shell/ \
        app/src/test/java/com/weightflow/ui/DashboardViewModelTest.kt
git commit -m "feat: Dashboard screen with WeightCard, sparkline placeholder, recent entries"
```

---

## Task 4: Log Weight Screen

**Files:**
- Create: `app/src/main/java/com/weightflow/ui/log/LogViewModel.kt`
- Create: `app/src/main/java/com/weightflow/ui/log/LogScreen.kt`
- Test: `app/src/test/java/com/weightflow/ui/LogViewModelTest.kt`

- [ ] **Step 1: Write failing ViewModel test**

```kotlin
// app/src/test/java/com/weightflow/ui/LogViewModelTest.kt
package com.weightflow.ui

import com.weightflow.data.prefs.UserPrefsDataStore
import com.weightflow.data.prefs.WeightUnit
import com.weightflow.data.repository.WeightRepository
import com.weightflow.ui.log.LogViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LogViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val weightRepo     = mockk<WeightRepository>()
    private val prefs          = mockk<UserPrefsDataStore>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { prefs.weightUnit } returns flowOf(WeightUnit.KG)
        every { prefs.isProMode  } returns flowOf(false)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun initialWeight_is80kg() {
        val vm = LogViewModel(weightRepo, prefs)
        assertEquals(80.0, vm.weightKg.value, 0.01)
    }

    @Test
    fun incrementWeight_addsTenth() {
        val vm = LogViewModel(weightRepo, prefs)
        vm.increment()
        assertEquals(80.1, vm.weightKg.value, 0.001)
    }

    @Test
    fun decrementWeight_subtractsTenth() {
        val vm = LogViewModel(weightRepo, prefs)
        vm.decrement()
        assertEquals(79.9, vm.weightKg.value, 0.001)
    }

    @Test
    fun saveEntry_callsRepository() = runTest {
        coEvery { weightRepo.insertEntry(any(), any(), any(), any(), any()) } returns Unit
        val vm = LogViewModel(weightRepo, prefs)
        vm.save()
        advanceUntilIdle()
        coVerify { weightRepo.insertEntry(weightKg = 80.0, any(), any(), any(), any()) }
    }

    @Test
    fun saveEntry_setsNavigateAwayFlag() = runTest {
        coEvery { weightRepo.insertEntry(any(), any(), any(), any(), any()) } returns Unit
        val vm = LogViewModel(weightRepo, prefs)
        vm.save()
        advanceUntilIdle()
        assertTrue(vm.navigateAway.value)
    }
}
```

- [ ] **Step 2: Run test — verify it fails**

  Expected: FAIL — `LogViewModel` not found.

- [ ] **Step 3: Create `LogViewModel`**

```kotlin
// app/src/main/java/com/weightflow/ui/log/LogViewModel.kt
package com.weightflow.ui.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weightflow.data.prefs.UserPrefsDataStore
import com.weightflow.data.prefs.WeightUnit
import com.weightflow.data.repository.WeightRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class LogViewModel(
    private val weightRepo: WeightRepository,
    private val prefs: UserPrefsDataStore
) : ViewModel() {

    private val _weightKg    = MutableStateFlow(80.0)
    private val _notes       = MutableStateFlow("")
    private val _bodyFat     = MutableStateFlow<Double?>(null)
    private val _muscleMass  = MutableStateFlow<Double?>(null)
    private val _navigateAway = MutableStateFlow(false)

    val weightKg: StateFlow<Double>    = _weightKg.asStateFlow()
    val notes: StateFlow<String>       = _notes.asStateFlow()
    val bodyFat: StateFlow<Double?>    = _bodyFat.asStateFlow()
    val muscleMass: StateFlow<Double?> = _muscleMass.asStateFlow()
    val navigateAway: StateFlow<Boolean> = _navigateAway.asStateFlow()
    val weightUnit: StateFlow<WeightUnit> = prefs.weightUnit
        .stateIn(viewModelScope, SharingStarted.Eagerly, WeightUnit.KG)
    val isProMode: StateFlow<Boolean> = prefs.isProMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /** Selected date — defaults to today midnight */
    private val _dateMs = MutableStateFlow(startOfToday())
    val dateMs: StateFlow<Long> = _dateMs.asStateFlow()

    fun increment() { _weightKg.value = ((_weightKg.value + 0.1) * 10).toLong() / 10.0 }
    fun decrement() { _weightKg.value = ((_weightKg.value - 0.1) * 10).toLong() / 10.0 }
    fun setWeight(kg: Double) { _weightKg.value = kg }
    fun setNotes(text: String) { _notes.value = text }
    fun setBodyFat(pct: Double?) { _bodyFat.value = pct }
    fun setMuscleMass(kg: Double?) { _muscleMass.value = kg }
    fun setDate(ms: Long) { _dateMs.value = ms }

    fun save() {
        viewModelScope.launch {
            weightRepo.insertEntry(
                weightKg      = _weightKg.value,
                bodyFatPercent = _bodyFat.value,
                muscleMassKg  = _muscleMass.value,
                notes         = _notes.value.takeIf { it.isNotBlank() },
                timestampMs   = _dateMs.value
            )
            _navigateAway.value = true
        }
    }

    private fun startOfToday(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 8)
        set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
```

- [ ] **Step 4: Create `LogScreen`**

```kotlin
// app/src/main/java/com/weightflow/ui/log/LogScreen.kt
package com.weightflow.ui.log

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weightflow.data.prefs.WeightUnit
import com.weightflow.ui.util.DateFormatter
import com.weightflow.ui.util.WeightConverter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(
    viewModel: LogViewModel,
    onBack: () -> Unit
) {
    val weightKg   by viewModel.weightKg.collectAsStateWithLifecycle()
    val unit       by viewModel.weightUnit.collectAsStateWithLifecycle()
    val isProMode  by viewModel.isProMode.collectAsStateWithLifecycle()
    val notes      by viewModel.notes.collectAsStateWithLifecycle()
    val dateMs     by viewModel.dateMs.collectAsStateWithLifecycle()
    val navigateAway by viewModel.navigateAway.collectAsStateWithLifecycle()
    val bodyFat    by viewModel.bodyFat.collectAsStateWithLifecycle()
    val muscleMass by viewModel.muscleMass.collectAsStateWithLifecycle()

    LaunchedEffect(navigateAway) { if (navigateAway) onBack() }

    val displayWeight = WeightConverter.display(weightKg, unit)
    val unitLabel = WeightConverter.label(unit)

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Back row
        Row(Modifier.padding(start = 12.dp, top = 12.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Log Weight", style = MaterialTheme.typography.titleLarge)
        }

        // Weight input card
        Card(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Enter Weight", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    FilledTonalButton(onClick = { viewModel.decrement() },
                        modifier = Modifier.size(44.dp),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(12.dp)) {
                        Text("−", style = MaterialTheme.typography.titleLarge)
                    }
                    Text(
                        "%.1f".format(displayWeight),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    FilledTonalButton(onClick = { viewModel.increment() },
                        modifier = Modifier.size(44.dp),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(12.dp)) {
                        Text("+", style = MaterialTheme.typography.titleLarge)
                    }
                }
                Spacer(Modifier.height(8.dp))
                // Unit toggle
                Row(Modifier.background(MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(8.dp))) {
                    listOf(WeightUnit.KG, WeightUnit.LBS, WeightUnit.STONE).forEach { u ->
                        val selected = u == unit
                        Box(
                            Modifier
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(WeightConverter.label(u),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Date row
        Card(Modifier.fillMaxWidth().padding(horizontal = 14.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Row(Modifier.padding(14.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Date", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(DateFormatter.fullDate(dateMs),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 2.dp))
                }
                Icon(Icons.Filled.DateRange, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(10.dp))

        // Notes
        OutlinedTextField(
            value = notes,
            onValueChange = viewModel::setNotes,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
            label = { Text("Notes (optional)") },
            placeholder = { Text("e.g. After morning workout...") },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        // Pro metrics (only shown in Pro mode)
        if (isProMode) {
            Spacer(Modifier.height(10.dp))
            Card(Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))) {
                Column(Modifier.padding(14.dp)) {
                    Row(Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Body Metrics", style = MaterialTheme.typography.titleMedium)
                            Text("Optional", style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("PRO", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = bodyFat?.let { "%.1f".format(it) } ?: "",
                            onValueChange = { viewModel.setBodyFat(it.toDoubleOrNull()) },
                            modifier = Modifier.weight(1f),
                            label = { Text("Body Fat %") },
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = muscleMass?.let { "%.1f".format(it) } ?: "",
                            onValueChange = { viewModel.setMuscleMass(it.toDoubleOrNull()) },
                            modifier = Modifier.weight(1f),
                            label = { Text("Muscle kg") },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Save button
        Button(
            onClick = { viewModel.save() },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp).height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Save Entry", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimary)
        }
        Spacer(Modifier.height(16.dp))
    }
}
```

- [ ] **Step 5: Wire Log into `NavGraph.kt`**

  Add to imports and replace the Log composable in NavGraph:

```kotlin
composable(Screen.Log.route) {
    val vm: LogViewModel = viewModel(
        factory = ViewModelFactory {
            LogViewModel(app.weightRepository, app.userPrefs)
        }
    )
    LogScreen(
        viewModel = vm,
        onBack    = { navController.popBackStack() }
    )
}
```

- [ ] **Step 6: Run tests and run on emulator**

```bash
./gradlew test
```
  Then run app. Tap Log tab → Log screen appears with +/- controls. Enter 82.4 → tap Save → navigates back to Home. Home now shows the entry (leaves Loading state).

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/log/ \
        app/src/main/java/com/weightflow/ui/navigation/ \
        app/src/test/java/com/weightflow/ui/LogViewModelTest.kt
git commit -m "feat: Log Weight screen with +/- controls, unit toggle, Pro metrics"
```

---

## Task 5: Trends Screen + Vico Chart

**Files:**
- Create: `app/src/main/java/com/weightflow/ui/trends/TrendsViewModel.kt`
- Create: `app/src/main/java/com/weightflow/ui/trends/TrendsScreen.kt`
- Test: `app/src/test/java/com/weightflow/ui/TrendsViewModelTest.kt`

- [ ] **Step 1: Write failing ViewModel test**

```kotlin
// app/src/test/java/com/weightflow/ui/TrendsViewModelTest.kt
package com.weightflow.ui

import app.cash.turbine.test
import com.weightflow.data.db.WeightEntryEntity
import com.weightflow.data.prefs.UserPrefsDataStore
import com.weightflow.data.prefs.WeightUnit
import com.weightflow.data.repository.WeightRepository
import com.weightflow.ui.trends.ChartType
import com.weightflow.ui.trends.TimeRange
import com.weightflow.ui.trends.TrendsUiState
import com.weightflow.ui.trends.TrendsViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TrendsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val weightRepo = mockk<WeightRepository>()
    private val prefs      = mockk<UserPrefsDataStore>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { prefs.weightUnit } returns flowOf(WeightUnit.KG)
        every { prefs.isProMode  } returns flowOf(false)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun defaultRange_is30Days() {
        val vm = TrendsViewModel(weightRepo, prefs)
        assertEquals(TimeRange.DAYS_30, vm.selectedRange.value)
    }

    @Test
    fun defaultChartType_isLine() {
        val vm = TrendsViewModel(weightRepo, prefs)
        assertEquals(ChartType.LINE, vm.selectedChart.value)
    }

    @Test
    fun filteredEntries_respectsRange() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            WeightEntryEntity(weightKg = 82.4, timestampMs = now),
            WeightEntryEntity(weightKg = 85.0, timestampMs = now - 40L * 86400 * 1000)  // 40 days ago
        )
        every { weightRepo.getAllEntries() } returns flowOf(entries)

        val vm = TrendsViewModel(weightRepo, prefs)
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem() as TrendsUiState.Ready
            // 30D filter should exclude the 40-day-old entry
            assertEquals(1, state.filteredEntries.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun stats_calculatedCorrectly() = runTest {
        val now = System.currentTimeMillis()
        val entries = listOf(
            WeightEntryEntity(weightKg = 82.4, timestampMs = now),
            WeightEntryEntity(weightKg = 83.0, timestampMs = now - 86400000L),
            WeightEntryEntity(weightKg = 85.2, timestampMs = now - 2 * 86400000L)
        )
        every { weightRepo.getAllEntries() } returns flowOf(entries)

        val vm = TrendsViewModel(weightRepo, prefs)
        advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem() as TrendsUiState.Ready
            assertEquals(85.2, state.maxKg, 0.01)
            assertEquals(82.4, state.minKg, 0.01)
            assertEquals(83.53, state.avgKg, 0.01)
            assertEquals(-2.8, state.changeKg, 0.01)  // 82.4 - 85.2
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

- [ ] **Step 2: Run test — verify it fails**

  Expected: FAIL — `TrendsViewModel`, `TimeRange`, `ChartType` not found.

- [ ] **Step 3: Create `TrendsViewModel`**

```kotlin
// app/src/main/java/com/weightflow/ui/trends/TrendsViewModel.kt
package com.weightflow.ui.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weightflow.data.db.WeightEntryEntity
import com.weightflow.data.prefs.UserPrefsDataStore
import com.weightflow.data.prefs.WeightUnit
import com.weightflow.data.repository.WeightRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class TimeRange(val label: String, val days: Int) {
    DAYS_7("7D", 7),
    DAYS_30("30D", 30),
    DAYS_90("3M", 90),
    DAYS_365("1Y", 365),
    ALL("All", Int.MAX_VALUE)
}

enum class ChartType(val label: String) {
    LINE("Line"), BAR("Bar"), AREA("Area"), CANDLE("Candle")
}

sealed class TrendsUiState {
    object Loading : TrendsUiState()
    object Empty   : TrendsUiState()
    data class Ready(
        val filteredEntries: List<WeightEntryEntity>,
        val allEntries: List<WeightEntryEntity>,
        val maxKg: Double,
        val minKg: Double,
        val avgKg: Double,
        val changeKg: Double,        // latest - oldest in range
        val weightUnit: WeightUnit,
        val isProMode: Boolean
    ) : TrendsUiState()
}

class TrendsViewModel(
    private val weightRepo: WeightRepository,
    private val prefs: UserPrefsDataStore
) : ViewModel() {

    private val _selectedRange = MutableStateFlow(TimeRange.DAYS_30)
    private val _selectedChart = MutableStateFlow(ChartType.LINE)

    val selectedRange: StateFlow<TimeRange>  = _selectedRange.asStateFlow()
    val selectedChart: StateFlow<ChartType>  = _selectedChart.asStateFlow()

    private val _uiState = MutableStateFlow<TrendsUiState>(TrendsUiState.Loading)
    val uiState: StateFlow<TrendsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                weightRepo.getAllEntries(),
                _selectedRange,
                prefs.weightUnit,
                prefs.isProMode
            ) { all, range, unit, pro ->
                if (all.isEmpty()) return@combine TrendsUiState.Empty
                val sorted = all.sortedBy { it.timestampMs }
                val cutoff = if (range == TimeRange.ALL) 0L
                    else System.currentTimeMillis() - range.days.toLong() * 86400_000L
                val filtered = sorted.filter { it.timestampMs >= cutoff }
                if (filtered.isEmpty()) return@combine TrendsUiState.Empty
                TrendsUiState.Ready(
                    filteredEntries = filtered,
                    allEntries      = sorted,
                    maxKg    = filtered.maxOf { it.weightKg },
                    minKg    = filtered.minOf { it.weightKg },
                    avgKg    = filtered.map { it.weightKg }.average(),
                    changeKg = filtered.last().weightKg - filtered.first().weightKg,
                    weightUnit = unit,
                    isProMode  = pro
                )
            }.collect { _uiState.value = it }
        }
    }

    fun selectRange(range: TimeRange) { _selectedRange.value = range }
    fun selectChart(chart: ChartType) { _selectedChart.value = chart }
}
```

- [ ] **Step 4: Create `TrendsScreen`**

```kotlin
// app/src/main/java/com/weightflow/ui/trends/TrendsScreen.kt
package com.weightflow.ui.trends

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patrykandpatrick.vico.compose.cartesian.*
import com.patrykandpatrick.vico.compose.cartesian.axis.*
import com.patrykandpatrick.vico.compose.cartesian.layer.*
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.weightflow.data.db.WeightEntryEntity
import com.weightflow.ui.util.WeightConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun TrendsScreen(viewModel: TrendsViewModel) {
    val state        by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedRange by viewModel.selectedRange.collectAsStateWithLifecycle()
    val selectedChart by viewModel.selectedChart.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Row(Modifier.padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()) {
            Text("Trends", style = MaterialTheme.typography.headlineLarge)
            if (state is TrendsUiState.Ready && (state as TrendsUiState.Ready).isProMode) {
                Text("PRO MODE", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
            }
        }

        // Time range chips
        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            TimeRange.values().forEach { range ->
                FilterChip(
                    selected = range == selectedRange,
                    onClick  = { viewModel.selectRange(range) },
                    label    = { Text(range.label, style = MaterialTheme.typography.labelLarge) },
                    shape    = RoundedCornerShape(20.dp)
                )
            }
        }

        // Chart type buttons
        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 14.dp).padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ChartType.values().forEach { type ->
                val selected = type == selectedChart
                Box(
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.surfaceVariant
                            else MaterialTheme.colorScheme.background
                        )
                        .clickable { viewModel.selectChart(type) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(type.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        when (val s = state) {
            is TrendsUiState.Loading, TrendsUiState.Empty -> {
                Box(Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) {
                    Text("No data yet. Log your weight to see trends.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            is TrendsUiState.Ready -> {
                ChartCard(entries = s.filteredEntries, chartType = selectedChart)
                StatsRow(state = s, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))

                if (s.isProMode) {
                    BodyCompSection(modifier = Modifier.padding(horizontal = 14.dp))
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ChartCard(entries: List<WeightEntryEntity>, chartType: ChartType) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(entries) {
        withContext(Dispatchers.Default) {
            modelProducer.runTransaction {
                lineSeries {
                    series(entries.map { it.weightKg.toFloat() })
                }
            }
        }
    }

    Card(
        Modifier.fillMaxWidth().padding(horizontal = 14.dp).height(240.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(Modifier.fillMaxSize().padding(12.dp)) {
            ProvideVicoTheme(rememberM3VicoTheme()) {
                CartesianChartHost(
                    chart = rememberCartesianChart(
                        rememberLineCartesianLayer(),
                        startAxis = rememberStartAxis(),
                        bottomAxis = rememberBottomAxis()
                    ),
                    modelProducer = modelProducer,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun StatsRow(state: TrendsUiState.Ready, modifier: Modifier = Modifier) {
    val unit = state.weightUnit
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatCell(WeightConverter.format(state.filteredEntries.last().weightKg, unit), "Now",     Modifier.weight(1f))
        StatCell(
            (if (state.changeKg < 0) "−" else "+") + WeightConverter.format(Math.abs(state.changeKg), unit),
            "Change",
            Modifier.weight(1f),
            if (state.changeKg < 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
        )
        StatCell(WeightConverter.format(state.avgKg, unit), "Avg",  Modifier.weight(1f))
        StatCell(WeightConverter.format(state.maxKg, unit), "Max",  Modifier.weight(1f))
    }
}

@Composable
private fun StatCell(
    value: String, label: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Card(modifier, shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.displaySmall, color = valueColor)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BodyCompSection(modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Body Composition", style = MaterialTheme.typography.titleMedium)
                Text("PRO", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.height(8.dp))
            Text("Log body fat % and muscle mass when logging weight to see trends here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
```

- [ ] **Step 5: Wire Trends into NavGraph**

```kotlin
composable(Screen.Trends.route) {
    val vm: TrendsViewModel = viewModel(
        factory = ViewModelFactory {
            TrendsViewModel(app.weightRepository, app.userPrefs)
        }
    )
    TrendsScreen(viewModel = vm)
}
```

- [ ] **Step 6: Run tests and verify on emulator**

```bash
./gradlew test
```
  Expected: all tests GREEN including TrendsViewModelTest.

  On emulator: after logging an entry, Trends tab shows the Vico line chart. Time range chips and chart type buttons are tappable.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/trends/ \
        app/src/main/java/com/weightflow/ui/navigation/ \
        app/src/test/java/com/weightflow/ui/TrendsViewModelTest.kt
git commit -m "feat: Trends screen with Vico line chart, time range + chart type filters"
```

---

## Task 6: History Screen

**Files:**
- Create: `app/src/main/java/com/weightflow/ui/history/HistoryViewModel.kt`
- Create: `app/src/main/java/com/weightflow/ui/history/HistoryScreen.kt`

- [ ] **Step 1: Create `HistoryViewModel`**

```kotlin
// app/src/main/java/com/weightflow/ui/history/HistoryViewModel.kt
package com.weightflow.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weightflow.data.db.WeightEntryEntity
import com.weightflow.data.prefs.UserPrefsDataStore
import com.weightflow.data.prefs.WeightUnit
import com.weightflow.data.repository.WeightRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HistoryUiState(
    val groupedEntries: Map<String, List<WeightEntryEntity>> = emptyMap(),
    val weightUnit: WeightUnit = WeightUnit.KG
)

class HistoryViewModel(
    private val weightRepo: WeightRepository,
    private val prefs: UserPrefsDataStore
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = combine(
        weightRepo.getAllEntries(),
        prefs.weightUnit
    ) { entries, unit ->
        val sorted = entries.sortedByDescending { it.timestampMs }
        // Group by "MMMM yyyy" label
        val grouped = sorted.groupBy { entry ->
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = entry.timestampMs }
            "${monthName(cal.get(java.util.Calendar.MONTH))} ${cal.get(java.util.Calendar.YEAR)}"
        }
        HistoryUiState(groupedEntries = grouped, weightUnit = unit)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState())

    fun deleteEntry(entry: WeightEntryEntity) {
        viewModelScope.launch { weightRepo.deleteEntry(entry) }
    }

    private fun monthName(month: Int): String = arrayOf(
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    )[month]
}
```

- [ ] **Step 2: Create `HistoryScreen`**

```kotlin
// app/src/main/java/com/weightflow/ui/history/HistoryScreen.kt
package com.weightflow.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weightflow.data.db.WeightEntryEntity
import com.weightflow.ui.components.DeltaPill
import com.weightflow.ui.util.DateFormatter
import com.weightflow.ui.util.WeightConverter

@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Text("History",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(start = 18.dp, top = 16.dp, bottom = 8.dp))

        if (state.groupedEntries.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No entries yet. Start logging!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn {
                state.groupedEntries.forEach { (month, entries) ->
                    item {
                        Text(month.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 18.dp, top = 16.dp, bottom = 6.dp))
                    }
                    items(entries, key = { it.id }) { entry ->
                        val prevEntry = entries.getOrNull(entries.indexOf(entry) + 1)
                        val delta = prevEntry?.let { entry.weightKg - it.weightKg }
                        HistoryRow(
                            entry = entry,
                            delta = delta,
                            unit  = state.weightUnit,
                            onDelete = { viewModel.deleteEntry(entry) }
                        )
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun HistoryRow(
    entry: WeightEntryEntity,
    delta: Double?,
    unit: com.weightflow.data.prefs.WeightUnit,
    onDelete: () -> Unit
) {
    var showDelete by remember { mutableStateOf(false) }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Day number + name
        Column(Modifier.width(42.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(DateFormatter.dayNum(entry.timestampMs),
                style = MaterialTheme.typography.displaySmall)
            Text(DateFormatter.dayOfWeek(entry.timestampMs),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(14.dp))

        // Weight
        Column(Modifier.weight(1f)) {
            Text(
                "${WeightConverter.format(entry.weightKg, unit)} ${WeightConverter.label(unit)}",
                style = MaterialTheme.typography.displaySmall
            )
            if (!entry.notes.isNullOrBlank()) {
                Text(entry.notes, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1)
            }
        }

        // Delta pill
        if (delta != null) {
            DeltaPill(deltaKg = delta)
        }

        // Delete icon
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp))
        }
    }
    HorizontalDivider(Modifier.padding(horizontal = 18.dp),
        color = MaterialTheme.colorScheme.outline)
}
```

- [ ] **Step 3: Wire History into NavGraph**

```kotlin
composable(Screen.History.route) {
    val vm: HistoryViewModel = viewModel(
        factory = ViewModelFactory {
            HistoryViewModel(app.weightRepository, app.userPrefs)
        }
    )
    HistoryScreen(viewModel = vm)
}
```

- [ ] **Step 4: Wire History tab into bottom nav**

  In `ShellScreen.kt`, add History to `navItems`:

```kotlin
val navItems = listOf(
    NavItem(Screen.Home,    "Home",    Icons.Filled.Home),
    NavItem(Screen.Trends,  "Trends",  Icons.Filled.ShowChart),
    NavItem(Screen.Log,     "Log",     Icons.Filled.AddCircle),
    NavItem(Screen.History, "History", Icons.Filled.List),
    NavItem(Screen.Profile, "Profile", Icons.Filled.Person),
    NavItem(Screen.Settings,"Settings",Icons.Filled.Settings),
)
```

  Note: This gives 6 tabs. Navigation bar handles up to 5 well. Remove the Settings tab from the bottom nav — Settings will be reachable from Profile. Update `navItems` back to 5:

```kotlin
val navItems = listOf(
    NavItem(Screen.Home,    "Home",    Icons.Filled.Home),
    NavItem(Screen.Trends,  "Trends",  Icons.Filled.ShowChart),
    NavItem(Screen.Log,     "Log",     Icons.Filled.AddCircle),
    NavItem(Screen.History, "History", Icons.Filled.List),
    NavItem(Screen.Profile, "Profile", Icons.Filled.Person),
)
```

- [ ] **Step 5: Run on emulator and verify**

  Log several entries → History tab shows them grouped by month with delta pills and delete buttons.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/history/ \
        app/src/main/java/com/weightflow/ui/navigation/ \
        app/src/main/java/com/weightflow/ui/shell/
git commit -m "feat: History screen with month grouping, delta pills, delete"
```

---

## Task 7: Profile Screen

**Files:**
- Create: `app/src/main/java/com/weightflow/ui/profile/ProfileViewModel.kt`
- Create: `app/src/main/java/com/weightflow/ui/profile/ProfileScreen.kt`

- [ ] **Step 1: Create `ProfileViewModel`**

```kotlin
// app/src/main/java/com/weightflow/ui/profile/ProfileViewModel.kt
package com.weightflow.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weightflow.data.db.UserProfileEntity
import com.weightflow.data.db.WeightEntryEntity
import com.weightflow.data.prefs.UserPrefsDataStore
import com.weightflow.data.prefs.WeightUnit
import com.weightflow.data.repository.UserProfileRepository
import com.weightflow.data.repository.WeightRepository
import kotlinx.coroutines.flow.*

data class ProfileUiState(
    val profile: UserProfileEntity = UserProfileEntity(),
    val latestWeightKg: Double = 0.0,
    val startWeightKg: Double = 0.0,
    val totalLostKg: Double = 0.0,
    val progressFraction: Float = 0f,
    val streakDays: Int = 0,
    val totalDaysLogged: Int = 0,
    val weightUnit: WeightUnit = WeightUnit.KG
)

class ProfileViewModel(
    private val weightRepo: WeightRepository,
    private val profileRepo: UserProfileRepository,
    private val prefs: UserPrefsDataStore
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = combine(
        weightRepo.getAllEntries(),
        profileRepo.getProfile(),
        prefs.weightUnit
    ) { entries, profile, unit ->
        val sorted  = entries.sortedByDescending { it.timestampMs }
        val latestKg = sorted.firstOrNull()?.weightKg ?: 0.0
        val startKg  = profile?.startWeightKg?.takeIf { it > 0 } ?: sorted.lastOrNull()?.weightKg ?: 0.0
        val goalKg   = profile?.goalWeightKg ?: latestKg
        val lost     = startKg - latestKg
        val total    = startKg - goalKg
        val progress = if (total > 0) (lost / total).toFloat().coerceIn(0f, 1f) else 0f
        val streak   = calculateStreak(sorted)

        ProfileUiState(
            profile          = profile ?: UserProfileEntity(),
            latestWeightKg   = latestKg,
            startWeightKg    = startKg,
            totalLostKg      = lost,
            progressFraction = progress,
            streakDays       = streak,
            totalDaysLogged  = sorted.size,
            weightUnit       = unit
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())

    private fun calculateStreak(sorted: List<WeightEntryEntity>): Int {
        if (sorted.isEmpty()) return 0
        var streak = 1
        val msPerDay = 86400_000L
        for (i in 0 until sorted.size - 1) {
            val diff = sorted[i].timestampMs - sorted[i + 1].timestampMs
            if (diff in (msPerDay - 3_600_000)..(msPerDay + 3_600_000)) streak++
            else break
        }
        return streak
    }
}
```

- [ ] **Step 2: Create `ProfileScreen`**

```kotlin
// app/src/main/java/com/weightflow/ui/profile/ProfileScreen.kt
package com.weightflow.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weightflow.ui.components.ProgressSection
import com.weightflow.ui.util.WeightConverter

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onSettingsClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val unit = state.weightUnit

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(Modifier.fillMaxWidth().padding(start = 18.dp, end = 12.dp, top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("Profile", style = MaterialTheme.typography.headlineLarge)
            TextButton(onClick = onSettingsClick) {
                Text("Settings", color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }

        // Avatar + name
        Row(Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    state.profile.name.take(2).uppercase(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(state.profile.name, style = MaterialTheme.typography.titleLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔥", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.width(4.dp))
                    Text("${state.streakDays}", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(4.dp))
                    Text("day streak", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Goal card
        Card(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(
                1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(Modifier.padding(18.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("CURRENT GOAL", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary)
                    Text(state.profile.goalType.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp))
                }
                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth()) {
                    GoalNode("Start", WeightConverter.format(state.startWeightKg, unit), WeightConverter.label(unit), Modifier.weight(1f))
                    GoalNode("Current", WeightConverter.format(state.latestWeightKg, unit), WeightConverter.label(unit), Modifier.weight(1f), highlight = true)
                    GoalNode("Goal", WeightConverter.format(state.profile.goalWeightKg, unit), WeightConverter.label(unit), Modifier.weight(1f))
                }
                Spacer(Modifier.height(14.dp))
                ProgressSection(
                    progressFraction = state.progressFraction,
                    goalLabel = "Goal: ${WeightConverter.format(state.profile.goalWeightKg, unit)} ${WeightConverter.label(unit)}",
                    remainingLabel = "${WeightConverter.format(maxOf(0.0, state.latestWeightKg - state.profile.goalWeightKg), unit)} ${WeightConverter.label(unit)}"
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Stats grid
        Row(Modifier.padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatBlock("Height", "${state.profile.heightCm.toInt()} cm", Modifier.weight(1f))
            StatBlock("Days Logged", "${state.totalDaysLogged}", Modifier.weight(1f))
            StatBlock("Lost", "−${WeightConverter.format(state.totalLostKg, unit)} ${WeightConverter.label(unit)}", Modifier.weight(1f),
                valueColor = MaterialTheme.colorScheme.secondary)
        }

        // Achievements
        Spacer(Modifier.height(16.dp))
        Text("ACHIEVEMENTS",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 18.dp))
        Spacer(Modifier.height(8.dp))
        AchievementsRow(
            totalLost = state.totalLostKg,
            streakDays = state.streakDays,
            totalDays = state.totalDaysLogged
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun GoalNode(label: String, value: String, unit: String, modifier: Modifier,
                     highlight: Boolean = false) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.displaySmall,
            color = if (highlight) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface)
        Text(unit, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StatBlock(label: String, value: String, modifier: Modifier,
                      valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface) {
    Card(modifier, shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.displaySmall, color = valueColor)
        }
    }
}

data class Achievement(val emoji: String, val name: String, val earned: Boolean)

@Composable
private fun AchievementsRow(totalLost: Double, streakDays: Int, totalDays: Int) {
    val achievements = listOf(
        Achievement("🏆", "First 5kg",  totalLost >= 5.0),
        Achievement("🔥", "30-Day",     streakDays >= 30),
        Achievement("⚡", "Fast Drop",  totalLost >= 3.0),
        Achievement("💪", "10kg Lost",  totalLost >= 10.0),
        Achievement("📅", "6 Months",   totalDays >= 180),
        Achievement("🎯", "On Track",   totalDays >= 7),
        Achievement("🌟", "100 Days",   totalDays >= 100),
        Achievement("🏅", "Goal Hit",   false)
    )
    Row(
        Modifier.horizontalScroll(androidx.compose.foundation.rememberScrollState())
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        achievements.forEach { a ->
            Column(
                Modifier.width(58.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .size(46.dp)
                        .background(
                            if (a.earned) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(15.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(a.emoji, style = MaterialTheme.typography.titleLarge,
                        color = if (a.earned) LocalContentColor.current
                        else LocalContentColor.current.copy(alpha = 0.3f))
                }
                Spacer(Modifier.height(4.dp))
                Text(a.name, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1)
            }
        }
    }
}
```

- [ ] **Step 3: Wire Profile into NavGraph**

```kotlin
composable(Screen.Profile.route) {
    val vm: ProfileViewModel = viewModel(
        factory = ViewModelFactory {
            ProfileViewModel(app.weightRepository, app.userProfileRepository, app.userPrefs)
        }
    )
    ProfileScreen(
        viewModel = vm,
        onSettingsClick = { navController.navigate(Screen.Settings.route) }
    )
}
```

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/profile/ \
        app/src/main/java/com/weightflow/ui/navigation/
git commit -m "feat: Profile screen with avatar, goal showcase, stats, achievements"
```

---

## Task 8: Settings Screen

**Files:**
- Create: `app/src/main/java/com/weightflow/ui/settings/SettingsViewModel.kt`
- Create: `app/src/main/java/com/weightflow/ui/settings/SettingsScreen.kt`
- Test: `app/src/test/java/com/weightflow/ui/SettingsViewModelTest.kt`

- [ ] **Step 1: Write failing ViewModel test**

```kotlin
// app/src/test/java/com/weightflow/ui/SettingsViewModelTest.kt
package com.weightflow.ui

import com.weightflow.data.prefs.*
import com.weightflow.ui.settings.SettingsViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val prefs = mockk<UserPrefsDataStore>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { prefs.accentColor } returns flowOf(AccentColor.LIME)
        every { prefs.isDarkMode   } returns flowOf(true)
        every { prefs.isProMode    } returns flowOf(false)
        every { prefs.weightUnit   } returns flowOf(WeightUnit.KG)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun setAccentColor_callsPrefs() = runTest {
        coEvery { prefs.setAccentColor(any()) } returns Unit
        val vm = SettingsViewModel(prefs)
        vm.setAccentColor(AccentColor.CORAL)
        advanceUntilIdle()
        coVerify { prefs.setAccentColor(AccentColor.CORAL) }
    }

    @Test
    fun setProMode_callsPrefs() = runTest {
        coEvery { prefs.setProMode(any()) } returns Unit
        val vm = SettingsViewModel(prefs)
        vm.setProMode(true)
        advanceUntilIdle()
        coVerify { prefs.setProMode(true) }
    }
}
```

- [ ] **Step 2: Run test — verify it fails**

  Expected: FAIL — `SettingsViewModel` not found.

- [ ] **Step 3: Create `SettingsViewModel`**

```kotlin
// app/src/main/java/com/weightflow/ui/settings/SettingsViewModel.kt
package com.weightflow.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weightflow.data.prefs.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(private val prefs: UserPrefsDataStore) : ViewModel() {

    val accentColor: StateFlow<AccentColor> = prefs.accentColor
        .stateIn(viewModelScope, SharingStarted.Eagerly, AccentColor.LIME)
    val isDarkMode: StateFlow<Boolean> = prefs.isDarkMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val isProMode: StateFlow<Boolean> = prefs.isProMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val weightUnit: StateFlow<WeightUnit> = prefs.weightUnit
        .stateIn(viewModelScope, SharingStarted.Eagerly, WeightUnit.KG)

    fun setAccentColor(c: AccentColor) = viewModelScope.launch { prefs.setAccentColor(c) }
    fun setDarkMode(v: Boolean)        = viewModelScope.launch { prefs.setDarkMode(v) }
    fun setProMode(v: Boolean)         = viewModelScope.launch { prefs.setProMode(v) }
    fun setWeightUnit(u: WeightUnit)   = viewModelScope.launch { prefs.setWeightUnit(u) }
}
```

- [ ] **Step 4: Create `SettingsScreen`**

```kotlin
// app/src/main/java/com/weightflow/ui/settings/SettingsScreen.kt
package com.weightflow.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weightflow.data.prefs.AccentColor
import com.weightflow.data.prefs.WeightUnit
import com.weightflow.ui.theme.toColor

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val accent   by viewModel.accentColor.collectAsStateWithLifecycle()
    val darkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val proMode  by viewModel.isProMode.collectAsStateWithLifecycle()
    val unit     by viewModel.weightUnit.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Row(Modifier.padding(start = 8.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Settings", style = MaterialTheme.typography.headlineLarge)
        }

        // Accent color picker
        SettingsSectionLabel("Accent Color")
        Card(Modifier.fillMaxWidth().padding(horizontal = 14.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Theme Color", style = MaterialTheme.typography.titleMedium)
                    Box(Modifier.size(24.dp).background(accent.toColor(), RoundedCornerShape(8.dp)))
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    AccentColor.values().forEach { c ->
                        Box(
                            Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(c.toColor())
                                .clickable { viewModel.setAccentColor(c) }
                        ) {
                            if (c == accent) {
                                Box(Modifier.fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(10.dp)))
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Display
        SettingsSectionLabel("Display")
        SettingsGroup {
            ToggleRow("Dark Mode", darkMode) { viewModel.setDarkMode(it) }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            ToggleRow("Pro Mode", proMode,
                subtitle = "Body composition, advanced stats") { viewModel.setProMode(it) }
        }

        Spacer(Modifier.height(14.dp))

        // Unit preference
        SettingsSectionLabel("Preferences")
        SettingsGroup {
            Column(Modifier.padding(14.dp)) {
                Text("Weight Unit", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WeightUnit.values().forEach { u ->
                        val selected = u == unit
                        OutlinedButton(
                            onClick = { viewModel.setWeightUnit(u) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.background
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Text(
                                when (u) { WeightUnit.KG -> "kg"; WeightUnit.LBS -> "lbs"; WeightUnit.STONE -> "st" },
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsSectionLabel(title: String) {
    Text(title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 18.dp, bottom = 6.dp))
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 14.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        content = { Column { content() } }
    )
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, subtitle: String? = null,
                      onToggle: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = checked, onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            ))
    }
}
```

- [ ] **Step 5: Wire Settings into NavGraph**

```kotlin
composable(Screen.Settings.route) {
    val vm: SettingsViewModel = viewModel(
        factory = ViewModelFactory { SettingsViewModel(app.userPrefs) }
    )
    SettingsScreen(
        viewModel = vm,
        onBack    = { navController.popBackStack() }
    )
}
```

- [ ] **Step 6: Run all tests**

```bash
./gradlew test
```
  Expected: All tests in `DashboardViewModelTest`, `LogViewModelTest`, `TrendsViewModelTest`, `SettingsViewModelTest`, `WeightRepositoryTest`, `UserPrefsDataStoreTest`, `WeightConverterTest` GREEN.

- [ ] **Step 7: Run on emulator — full flow walkthrough**

  1. Launch app → Dashboard shows loading spinner
  2. Tap Log → enter 82.4 kg → Save → back to Dashboard → weight shows 82.4
  3. Log a second entry (82.7 kg, yesterday's date) → Dashboard shows delta pill
  4. Trends tab → chart renders, time range chips work
  5. History tab → entry listed, delete works
  6. Profile tab → avatar, stats, achievements
  7. Profile → tap Settings → color picker changes accent live → Pro mode toggle adds body metric fields to Log screen
  8. Back from Settings returns to Profile

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/settings/ \
        app/src/main/java/com/weightflow/ui/navigation/ \
        app/src/test/java/com/weightflow/ui/SettingsViewModelTest.kt
git commit -m "feat: Settings screen — accent color picker, dark/pro mode toggles, unit selector"
```

---

## Task 9: Wire Sparkline in Dashboard (Vico)

Now that Vico is confirmed working from the Trends screen, replace the placeholder sparkline in `DashboardScreen`.

**Files:**
- Modify: `app/src/main/java/com/weightflow/ui/dashboard/DashboardScreen.kt`
- Create: `app/src/main/java/com/weightflow/ui/components/SparklineChart.kt`

- [ ] **Step 1: Create `SparklineChart` composable**

```kotlin
// app/src/main/java/com/weightflow/ui/components/SparklineChart.kt
package com.weightflow.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.weightflow.data.db.WeightEntryEntity
import com.weightflow.ui.util.DateFormatter
import com.weightflow.ui.util.WeightConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SparklineChart(
    entries: List<WeightEntryEntity>,
    modifier: Modifier = Modifier
) {
    if (entries.size < 2) {
        Card(modifier.fillMaxWidth().height(88.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Box(contentAlignment = androidx.compose.ui.Alignment.Center,
                modifier = Modifier.fillMaxSize()) {
                Text("Log more entries to see your trend",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(entries) {
        withContext(Dispatchers.Default) {
            modelProducer.runTransaction {
                lineSeries { series(entries.map { it.weightKg.toFloat() }) }
            }
        }
    }

    Card(modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(12.dp)) {
            ProvideVicoTheme(rememberM3VicoTheme()) {
                CartesianChartHost(
                    chart = rememberCartesianChart(rememberLineCartesianLayer()),
                    modelProducer = modelProducer,
                    modifier = Modifier.fillMaxWidth().height(72.dp)
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(DateFormatter.monthYear(entries.first().timestampMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                val change = entries.last().weightKg - entries.first().weightKg
                val sign = if (change < 0) "−" else "+"
                Text("$sign${"%.1f".format(Math.abs(change))} kg this period",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (change < 0) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.error,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text(DateFormatter.monthYear(entries.last().timestampMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
```

- [ ] **Step 2: Replace `SparklineCard` call in `DashboardScreen.kt`**

  Find the `SparklineCard` composable inside `DashboardScreen.kt` and replace it with `SparklineChart`:

```kotlin
// Replace the entire SparklineCard composable definition with this import + call:
// (In the LazyColumn items section, replace the sparkline item block)
item {
    SparklineChart(
        entries  = state.sparklineEntries,
        modifier = Modifier.padding(horizontal = 14.dp)
    )
}
```

  And delete the private `SparklineCard` composable from `DashboardScreen.kt`.

- [ ] **Step 3: Build and run on emulator**

  After logging 3+ entries, Dashboard sparkline shows a real Vico line chart.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/components/SparklineChart.kt \
        app/src/main/java/com/weightflow/ui/dashboard/DashboardScreen.kt
git commit -m "feat: replace Dashboard sparkline placeholder with live Vico chart"
```

---

## All Screens Complete ✅

**Full regression check before shipping:**

```bash
./gradlew test connectedAndroidTest
```

Expected:
- `WeightConverterTest` — 5 tests PASS
- `WeightRepositoryTest` — 4 tests PASS
- `UserPrefsDataStoreTest` — 4 tests PASS
- `DashboardViewModelTest` — 3 tests PASS
- `LogViewModelTest` — 5 tests PASS
- `TrendsViewModelTest` — 4 tests PASS
- `SettingsViewModelTest` — 2 tests PASS
- `WeightEntryDaoTest` — 4 tests PASS (instrumented)

**Manual smoke test checklist:**
- [ ] Log 3 entries on different days
- [ ] Dashboard: weight card shows correctly, sparkline renders, delta pill correct
- [ ] Trends: chart shows all entries, 7D filter reduces count, chart type switcher works
- [ ] History: entries grouped by month, delete removes from list
- [ ] Profile: streak increments with consecutive days, achievements unlock correctly
- [ ] Settings: change accent color → whole app theme updates live; switch to lbs → all weight displays convert; enable Pro mode → body metrics section appears in Log

**Next:** `2026-04-11-weightflow-firebase-admob.md` (Phase 3 — when ready to monetize)
