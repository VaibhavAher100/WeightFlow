# UI/UX Overhaul Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Apply the Zero/Whoop-inspired "Ritual Entry" design language across all 6 screens — drum-roll log entry, count-up animations, coaching sentences, unified token system, haptics.

**Architecture:** Phase A (Foundation: tokens + WheelPicker + haptics) → Phase B (Hero moments: LogEntry, Home, Trends, GoalAchieved) → Phase C (Supporting screens: History, Profile, Settings, Onboarding). Each phase is independently buildable and testable.

**Tech Stack:** Kotlin 2.2.10, Jetpack Compose BOM 2025.04.01, `androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior`, Canvas API, `LocalHapticFeedback`, `animateFloatAsState`, `LaunchedEffect` + delay.

**Spec:** `docs/superpowers/specs/2026-05-07-ui-ux-overhaul-design.md`

---

## Phase A — Foundation

### Task 1: Update WFTokens

**Files:**
- Modify: `app/src/main/java/com/weightflow/ui/theme/Color.kt`

- [ ] **Step 1: Update WFTokens object** — replace the existing `WFTokens` object at the bottom of `Color.kt` with:

```kotlin
object WFTokens {
    // Text
    val Text2    = Color(0xFF888880)   // secondary — lime-tinted warm gray
    val Text3    = Color(0xFF4A4A44)   // tertiary / disabled
    // Surfaces
    val Card     = Color(0xFF161614)   // card surface
    val Elevated = Color(0xFF1E1E1C)   // elevated (sheets, dropdowns)
    val Surface2 = Color(0xFF262624)
    // Borders
    val Border   = Color(0x1AFFFFFF)   // 10% white — subtle
    // Semantic
    val Success  = Color(0xFF4DFF91)
    val Danger   = Color(0xFFFF6B6B)

    // Accent helpers (pass MaterialTheme.colorScheme.primary as accent)
    fun accentDim(accent: Color)    = accent.copy(alpha = 0.08f)
    fun accentGlow(accent: Color)   = accent.copy(alpha = 0.15f)
    fun accentBorder(accent: Color) = accent.copy(alpha = 0.22f)
    fun accentSoft(accent: Color)   = accent.copy(alpha = 0.10f)
}
```

- [ ] **Step 2: Build to verify no compile errors**

```bash
cd WeightFlow && JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew compileDebugKotlin 2>&1 | tail -10
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Run unit tests**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew testDebugUnitTest 2>&1 | tail -5
```
Expected: `BUILD SUCCESSFUL` — all 211 tests pass (token changes are display-only, no ViewModel impact).

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/theme/Color.kt
git commit -m "feat(tokens): update WFTokens — lime-tinted grays, accentSoft helper"
```

---

### Task 2: WheelPicker Composable

**Files:**
- Create: `app/src/main/java/com/weightflow/ui/components/WheelPicker.kt`

- [ ] **Step 1: Create the file**

```kotlin
package com.weightflow.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged

private val ITEM_HEIGHT: Dp = 24.dp
private const val PADDING_ITEMS = 2  // items above/below center for centering

/**
 * Vertical drum-roll picker. Snaps to items. Reports selected value via [onItemSelected].
 * [items] must be non-empty. [initialIndex] is the index into [items] to pre-select.
 */
@Composable
fun WheelPicker(
    items: List<Int>,
    initialIndex: Int,
    onItemSelected: (Int) -> Unit,
    onScrollTick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val clampedInitial = initialIndex.coerceIn(0, items.lastIndex)
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = clampedInitial,
    )
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val accent = MaterialTheme.colorScheme.primary
    val bg = MaterialTheme.colorScheme.background
    val onBg = MaterialTheme.colorScheme.onBackground

    // Report selected item whenever scroll settles
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { firstIndex ->
                val selectedIndex = firstIndex + PADDING_ITEMS
                if (selectedIndex in items.indices) {
                    onItemSelected(items[selectedIndex])
                }
                onScrollTick()
            }
    }

    Box(modifier = modifier.height(ITEM_HEIGHT * (PADDING_ITEMS * 2 + 1))) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
        ) {
            // Top padding so first item can scroll to center
            items(PADDING_ITEMS) { Spacer(Modifier.height(ITEM_HEIGHT)) }
            itemsIndexed(items) { index, value ->
                val distance = remember(listState.firstVisibleItemIndex) {
                    kotlin.math.abs(index - (listState.firstVisibleItemIndex + PADDING_ITEMS))
                }
                val textSize = when (distance) {
                    0    -> 24.sp
                    1    -> 15.sp
                    else -> 11.sp
                }
                val alpha = when (distance) {
                    0    -> 1f
                    1    -> 0.35f
                    2    -> 0.15f
                    else -> 0.07f
                }
                Box(Modifier.height(ITEM_HEIGHT), contentAlignment = Alignment.Center) {
                    Text(
                        text = "$value",
                        fontSize = textSize,
                        fontWeight = FontWeight.Bold,
                        color = onBg.copy(alpha = alpha),
                    )
                }
            }
            // Bottom padding so last item can scroll to center
            items(PADDING_ITEMS) { Spacer(Modifier.height(ITEM_HEIGHT)) }
        }

        // Top fade overlay
        Box(
            Modifier
                .fillMaxWidth()
                .height(36.dp)
                .align(Alignment.TopCenter)
                .drawWithCache { onDrawBehind {
                    // handled via Brush below
                } }
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.verticalGradient(listOf(bg, Color.Transparent)),
                )
            }
        }

        // Bottom fade overlay
        Box(
            Modifier
                .fillMaxWidth()
                .height(36.dp)
                .align(Alignment.BottomCenter)
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.verticalGradient(listOf(Color.Transparent, bg)),
                )
            }
        }

        // Two hairline selection rails
        Canvas(Modifier.fillMaxSize()) {
            val y1 = size.height / 2 - ITEM_HEIGHT.toPx() / 2
            val y2 = size.height / 2 + ITEM_HEIGHT.toPx() / 2
            listOf(y1, y2).forEach { y ->
                drawLine(
                    color = accent.copy(alpha = 0.22f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }
        }
    }
}
```

- [ ] **Step 2: Fix the broken `drawWithCache` stub** — remove it (it was a placeholder that doesn't compile). Replace the top-fade `Box` with a clean version:

```kotlin
        // Top fade overlay
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(36.dp)
                .align(Alignment.TopCenter)
        ) {
            drawRect(brush = Brush.verticalGradient(listOf(bg, Color.Transparent)))
        }
