# Issue 2: Room schema + DAOs (WeightEntry + UserProfile)

## Parent PRD
docs/specs/2026-04-12-weightflow-master-prd.md

## What to build
Define the Room database schema and DAOs for the two core entities. This is the single source of truth for all weight and profile data in the app.

End-to-end: a `WeightEntryDao` test can insert an entry, query it back, and observe a Flow emission on change. All weights stored in kg internally — unit conversion is never a database concern.

## Acceptance criteria
- [ ] `WeightEntryEntity`: id (autoGenerate), timestamp (Long epoch ms), weightKg (Double), note (String, default empty)
- [ ] `UserProfileEntity`: id=1 singleton, displayName, goalWeightKg (nullable), targetDate (nullable Long), heightCm (nullable Int), maintenanceMode (Boolean), maintenanceRangeKg (Double default 1.0)
- [ ] `WeightEntryDao`: insert, update, delete, getAll (Flow), getByDateRange, getMostRecent(n)
- [ ] `UserProfileDao`: upsert singleton, get (Flow)
- [ ] `AppDatabase`: Room singleton with both entities, version=1, exported schema
- [ ] Migrations framework in place — `fallbackToDestructiveMigration()` NOT used
- [ ] `WeightEntryDaoTest` (instrumented, in-memory DB): insert/query/delete/update/flow emission
- [ ] `UserProfileDaoTest` (instrumented): upsert + read back

## Blocked by
- Blocked by #1 (project setup)

## User stories addressed
Foundation for: 8–13 (logging), 14–23 (Home), 24–32 (Trends), 33–37 (History), 38–43 (Goals).
