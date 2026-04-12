## Problem

`WeightConverter` is correct but if unit conversion happens inside each UI component independently, one lagging `collectAsStateWithLifecycle()` produces a screen where the hero weight shows lbs while the sparkline shows kg. No test currently catches this.

## Proposed Interface

```kotlin
// domain/weight/WeightTypes.kt

@JvmInline
value class StoredWeight(val kg: Double)   // Room column type — never shown directly

// WeightConverter gains a single dispatch function
fun StoredWeight.toDisplayString(unit: WeightUnit): String =
    WeightConverter.format(kg, unit)
```

Conversion happens **once**, in `HomeUiStateMapper`, inside the ViewModel's `combine {}`:

```kotlin
// HomeUiStateMapper.kt — single conversion point
object HomeUiStateMapper {
    fun map(entries: List<StoredWeight>, unit: WeightUnit, goal: GoalEntity?, streak: Int): HomeUiState.Ready {
        val latest = entries.lastOrNull()
        val prev   = entries.dropLast(1).lastOrNull()
        return HomeUiState.Ready(
            currentWeight  = latest?.toDisplayString(unit) ?: "--",
            trendDelta     = if (latest != null && prev != null)
                                WeightConverter.format(latest.kg - prev.kg, unit) else null,
            sparklineKg    = entries.map { it.kg },   // raw kg for Vico axis
            sparklineUnit  = unit,
            goalProgress   = goal?.let { GoalProgressCalculator.compute(latest?.kg, it) },
            streak         = streak,
            recentEntries  = entries.takeLast(3).map { it.toDisplayString(unit) }
        )
    }
}
```

```kotlin
// HomeViewModel — atomic re-render on unit change
val uiState = combine(
    weightRepository.getRecentEntriesDescending(7),   // Flow<List<StoredWeight>>
    userPrefsDataStore.unitPref,                       // Flow<WeightUnit>
    goalRepository.getActiveGoal(),
    streakRepository.currentStreak()
) { entries, unit, goal, streak ->
    HomeUiStateMapper.map(entries, unit, goal, streak)
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)
```

## What this fixes

- `unitPref` change fires `combine` → `HomeUiStateMapper.map` runs once → single `HomeUiState` emitted → all 6 components recompose from the same snapshot. No component lags.
- `StoredWeight` in Room documents the kg-storage contract at the type level. Zero runtime overhead (`@JvmInline`).
- Composables never call `WeightConverter` directly — they only render pre-formatted strings.
- `HomeUiStateMapper` is a pure function — trivially testable with no Android deps.

## Not included (deferred)

Full `DisplayWeight` sealed class hierarchy (`Metric`, `Imperial`, `Stone` subtypes) — this is useful but overkill for Phase 1. Add in Phase 3 if exhaustive `when` enforcement is needed.

## Affects

Issues #4 (WeightConverter), #15 (Home screen), #3 (DataStore)