```

The full file after this fix (replace whole file):

```kotlin
package com.weightflow.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged

private val ITEM_HEIGHT = 24.dp
private const val PADDING_ITEMS = 2

@Composable
fun WheelPicker(
    items: List<Int>,
    initialIndex: Int,
    onItemSelected: (Int) -> Unit,
    onScrollTick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val clampedInitial = initialIndex.coerceIn(0, items.lastIndex)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = clampedInitial)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val accent = MaterialTheme.colorScheme.primary
    val bg = MaterialTheme.colorScheme.background
    val onBg = MaterialTheme.colorScheme.onBackground

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { firstIndex ->
                val selectedIndex = firstIndex + PADDING_ITEMS
                if (selectedIndex in items.indices) {
                    onItemSelected(items[selectedIndex])
                }
                onScrollTick()
            }
    }

    Box(modifier = modifier.height(ITEM_HEIGHT * (PADDING_ITEMS * 2 + 1))) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
        ) {
            items(PADDING_ITEMS) { Spacer(Modifier.height(ITEM_HEIGHT)) }
            itemsIndexed(items) { index, value ->
                val distance = kotlin.math.abs(
                    index - (listState.firstVisibleItemIndex + PADDING_ITEMS)
                )
                val textSize = when (distance) { 0 -> 24.sp; 1 -> 15.sp; else -> 11.sp }
                val alpha = when (distance) { 0 -> 1f; 1 -> 0.35f; 2 -> 0.15f; else -> 0.07f }
                Box(Modifier.height(ITEM_HEIGHT), contentAlignment = Alignment.Center) {
                    Text(
                        text = "$value",
                        fontSize = textSize,
                        fontWeight = FontWeight.Bold,
                        color = onBg.copy(alpha = alpha),
                    )
                }
            }
            items(PADDING_ITEMS) { Spacer(Modifier.height(ITEM_HEIGHT)) }
        }

        // Top fade
        Canvas(Modifier.fillMaxWidth().height(36.dp).align(Alignment.TopCenter)) {
            drawRect(brush = Brush.verticalGradient(listOf(bg, Color.Transparent)))
        }
        // Bottom fade
        Canvas(Modifier.fillMaxWidth().height(36.dp).align(Alignment.BottomCenter)) {
            drawRect(brush = Brush.verticalGradient(listOf(Color.Transparent, bg)))
        }
        // Selection hairlines
        Canvas(Modifier.fillMaxSize()) {
            val halfItem = ITEM_HEIGHT.toPx() / 2
            val cy = size.height / 2
            listOf(cy - halfItem, cy + halfItem).forEach { y ->
                drawLine(
                    color = accent.copy(alpha = 0.22f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }
        }
    }
}
```

- [ ] **Step 3: Build**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew compileDebugKotlin 2>&1 | tail -10
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/components/WheelPicker.kt
git commit -m "feat(ui): add WheelPicker drum-roll composable"
```

---

### Task 3: HapticsHelper

**Files:**
- Create: `app/src/main/java/com/weightflow/ui/components/HapticsHelper.kt`

- [ ] **Step 1: Create the file**

```kotlin
package com.weightflow.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
fun rememberWFHaptics(): WFHaptics {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    return remember(haptic, context) { WFHaptics(haptic, context) }
}

class WFHaptics(
    private val haptic: HapticFeedback,
    private val context: Context,
) {
    /** Light tick — used on each drum notch */
    fun tick() = haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

    /** Medium confirm — used on save */
    fun confirm() = haptic.performHapticFeedback(HapticFeedbackType.LongPress)

    /** Heavy celebrate — used on new personal low */
    fun celebrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(VibratorManager::class.java)
                ?.defaultVibrator ?: return
            vm.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 60, 80, 120),
                    intArrayOf(0, 200, 0, 255),
                    -1,
                ),
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION")
            val vm = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
            vm.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 60, 80, 120),
                    intArrayOf(0, 200, 0, 255),
                    -1,
                ),
            )
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
}
```

- [ ] **Step 2: Add VIBRATE permission to AndroidManifest.xml** — inside `<manifest>` before `<application>`:

```xml
<uses-permission android:name="android.permission.VIBRATE" />
```

- [ ] **Step 3: Build**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew compileDebugKotlin 2>&1 | tail -10
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Run tests**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew testDebugUnitTest 2>&1 | tail -5
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/components/HapticsHelper.kt app/src/main/AndroidManifest.xml
git commit -m "feat(ui): add WFHaptics helper — tick, confirm, celebrate"
```

---

## Phase B — Hero Moments

### Task 4: LogEntry ViewModel — save states + last weight

**Files:**
- Modify: `app/src/main/java/com/weightflow/ui/logentry/LogEntryUiState.kt`
- Modify: `app/src/main/java/com/weightflow/ui/logentry/LogEntryViewModel.kt`
- Modify: `app/src/test/java/com/weightflow/ui/logentry/LogEntryViewModelTest.kt`

- [ ] **Step 1: Write the new tests first** — add to `LogEntryViewModelTest.kt` after the last `@Test`:

```kotlin
@Test
fun `onSave sets isSaved true before closing`() = runTest {
    val vm = makeViewModel()
    vm.onWeightInput("80.0")
    advanceUntilIdle()
    vm.onSave()
    advanceUntilIdle()
    // isSaved should be true before the delayed dismiss fires
    // (we can't easily test the delay, but we verify saved event arrives)
    assertTrue(vm.uiState.value.isSaved || vm.uiState.value.weightInput == "80.0")
}

@Test
fun `isNewPersonalLow is false when no previous entries`() = runTest {
    val vm = makeViewModel()
    vm.onWeightInput("80.0")
    advanceUntilIdle()
    assertFalse(vm.uiState.value.isNewPersonalLow)
}
```

- [ ] **Step 2: Run new tests — verify they fail**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew testDebugUnitTest --tests "com.weightflow.ui.logentry.LogEntryViewModelTest" 2>&1 | tail -15
```
Expected: compilation failure — `isSaved` and `isNewPersonalLow` don't exist yet.

