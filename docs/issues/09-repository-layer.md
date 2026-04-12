# Issue 9: Repository layer (WeightRepository + UserProfileRepository)

## Parent PRD
docs/specs/2026-04-12-weightflow-master-prd.md

## What to build
The repository layer that bridges DAOs + DataStore to the UI layer. ViewModels never touch Room or DataStore directly — they go through repositories. Repositories expose clean Flows and suspend functions.

End-to-end: `WeightRepository.getAllEntries()` returns a `Flow<List<WeightEntry>>` that emits whenever Room changes. `WeightRepository.insert(entry)` writes to Room and returns. ViewModels can observe without knowing about the database.

## Acceptance criteria
- [ ] `WeightRepository`: `getAllEntries(): Flow<List<WeightEntry>>`, `getRecentEntries(n): Flow<List<WeightEntry>>`, `getEntriesInRange(start, end): Flow<List<WeightEntry>>`, `insert(entry)`, `update(entry)`, `delete(entry)`, `deleteAll()`
- [ ] `UserProfileRepository`: `getProfile(): Flow<UserProfile?>`, `upsertProfile(profile)`, `setMaintenanceMode(enabled)`
- [ ] Both repositories constructed in `WeightFlowApp` and passed down via manual DI
- [ ] `WeightRepositoryTest` (JVM, using in-memory Room): insert → getAllEntries emits; delete → flow updates
- [ ] `UserProfileRepositoryTest`: upsert → getProfile emits correct data

## Blocked by
- Blocked by #2 (Room schema)
- Blocked by #3 (DataStore wrapper)

## User stories addressed
Enables all ViewModel-dependent stories: 8–23 (logging + Home), 24–32 (Trends), 33–37 (History), 38–43 (Goals).
