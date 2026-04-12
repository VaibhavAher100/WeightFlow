# Issue 8: CsvExporter domain module + tests

## Parent PRD
docs/specs/2026-04-12-weightflow-master-prd.md

## What to build
A pure Kotlin exporter that serialises the full weight log to a CSV string in the user's preferred unit. No Android dependencies.

End-to-end: `CsvExporter.export(entries, WeightUnit.LBS)` returns a valid CSV string with headers `date,weight_lbs` and all entries converted from internal kg storage.

## Acceptance criteria
- [ ] `CsvExporter.export(entries: List<WeightEntry>, unit: WeightUnit): String`
- [ ] Output headers: `date,weight_<unit>` (e.g., `weight_kg`, `weight_lbs`, `weight_st`)
- [ ] Date format: ISO-8601 (`yyyy-MM-dd`)
- [ ] Entries sorted chronologically (oldest first)
- [ ] Handles empty list (returns header row only)
- [ ] No Android imports
- [ ] `CsvExporterTest`: correct headers per unit, correct conversion, chronological order, empty list

## Blocked by
- Blocked by #2 (Room schema — needs WeightEntry shape)

## User stories addressed
64 (export CSV from Settings).