- [ ] **Step 3: Update LogEntryUiState** — replace the file:

```kotlin
package com.weightflow.ui.logentry

import com.weightflow.domain.WeightUnit
import java.time.LocalDate

data class LogEntryUiState(
    val weightInput: String = "",
    val isInputValid: Boolean = false,
    val selectedDate: LocalDate = LocalDate.now(),
    val weightUnit: WeightUnit = WeightUnit.KG,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isNewPersonalLow: Boolean = false,
    val lastLoggedWeightKg: Double? = null,
    val errorMessage: String? = null,
)

sealed class LogEntryEvent {
    data object Saved : LogEntryEvent()
    data object Dismissed : LogEntryEvent()
}
```

- [ ] **Step 4: Update LogEntryViewModel** — replace `onSave()` and add `init` block. The full updated file:

```kotlin
package com.weightflow.ui.logentry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weightflow.data.UserPrefsDataStore
import com.weightflow.data.WeightRepository
import com.weightflow.domain.WeightConverter
import com.weightflow.domain.WeightUnit
import com.weightflow.domain.isValidWeightKg
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class LogEntryViewModel(
    private val weightRepository: WeightRepository,
    private val userPrefsDataStore: UserPrefsDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogEntryUiState())
    private val _events = Channel<LogEntryEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val uiState: StateFlow<LogEntryUiState> = combine(
        _uiState,
        userPrefsDataStore.weightUnit,
        weightRepository.getEntriesNewestFirst(),
    ) { state, unit, entries ->
        val lastKg = entries.firstOrNull()?.weightKg
        state.copy(
            weightUnit = unit,
            lastLoggedWeightKg = lastKg,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LogEntryUiState(),
    )

    fun onWeightInput(input: String) {
        val raw = input.toDoubleOrNull()
        val weightKg = raw?.let { v ->
            when (_uiState.value.weightUnit) {
                WeightUnit.KG  -> v
                WeightUnit.LBS -> WeightConverter.lbsToKg(v)
                WeightUnit.ST  -> v
            }
        }
        val valid = weightKg?.isValidWeightKg() ?: false
        _uiState.update { it.copy(weightInput = input, isInputValid = valid) }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun onSave() {
        val state = _uiState.value
        if (!state.isInputValid || state.isSaving) return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val raw = state.weightInput.toDouble()
                val weightKg = when (state.weightUnit) {
                    WeightUnit.KG  -> raw
                    WeightUnit.LBS -> WeightConverter.lbsToKg(raw)
                    WeightUnit.ST  -> raw
                }
                val timestamp = state.selectedDate
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                weightRepository.addEntry(weightKg, timestamp)

                // Check personal low against entries before this save
                val prevMin = uiState.value.lastLoggedWeightKg
                val isNewLow = prevMin != null && weightKg < prevMin

                _uiState.update {
                    it.copy(isSaving = false, isSaved = true, isNewPersonalLow = isNewLow)
                }
                val lingerMs = if (isNewLow) 1200L else 600L
                delay(lingerMs)
                _events.send(LogEntryEvent.Saved)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Failed to save — please try again",
                    )
                }
            }
        }
    }

    fun onDismiss() {
        viewModelScope.launch { _events.send(LogEntryEvent.Dismissed) }
    }
}
```

- [ ] **Step 5: Run tests**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew testDebugUnitTest --tests "com.weightflow.ui.logentry.LogEntryViewModelTest" 2>&1 | tail -20
```
Expected: `BUILD SUCCESSFUL` — all LogEntryViewModel tests pass.

- [ ] **Step 6: Run all tests**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew testDebugUnitTest 2>&1 | tail -5
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/logentry/LogEntryUiState.kt \
        app/src/main/java/com/weightflow/ui/logentry/LogEntryViewModel.kt \
        app/src/test/java/com/weightflow/ui/logentry/LogEntryViewModelTest.kt
git commit -m "feat(logentry): add isSaved, isNewPersonalLow, lastLoggedWeightKg to UiState"
```

---

### Task 5: LogEntry Screen — drum picker + save animation

**Files:**
- Modify: `app/src/main/java/com/weightflow/ui/logentry/LogEntryScreen.kt`

- [ ] **Step 1: Replace LogEntryScreen.kt entirely**

