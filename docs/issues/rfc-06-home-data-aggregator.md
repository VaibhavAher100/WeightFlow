## Problem

`HomeUiState` has 8 fields sourced from 3 repositories + 2 domain calculators. Without intervention, `HomeViewModel` becomes a 200-line god class with 5 direct dependencies. `HomeViewModelTest` (not yet pre-written) will need to mock all 5 — brittle, verbose, hard to maintain.

## Proposed Interface

Introduce a `HomeDataAggregator` pure Kotlin object that owns the data combination. `HomeViewModel` gets one dependency.

```kotlin
// domain/HomeDataAggregator.kt — pure Kotlin, no Android deps
class HomeDataAggregator(
    private val weightRepository: WeightRepository,
    private val userProfileRepository: UserProfileRepository,
    private val userPrefsDataStore: UserPrefsDataStore,
    private val goalStateMachine: GoalStateMachine,
    private val badgeObserver: BadgeObserver
) {
    fun observe(): Flow<HomeSnapshot> = combine(
        weightRepository.getRecentEntriesDescending(7),
        userPrefsDataStore.unitPref,
        userProfileRepository.getProfile(),
        goalStateMachine.state,
        badgeObserver.earnedBadges
    ) { entries, unit, profile, goalState, badges ->
        HomeSnapshot(
            entries = entries,
            unit = unit,
            profile = profile,
            goalState = goalState,
            earnedBadgeCount = badges.size
        )
    }
}

data class HomeSnapshot(
    val entries: SortedEntries,
    val unit: WeightUnit,
    val profile: UserProfile?,
    val goalState: GoalState,
    val earnedBadgeCount: Int
)
```

```kotlin
// HomeViewModel — thin mapper
class HomeViewModel(private val aggregator: HomeDataAggregator) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = aggregator.observe()
        .map { HomeUiStateMapper.map(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)
}
```

## HomeViewModelTest — what gets tested

```kotlin
// Simple fake for testing — one dependency to mock
val fakeAggregator = FakeHomeDataAggregator()
val viewModel = HomeViewModel(fakeAggregator)

@Test
fun `loading state before first emission`() {
    assertEquals(HomeUiState.Loading, viewModel.uiState.value)
}

@Test
fun `ready state emitted after aggregator emits snapshot`() = runTest {
    fakeAggregator.emit(testSnapshot)
    assertEquals(HomeUiState.Ready::class, viewModel.uiState.value::class)
}
```

## HomeDataAggregatorTest — what gets tested

```kotlin
// Tests the combination logic directly
@Test
fun `snapshot reflects unit change immediately`() = runTest {
    fakePrefs.emitUnit(WeightUnit.LBS)
    val snapshot = aggregator.observe().first()
    assertEquals(WeightUnit.LBS, snapshot.unit)
}
```

## What this fixes

- `HomeViewModel` has 1 dependency instead of 5.
- `HomeDataAggregator` is pure Kotlin — testable without Android test runner.
- `HomeUiStateMapper` is also pure — testable in isolation.
- Two separate test surfaces: aggregator (data combination) + ViewModel (state mapping) + mapper (formatting).
- Adding a new data source to Home = change `HomeDataAggregator` only, ViewModel stays untouched.

## Affects

Issues #15 (Home screen), #10 (Repository layer), #24 (BadgeObserver RFC), #26 (GoalStateMachine RFC)
