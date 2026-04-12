## Problem

`GoalProgressCalculator` returns `isGoalReached=true` but nothing defines who calls `setMaintenanceMode(true)` or triggers the celebration screen. That side-effect chain will be buried silently in `HomeViewModel`, invisible to tests. Multiple ViewModels could race to write maintenance state.

## Proposed Interface

```kotlin
sealed class GoalState {
    data class Active(val progressPercent: Float, val daysRemaining: Int?) : GoalState()
    data class Reached(val goalKg: Double) : GoalState()
    data class Maintenance(val goalKg: Double, val drift: DriftDirection) : GoalState()
    data class NewGoalSet(val previousGoalKg: Double, val newGoalKg: Double) : GoalState()
}

interface GoalStateMachine {
    val state: StateFlow<GoalState>

    // Active → Reached. Illegal transitions throw IllegalStateTransitionException.
    fun onWeightLogged(currentKg: Double)

    // Reached → Maintenance. Writes maintenanceMode=true to UserProfile.
    suspend fun chooseMaintenance()

    // Reached → NewGoalSet. Chains history, resets startKg.
    suspend fun chooseNewGoal(newGoalKg: Double, targetDate: LocalDate?)

    // Maintenance → Maintenance. Called on every new log in maintenance mode.
    fun updateDrift(currentKg: Double)

    // Called once at app start to rehydrate state from UserProfile.
    fun restore(profile: UserProfile)
}
```

## Side effects per transition

| Transition | Side effects (owned by machine) |
|---|---|
| Active → Reached | None. UI observes `Reached`, shows celebration + choice prompt. |
| Reached → Maintenance | Writes `maintenanceMode=true` + timestamp to `UserProfile`. Awards GoalCrusher badge via BadgeObserver. |
| Reached → NewGoalSet | Writes new goal to `UserProfile`, resets `startKg`. Awards GoalCrusher badge. |
| Maintenance → Maintenance | No DB write. Drift derived from latest log. |

## ViewModel usage

```kotlin
// LogEntryViewModel — after save
machine.onWeightLogged(newEntry.weightKg)

// HomeViewModel — pure observer
machine.state.collectAsStateWithLifecycle().also { goalState ->
    when (goalState) {
        is GoalState.Reached -> uiState = uiState.copy(showCelebration = true)
        is GoalState.Maintenance -> uiState = uiState.copy(maintenanceMode = true, drift = goalState.drift)
        is GoalState.Active -> uiState = uiState.copy(progress = goalState.progressPercent)
        else -> Unit
    }
}
```

## Cold start rehydration

```kotlin
// WeightFlowApp.onCreate()
val profile = userProfileRepository.getProfileBlocking()
goalStateMachine.restore(profile)
```

One-time cost. Without it, cold launch into Maintenance state shows wrong screen for one frame.

## Affects

Issues #6 (GoalProgressCalculator), #18 (Goal completion flow), #15 (Home screen)