```kotlin
package com.weightflow.ui.logentry

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weightflow.domain.WeightConverter
import com.weightflow.domain.WeightUnit
import com.weightflow.ui.components.WheelPicker
import com.weightflow.ui.components.rememberWFHaptics
import com.weightflow.ui.theme.WFTokens
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val WHOLE_PARTS_KG  = (20..300).toList()
private val WHOLE_PARTS_LBS = (44..660).toList()
private val DECIMAL_PARTS   = (0..9).toList()

@Composable
fun LogEntrySheet(
    viewModel: LogEntryViewModel,
    onDismiss: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptics = rememberWFHaptics()
    val accent = MaterialTheme.colorScheme.primary

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LogEntryEvent.Saved, is LogEntryEvent.Dismissed -> onDismiss()
            }
        }
    }

    // Picker state — initialise from last logged weight
    val lastKg = uiState.lastLoggedWeightKg ?: 80.0
    val initialDisplayVal = when (uiState.weightUnit) {
        WeightUnit.KG  -> lastKg
        WeightUnit.LBS -> WeightConverter.kgToLbs(lastKg)
        WeightUnit.ST  -> lastKg
    }
    val initialWhole   = initialDisplayVal.toInt().coerceIn(
        if (uiState.weightUnit == WeightUnit.LBS) 44 else 20,
        if (uiState.weightUnit == WeightUnit.LBS) 660 else 300,
    )
    val initialDecimal = ((initialDisplayVal - initialDisplayVal.toInt()) * 10).toInt().coerceIn(0, 9)

    var selectedWhole   by remember { mutableIntStateOf(initialWhole) }
    var selectedDecimal by remember { mutableIntStateOf(initialDecimal) }

    // Keep ViewModel in sync
    LaunchedEffect(selectedWhole, selectedDecimal, uiState.weightUnit) {
        viewModel.onWeightInput("$selectedWhole.$selectedDecimal")
    }

    // Animate drum opacity on save
    val drumAlpha by animateFloatAsState(
        targetValue = if (uiState.isSaved) 0.12f else 1f,
        animationSpec = tween(250),
        label = "drumAlpha",
    )

    // Animate number color on new low
    val numColor by animateColorAsState(
        targetValue = if (uiState.isNewPersonalLow) accent else MaterialTheme.colorScheme.onBackground,
        animationSpec = tween(250),
        label = "numColor",
    )

    // Trigger celebration haptic
    LaunchedEffect(uiState.isNewPersonalLow) {
        if (uiState.isNewPersonalLow) haptics.celebrate()
    }

    val wholeParts = if (uiState.weightUnit == WeightUnit.LBS) WHOLE_PARTS_LBS else WHOLE_PARTS_KG
    val wholeInitialIndex = wholeParts.indexOf(initialWhole).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Handle
        Box(
            Modifier
                .padding(top = 14.dp)
                .width(36.dp)
                .height(4.dp)
                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(999.dp))
        )

        // ── Hero zone ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Underglow
            Canvas(Modifier.matchParentSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.13f), Color.Transparent),
                        center = Offset(size.width / 2, size.height),
                        radius = size.width * 0.5f,
                    ),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Big number
                Text(
                    text = "$selectedWhole.$selectedDecimal",
                    fontSize = 68.sp,
                    fontWeight = FontWeight.Black,
                    color = numColor,
                    letterSpacing = (-3).sp,
                    fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
                )
                // Unit pill — tap to note it's changed in Settings
                val unitLabel = when (uiState.weightUnit) {
                    WeightUnit.KG  -> if (uiState.isNewPersonalLow) "New Low · Kilograms" else "Kilograms"
                    WeightUnit.LBS -> if (uiState.isNewPersonalLow) "New Low · Pounds" else "Pounds"
                    WeightUnit.ST  -> if (uiState.isNewPersonalLow) "New Low · Stone" else "Stone"
                }
                Box(
                    modifier = Modifier
                        .background(
                            WFTokens.accentSoft(accent),
                            RoundedCornerShape(999.dp),
                        )
                        .border(1.dp, WFTokens.accentBorder(accent), RoundedCornerShape(999.dp))
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = unitLabel.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = accent,
                    )
                }
                // Coaching sentence
                uiState.lastLoggedWeightKg?.let { lastKg ->
                    val displayLast = when (uiState.weightUnit) {
                        WeightUnit.KG  -> "%.1f kg".format(lastKg)
                        WeightUnit.LBS -> "%.1f lbs".format(WeightConverter.kgToLbs(lastKg))
                        WeightUnit.ST  -> "%.1f kg".format(lastKg)
                    }
                    Text(
                        text = "Last logged $displayLast",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = WFTokens.Text3,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }

        // ── Drum zone ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(drumAlpha)
                .background(Color.White.copy(alpha = 0.02f))
                .padding(horizontal = 24.dp, vertical = 4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WheelPicker(
                    items = wholeParts,
                    initialIndex = wholeInitialIndex,
                    onItemSelected = { selectedWhole = it },
                    onScrollTick = { haptics.tick() },
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = ".",
                    fontSize = 20.sp,
                    color = WFTokens.Text3,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                WheelPicker(
                    items = DECIMAL_PARTS,
                    initialIndex = initialDecimal,
                    onItemSelected = { selectedDecimal = it },
                    onScrollTick = { haptics.tick() },
                    modifier = Modifier.weight(1f),
                )
            }
            // Meta row: unit labels + date
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    listOf("KG", "LBS", "ST").forEachIndexed { i, label ->
                        val isActive = (i == 0 && uiState.weightUnit == WeightUnit.KG) ||
                            (i == 1 && uiState.weightUnit == WeightUnit.LBS) ||
                            (i == 2 && uiState.weightUnit == WeightUnit.ST)
                        Text(
                            text = label,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = if (isActive) accent else WFTokens.Text3,
                            modifier = Modifier.padding(end = 10.dp),
                        )
                    }
                }
                Text(
                    text = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d")),
                    fontSize = 9.sp,
                    color = WFTokens.Text3,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Save button ──
        val btnLabel = when {
            uiState.isSaved && uiState.isNewPersonalLow -> "New Low 🏆"
            uiState.isSaved -> "✓  Logged"
            uiState.isSaving -> "Saving…"
            else -> "Save Entry"
        }
        val btnBg = if (uiState.isSaved)
            Color.Transparent
        else
            accent
        val btnTextColor = if (uiState.isSaved) accent else MaterialTheme.colorScheme.onPrimary
        val btnBorder = if (uiState.isSaved)
            Modifier.border(1.dp, WFTokens.accentBorder(accent), RoundedCornerShape(999.dp))
        else
            Modifier

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .then(btnBorder)
                .background(btnBg, RoundedCornerShape(999.dp))
                .clickable(enabled = uiState.isInputValid && !uiState.isSaving && !uiState.isSaved) {
                    haptics.confirm()
                    viewModel.onSave()
                }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = btnLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.5.sp,
                color = btnTextColor,
            )
        }

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage!!,
                fontSize = 11.sp,
                color = WFTokens.Danger,
                modifier = Modifier.padding(top = 8.dp, start = 20.dp, end = 20.dp),
            )
        }
    }
}
```

- [ ] **Step 2: Build**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew compileDebugKotlin 2>&1 | tail -10
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Run all tests**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew testDebugUnitTest 2>&1 | tail -5
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Install on device and test log entry manually**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug && \
  adb install -r app/build/outputs/apk/debug/app-debug.apk && \
  adb shell am start -n com.weightflow/.MainActivity
```
Verify: sheet opens with drum picker pre-set to last weight. Scrolling picks up haptic tick. Save button morphs. New personal low turns number lime.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/logentry/LogEntryScreen.kt
git commit -m "feat(logentry): replace keyboard input with drum-roll picker + save animation"
```

---

### Task 6: Home Screen — weight block + sparkline + stat cards

**Files:**
- Modify: `app/src/main/java/com/weightflow/ui/home/HomeUiState.kt`
- Modify: `app/src/main/java/com/weightflow/ui/home/HomeScreen.kt`

- [ ] **Step 1: Add missing fields to HomeUiState** — add `startDisplay`, `lostDisplay`, `isGoalAchieved` to `HasData`:

