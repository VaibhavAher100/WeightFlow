## Problem

`WeightRepository.getAllEntries()` has no ordering guarantee. `CsvExporter` sorts defensively internally. Future callers (Trends chart, Home sparkline, History list, analytics) will all assume sorted-by-timestamp data and sort defensively themselves — or worse, silently rely on insertion order which Room does not guarantee.

## Proposed Interface

Delete `getAllEntries()`. Replace with named, direction-encoded methods backed by a `SortedEntries` wrapper type.

```kotlin
// SortedEntries — only constructable inside the repository package
@JvmInline
value class SortedEntries private constructor(val entries: List<WeightEntry>) {
    companion object {
        internal fun from(raw: List<WeightEntry>): SortedEntries =
            SortedEntries(raw.sortedBy { it.timestamp })

        // Test-only constructor — visible to test sources only
        @VisibleForTesting
        fun testOnly(entries: List<WeightEntry>) = SortedEntries(entries)
    }
}

interface WeightRepository {
    fun getEntriesAscending(): Flow<SortedEntries>            // charts, CSV export
    fun getEntriesDescending(): Flow<SortedEntries>           // History list
    fun getRecentEntriesDescending(n: Int): Flow<SortedEntries>  // Home sparkline
    fun getEntriesInRangeAscending(start: Long, end: Long): Flow<SortedEntries>  // Trends range

    suspend fun insert(entry: WeightEntry)
    suspend fun update(entry: WeightEntry)
    suspend fun delete(entry: WeightEntry)
    suspend fun deleteAll()
}
```

## Room DAO

Every query carries an explicit `ORDER BY` — sort is done in SQLite, not in memory:

```kotlin
@Query("SELECT * FROM weight_entries ORDER BY timestamp ASC")
fun getAllAscending(): Flow<List<WeightEntryEntity>>

@Query("SELECT * FROM weight_entries ORDER BY timestamp DESC")
fun getAllDescending(): Flow<List<WeightEntryEntity>>
```

Repository maps entity list → `SortedEntries.from(mapped)`.

## Caller map

| Caller | Method |
|---|---|
| CsvExporter | `getEntriesAscending()` — drops its internal sort |
| Home sparkline | `getRecentEntriesDescending(7)` |
| Trends chart | `getEntriesInRangeAscending(start, end)` |
| History screen | `getEntriesDescending()` |
| BadgeObserver | `getEntriesAscending()` (streak calculation needs chronological order) |

## What this fixes

- No caller can accidentally receive unsorted data — the type makes it impossible.
- No defensive `.sortedBy{}` scattered across callers.
- `CsvExporter` signature changes to `export(sorted: SortedEntries, unit: WeightUnit)` — the sort contract is in the type, not the docs.
- Existing `CsvExporterTest` uses `SortedEntries.testOnly(...)` — one-line change, all assertions preserved.

## Affects

Issues #9 (Repository layer), #8 (CsvExporter), #15 (Trends screen), #14 (Home screen), #16 (History screen)