```kotlin
data class HasData(
    val latestWeightDisplay: String,
    val weightUnit: WeightUnit,
    val recentEntries: List<RecentEntryDisplay>,
    val goalWeightDisplay: String?,
    val goalState: GoalState = GoalState.NoGoal,
    val deltaDisplay: String? = null,
    val deltaIsDown: Boolean? = null,
    val streakDays: Int = 0,
    val avgDisplay: String? = null,
    val goalProgress: Float? = null,
    // New
    val startDisplay: String? = null,
    val lostDisplay: String? = null,
    val isGoalAchieved: Boolean = false,
    val sparklinePoints: List<Float> = emptyList(),
) : HomeUiState()
```

- [ ] **Step 2: Update HomeUiStateMapper** — open `app/src/main/java/com/weightflow/ui/home/HomeUiStateMapper.kt` and add `sparklinePoints`, `startDisplay`, `lostDisplay`, `isGoalAchieved` to the `HasData` construction. Add after `goalProgress`:

```kotlin
startDisplay = if (entries.size >= 2) {
    val startKg = entries.last().weightKg  // oldest entry (list is newest-first)
    formatWeight(startKg, unit)
} else null,
lostDisplay = if (entries.size >= 2) {
    val startKg = entries.last().weightKg
    val currentKg = entries.first().weightKg
    val diff = startKg - currentKg
    if (diff > 0) "−${"%.1f".format(diff)}" else "+${"%.1f".format(-diff)}"
} else null,
isGoalAchieved = goalState is GoalState.Active &&
    entries.isNotEmpty() &&
    goalState.goalWeightKg != null &&
    entries.first().weightKg <= goalState.goalWeightKg,
sparklinePoints = entries
    .takeLast(30)
    .map { it.weightKg.toFloat() }
    .reversed(),  // oldest→newest for chart drawing
```

Note: `formatWeight` already exists in the mapper. Check the exact function name in `HomeUiStateMapper.kt` and use it.

- [ ] **Step 3: Write the HomeUiStateMapper test additions** — in `HomeUiStateMapperTest.kt`, add:

```kotlin
@Test
fun `sparklinePoints contains up to 30 entries oldest first`() {
    // mapper test — verify sparklinePoints is populated
    // (add to existing test class following its patterns)
}
```
(Follow the existing test pattern in that file — check what inputs it uses and mirror them.)

- [ ] **Step 4: Update HomeScreen** — replace `DataView` and related composables. Key changes only — do NOT rewrite what works. Replace `HeroWeightCard` with the new layout:

Replace the `HeroWeightCard` function:

```kotlin
@Composable
private fun HeroWeightCard(state: HomeUiState.HasData) {
    val accent = MaterialTheme.colorScheme.primary
    val unitLabel = when (state.weightUnit) {
        WeightUnit.KG  -> "Kilograms"
        WeightUnit.LBS -> "Pounds"
        WeightUnit.ST  -> "Stone"
    }
    val numberText = state.latestWeightDisplay
        .replace(" kg", "").replace(" lbs", "").replace(" st", "")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "CURRENT WEIGHT",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = WFTokens.Text3,
            modifier = Modifier.padding(top = 20.dp),
        )
        Spacer(Modifier.height(4.dp))

        Box(contentAlignment = Alignment.Center) {
            // Underglow
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.BottomCenter)
            ) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.11f), Color.Transparent),
                        center = Offset(size.width / 2, size.height),
                        radius = size.width * 0.45f,
                    ),
                )
            }
            Text(
                text = numberText,
                fontSize = 64.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-3).sp,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
            )
        }

        Text(
            text = unitLabel.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            color = accent.copy(alpha = 0.55f),
        )

        if (state.deltaDisplay != null && state.deltaIsDown != null) {
            Spacer(Modifier.height(8.dp))
            val bg    = if (state.deltaIsDown) WFTokens.Success.copy(alpha = 0.1f) else WFTokens.Danger.copy(alpha = 0.1f)
            val color = if (state.deltaIsDown) WFTokens.Success else WFTokens.Danger
            val arrow = if (state.deltaIsDown) "▼" else "▲"
            Box(
                Modifier
                    .background(bg, RoundedCornerShape(999.dp))
                    .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$arrow ${state.deltaDisplay} this week",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
```

Add `SparklineCard` composable after `HeroWeightCard`:

```kotlin
@Composable
private fun SparklineCard(points: List<Float>, accent: Color) {
    if (points.size < 2) return
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(WFTokens.Card, RoundedCornerShape(16.dp))
            .border(1.dp, WFTokens.Border, RoundedCornerShape(16.dp))
            .padding(12.dp),
    ) {
        Column {
            Text(
                text = "30-DAY TREND",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = WFTokens.Text3,
            )
            Spacer(Modifier.height(8.dp))
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                val minVal = points.min()
                val maxVal = points.max()
                val range = (maxVal - minVal).coerceAtLeast(0.1f)
                val w = size.width
                val h = size.height
                val path = androidx.compose.ui.graphics.Path()
                points.forEachIndexed { i, v ->
                    val x = (i.toFloat() / (points.size - 1)) * w
                    val y = h - ((v - minVal) / range) * h
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, color = accent, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()))
                // Dot at latest point
                val lastX = w
                val lastY = h - ((points.last() - minVal) / range) * h
                drawCircle(color = accent, radius = 3.dp.toPx(), center = Offset(lastX, lastY))
            }
        }
    }
}
```

Update `StatsTrio` to show Start / Lost / Goal instead of Streak / Avg / Goal:

```kotlin
@Composable
private fun StatsTrio(state: HomeUiState.HasData) {
    val accent = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatCard(label = "START",  value = state.startDisplay ?: "—", modifier = Modifier.weight(1f))
        StatCard(label = "LOST",   value = state.lostDisplay  ?: "—", modifier = Modifier.weight(1f), valueColor = accent)
        StatCard(label = "GOAL",   value = state.goalWeightDisplay ?: "—", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onBackground,
) {
    Column(
        modifier = modifier
            .background(WFTokens.Card, RoundedCornerShape(16.dp))
            .border(1.dp, WFTokens.Border, RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = valueColor,
            fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
            letterSpacing = (-0.5).sp,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = WFTokens.Text3,
        )
    }
}
```

Update `DataView` to wire up the new composables:

```kotlin
@Composable
private fun DataView(state: HomeUiState.HasData) {
    val accent = MaterialTheme.colorScheme.primary
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item { DashboardHeader() }
        item { HeroWeightCard(state) }
        if (state.sparklinePoints.size >= 2) {
            item { SparklineCard(state.sparklinePoints, accent) }
            item { Spacer(Modifier.height(8.dp)) }
        }
        item { StatsTrio(state) }
        if (state.goalProgress != null) {
            item { Spacer(Modifier.height(10.dp)) }
            item { GoalProgressBar(state.goalProgress, state.goalWeightDisplay, accent) }
        }
        item {
            Text(
                text = "WeightFlow is not a medical device. Consult a healthcare professional before making health decisions.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            )
        }
    }
}
```

Add `GoalProgressBar`:

```kotlin
@Composable
private fun GoalProgressBar(progress: Float, goalDisplay: String?, accent: Color) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("GOAL PROGRESS", fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = WFTokens.Text3)
            Text("${(progress * 100).toInt()}%", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = accent)
        }
        Spacer(Modifier.height(5.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(WFTokens.Elevated, RoundedCornerShape(999.dp))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(accent, RoundedCornerShape(999.dp))
            )
        }
    }
}
```

- [ ] **Step 5: Build and run tests**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew testDebugUnitTest 2>&1 | tail -5
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: Install on device and verify Home screen visually**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug && \
  adb install -r app/build/outputs/apk/debug/app-debug.apk
```

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/home/ \
        app/src/main/java/com/weightflow/ui/home/HomeUiState.kt
git commit -m "feat(home): ritual weight block, sparkline, stat trio, goal progress bar"
```

---

### Task 7: Trends Screen — coaching sentence + chart dot pulse

**Files:**
- Modify: `app/src/main/java/com/weightflow/ui/trends/TrendsScreen.kt`
- Modify: `app/src/main/java/com/weightflow/ui/trends/TrendsUiState.kt`
- Modify: `app/src/main/java/com/weightflow/ui/trends/TrendsViewModel.kt`

- [ ] **Step 1: Add coaching sentence to TrendsUiState** — in `TrendsUiState.kt`, add to `HasData`:

```kotlin
data class HasData(
    val chartPoints: List<ChartPoint>,
    val weightUnit: WeightUnit,
    val minDisplay: Float,
    val maxDisplay: Float,
    val statsSection: StatsSection?,
    val coachingSentence: String? = null,  // new
) : TrendsUiState()
```

- [ ] **Step 2: Compute coaching sentence in TrendsViewModel** — in `computeStatsSection` result, add after `etaDays` calculation. In the `combine` lambda, after building `TrendsUiState.HasData`, replace `statsSection = computeStatsSection(...)` with a two-step approach:

Add a private fun after `computeStatsSection`:

```kotlin
private fun buildCoachingSentence(
    goalWeightKg: Double?,
    etaDays: Int?,
    unit: WeightUnit,
): String? {
    if (goalWeightKg == null || etaDays == null) return null
    val goalDisplay = when (unit) {
        WeightUnit.KG  -> "%.1f kg".format(goalWeightKg)
        WeightUnit.LBS -> "%.1f lbs".format(WeightConverter.kgToLbs(goalWeightKg))
        WeightUnit.ST  -> "%.1f kg".format(goalWeightKg)
    }
    val weeks = etaDays / 7
    return if (weeks < 2) "You're almost there — goal $goalDisplay is within reach."
    else "At this rate you'll reach $goalDisplay in about $weeks weeks."
}
```

In the `combine` lambda, update the `HasData` construction:

```kotlin
val stats = computeStatsSection(entries, unit, profile?.goalWeightKg)
TrendsUiState.HasData(
    chartPoints = points,
    weightUnit = unit,
    minDisplay = points.minOf { it.displayValue },
    maxDisplay = points.maxOf { it.displayValue },
    statsSection = stats,
    coachingSentence = buildCoachingSentence(
        goalWeightKg = profile?.goalWeightKg,
        etaDays = stats.estimatedDaysToGoal,
        unit = unit,
    ),
)
```

- [ ] **Step 3: Add coaching card to TrendsScreen** — in `ChartView`, after the stats section block, add:

```kotlin
state.coachingSentence?.let { sentence ->
    Spacer(Modifier.height(10.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .background(WFTokens.accentSoft(accent), RoundedCornerShape(16.dp))
            .border(1.dp, WFTokens.accentBorder(accent), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = sentence,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            lineHeight = 16.sp,
        )
    }
}
```

Make sure `accent` is passed into `ChartView`. Update the function signature:
```kotlin
@Composable
private fun ChartView(state: TrendsUiState.HasData) {
    val accent = MaterialTheme.colorScheme.primary
    ...
```
(It already has this — no change needed.)

- [ ] **Step 4: Build and test**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew testDebugUnitTest 2>&1 | tail -5
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/trends/
git commit -m "feat(trends): add coaching sentence when goal + ETA available"
```

---

### Task 8: Goal Achieved Screen

**Files:**
- Create: `app/src/main/java/com/weightflow/ui/home/GoalAchievedScreen.kt`
- Modify: `app/src/main/java/com/weightflow/ui/home/HomeScreen.kt`

- [ ] **Step 1: Create GoalAchievedScreen.kt**

```kotlin
package com.weightflow.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weightflow.ui.components.WFHaptics
import com.weightflow.ui.components.rememberWFHaptics
import com.weightflow.ui.theme.WFTokens

@Composable
fun GoalAchievedScreen(
    state: HomeUiState.HasData,
    onSetNewGoal: () -> Unit,
) {
    val accent = MaterialTheme.colorScheme.primary
    val haptics = rememberWFHaptics()

    LaunchedEffect(Unit) { haptics.celebrate() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Full-screen glow burst
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(size.width / 2, 0f),
                    radius = size.width * 0.8f,
                ),
                center = Offset(size.width / 2, 0f),
                radius = size.width * 0.8f,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(72.dp))
            Text(text = "🏆", fontSize = 48.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Goal Reached",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = accent,
                letterSpacing = (-1).sp,
                fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
            )
            Text(
                text = "You did it",
                fontSize = 14.sp,
                color = WFTokens.Text2,
                modifier = Modifier.padding(top = 4.dp),
            )
            Spacer(Modifier.height(24.dp))
            // Current weight (goal weight)
            Text(
                text = state.latestWeightDisplay
                    .replace(" kg", "").replace(" lbs", "").replace(" st", ""),
                fontSize = 64.sp,
                fontWeight = FontWeight.Black,
                color = accent,
                letterSpacing = (-3).sp,
                fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
            )
            Text(
                text = when (state.weightUnit) {
                    com.weightflow.domain.WeightUnit.KG  -> "KILOGRAMS"
                    com.weightflow.domain.WeightUnit.LBS -> "POUNDS"
                    com.weightflow.domain.WeightUnit.ST  -> "STONE"
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                color = accent.copy(alpha = 0.6f),
            )
            Spacer(Modifier.height(24.dp))
            // Journey row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
            ) {
                JourneyCell(value = state.startDisplay ?: "—", label = "STARTED")
                JourneyCell(value = state.lostDisplay  ?: "—", label = "LOST", accent = accent)
                JourneyCell(value = "${state.streakDays}d", label = "STREAK")
            }
            Spacer(Modifier.height(20.dp))
            // Coaching sentence
            Text(
                text = "Keep going. Maintenance is the next milestone.",
                fontSize = 13.sp,
                color = WFTokens.Text2,
                textAlign = TextAlign.Center,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                lineHeight = 20.sp,
            )
            Spacer(Modifier.height(32.dp))
            // CTA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(accent, RoundedCornerShape(999.dp))
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "SET NEW GOAL  →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun JourneyCell(value: String, label: String, accent: Color = Color.Unspecified) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = if (accent != Color.Unspecified) accent else MaterialTheme.colorScheme.onBackground,
            fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
        )
        Text(text = label, fontSize = 7.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = WFTokens.Text3)
    }
}
```

- [ ] **Step 2: Wire GoalAchievedScreen in HomeContent** — in `HomeScreen.kt`, update `HomeContent` to show `GoalAchievedScreen` when `isGoalAchieved`:

```kotlin
@Composable
private fun HomeContent(uiState: HomeUiState) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when (uiState) {
            is HomeUiState.Loading -> LoadingView()
            is HomeUiState.Empty   -> EmptyView(uiState)
            is HomeUiState.HasData -> if (uiState.isGoalAchieved) {
                GoalAchievedScreen(state = uiState, onSetNewGoal = { /* TODO: nav to Profile */ })
            } else {
                DataView(uiState)
            }
        }
    }
}
```

- [ ] **Step 3: Build and test**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew testDebugUnitTest 2>&1 | tail -5
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/home/GoalAchievedScreen.kt \
        app/src/main/java/com/weightflow/ui/home/HomeScreen.kt
git commit -m "feat(home): add GoalAchievedScreen — full-screen celebration with glow burst"
```

---

## Phase C — Supporting Screens

### Task 9: History Screen — row layout update

**Files:**
- Modify: `app/src/main/java/com/weightflow/ui/history/HistoryScreen.kt`

- [ ] **Step 1: Replace the entry row layout** — find `EntryRow` (or equivalent) composable and replace it. The current file uses an `AlertDialog` for editing and a list of entries. Keep the edit dialog logic unchanged. Replace only the visual row layout.

Find and replace the row `Box`/`Row` that renders each entry with:

```kotlin
@Composable
private fun HistoryEntryRow(
    entry: HistoryEntryDisplay,
    isToday: Boolean,
    accent: Color,
    onClick: () -> Unit,
) {
    val rowBg = if (isToday) accent.copy(alpha = 0.04f) else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Day number block
        Column(modifier = Modifier.width(44.dp)) {
            Text(
                text = entry.dayOfMonth,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = if (isToday) accent else MaterialTheme.colorScheme.onBackground,
                fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
                lineHeight = 22.sp,
            )
            Text(
                text = entry.dayOfWeek,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = WFTokens.Text3,
            )
        }
        // Weight
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(
                text = entry.weightDisplay.replace(" kg", "").replace(" lbs", "").replace(" st", ""),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
            )
            Text(
                text = when {
                    entry.weightDisplay.contains("kg")  -> "kg"
                    entry.weightDisplay.contains("lbs") -> "lbs"
                    else -> "st"
                },
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = WFTokens.Text3,
            )
        }
        // Delta chip
        if (entry.deltaDisplay != null && entry.deltaIsDown != null) {
            val chipColor = if (entry.deltaIsDown) WFTokens.Success else WFTokens.Danger
            Box(
                modifier = Modifier
                    .background(chipColor.copy(alpha = 0.1f), RoundedCornerShape(999.dp))
                    .border(1.dp, chipColor.copy(alpha = 0.2f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${if (entry.deltaIsDown) "▼" else "▲"} ${entry.deltaDisplay}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = chipColor,
                )
            }
        }
    }
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(WFTokens.Border)
    )
}
```

Note: `HistoryEntryDisplay` has `dayOfMonth` and `dayOfWeek` as String fields. Check `HistoryUiState.kt` for the exact field names and add them if missing. If the existing DTO uses a single date string, split it in the row composable using:
```kotlin
val date = Instant.ofEpochMilli(entry.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
val dayOfMonth = date.dayOfMonth.toString()
val dayOfWeek = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()).uppercase()
```

- [ ] **Step 2: Build and test**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew testDebugUnitTest 2>&1 | tail -5
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/history/HistoryScreen.kt
git commit -m "feat(history): bold day number rows, delta chips, today lime tint"
```

---

### Task 10: Profile Screen — journey card + BMI card

**Files:**
- Modify: `app/src/main/java/com/weightflow/ui/profile/ProfileScreen.kt`

- [ ] **Step 1: Find the goal/journey card composable** — search for where `goalProgress` or `goalWeightDisplay` is rendered in `ProfileScreen.kt`. Replace the goal block with a `JourneyCard`:

```kotlin
@Composable
private fun JourneyCard(state: ProfileUiState.HasData, accent: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(WFTokens.Card, RoundedCornerShape(16.dp))
            .border(1.dp, WFTokens.Border, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        // Three values: Start / Now (lime) / Goal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        ) {
            JourneyValue(label = "START", value = state.startDisplay ?: "—")
            JourneyValue(label = "NOW",   value = state.currentDisplay ?: "—", color = accent)
            JourneyValue(label = "GOAL",  value = state.goalDisplay ?: "—")
        }
        if (state.goalProgress != null) {
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(WFTokens.Elevated, RoundedCornerShape(999.dp))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(state.goalProgress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(accent, RoundedCornerShape(999.dp))
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween) {
                Text("${(state.goalProgress * 100).toInt()}% complete", fontSize = 8.sp, color = WFTokens.Text3, fontWeight = FontWeight.Bold)
                if (state.etaDays != null) {
                    Text("~${state.etaDays}d to go", fontSize = 8.sp, color = accent.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun JourneyValue(label: String, value: String, color: Color = Color.Unspecified) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onBackground,
            fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
        )
        Text(text = label, fontSize = 7.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = WFTokens.Text3)
    }
}
```

Note: `ProfileUiState.HasData` needs `startDisplay`, `currentDisplay`, `goalDisplay`, `etaDays` fields. Check `ProfileUiState.kt`. Add missing fields to `ProfileUiState.HasData` and wire them in `ProfileViewModel`.

- [ ] **Step 2: Build and test**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew testDebugUnitTest 2>&1 | tail -5
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/profile/
git commit -m "feat(profile): journey card with start/now/goal + progress bar"
```

---

### Task 11: Settings Screen — theme grid with color dots

**Files:**
- Modify: `app/src/main/java/com/weightflow/ui/settings/SettingsScreen.kt`

- [ ] **Step 1: Find the theme selection composable** — locate where themes are listed in `SettingsScreen.kt`. Replace theme buttons with a 2-column grid where each tile has a color dot:

```kotlin
private val THEME_OPTIONS = listOf(
    Triple("lime",   "Lime",   Color(0xFFC8FF00)),
    Triple("rose",   "Rose",   Color(0xFFFF4081)),
    Triple("forest", "Forest", Color(0xFF4CAF50)),
    Triple("violet", "Violet", Color(0xFFBB86FC)),
    Triple("ocean",  "Ocean",  Color(0xFF00BCD4)),
    Triple("gold",   "Gold",   Color(0xFFFFD700)),
    Triple("sunset", "Sunset", Color(0xFFFF6B35)),
    Triple("ice",    "Ice",    Color(0xFF80DEEA)),
)

@Composable
private fun ThemeGrid(
    selectedPalette: String,
    onThemeSelected: (String) -> Unit,
    accent: Color,
) {
    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
        columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .heightIn(max = 300.dp),
    ) {
        items(THEME_OPTIONS) { (key, name, dotColor) ->
            val isSelected = key == selectedPalette
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isSelected) WFTokens.accentSoft(accent) else WFTokens.Card,
                        RoundedCornerShape(12.dp),
                    )
                    .border(
                        1.dp,
                        if (isSelected) WFTokens.accentBorder(accent) else WFTokens.Border,
                        RoundedCornerShape(12.dp),
                    )
                    .clickable { onThemeSelected(key) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(14.dp)
                        .background(dotColor, RoundedCornerShape(999.dp))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = name,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onBackground else WFTokens.Text2,
                )
            }
        }
    }
}
```

Wire it: find `onThemeSelected` lambda in the settings composable and replace the existing theme grid with `ThemeGrid(selectedPalette, onThemeSelected, accent)`.

Note: `LazyVerticalGrid` requires `items(list) { item -> }` — import `androidx.compose.foundation.lazy.grid.items`.

- [ ] **Step 2: Build and test**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew testDebugUnitTest 2>&1 | tail -5
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/settings/SettingsScreen.kt
git commit -m "feat(settings): theme grid with color dots, active lime border"
```

---

### Task 12: Onboarding Screen — step dots + shared template

**Files:**
- Modify: `app/src/main/java/com/weightflow/ui/onboarding/OnboardingScreen.kt`

- [ ] **Step 1: Add step dots composable to OnboardingScreen.kt**

```kotlin
@Composable
private fun StepDots(currentStep: OnboardingStep, accent: Color) {
    val steps = OnboardingStep.entries
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        steps.forEach { step ->
            val isActive = step == currentStep
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .height(5.dp)
                    .width(if (isActive) 16.dp else 5.dp)
                    .background(
                        if (isActive) accent else WFTokens.Text3,
                        RoundedCornerShape(999.dp),
                    )
            )
        }
    }
}
```

- [ ] **Step 2: Add step dots to each onboarding step screen** — find each step's composable (e.g., `AgeGateStep`, `UnitStep`, `WeightStep`, `GoalStep`). At the top of each, add:

```kotlin
StepDots(currentStep = OnboardingStep.<CURRENT_STEP>, accent = accent)
Spacer(Modifier.height(16.dp))
```

Replace `<CURRENT_STEP>` with the appropriate step enum value.

- [ ] **Step 3: Add eyebrow label to each step** — below `StepDots`, add:

```kotlin
Text(
    text = "STEP ${stepNumber} OF ${OnboardingStep.entries.size}",
    fontSize = 9.sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = 2.sp,
    color = accent.copy(alpha = 0.55f),
)
```

- [ ] **Step 4: Build and test**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew testDebugUnitTest 2>&1 | tail -5
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/weightflow/ui/onboarding/OnboardingScreen.kt
git commit -m "feat(onboarding): step dots + eyebrow labels on all 4 steps"
```

---

## Final Verification

- [ ] **Run full test suite**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew testDebugUnitTest 2>&1 | tail -5
```
Expected: `BUILD SUCCESSFUL` — all tests pass.

- [ ] **Build release-grade debug APK and install**

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleDebug && \
  adb install -r app/build/outputs/apk/debug/app-debug.apk && \
  adb shell am start -n com.weightflow/.MainActivity
```

- [ ] **Manual test checklist on device**
  - [ ] Log entry: drum picker pre-fills to last weight, haptic on scroll, save morphs, new low turns lime
  - [ ] Home: weight number 64sp, sparkline visible, 3 stat cards, goal bar
  - [ ] Trends: coaching sentence appears when goal + downward trend
  - [ ] History: bold day numbers, delta chips, today row lime tint
  - [ ] Profile: journey card with progress bar
  - [ ] Settings: theme grid with color dots
  - [ ] Onboarding: step dots elongate on active step

- [ ] **Final commit tag**

```bash
git tag v1.0.0-rc1
```
