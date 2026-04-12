# Issue 21: CSV import/export UI (Settings flow + background import + summary)

## Parent PRD
docs/specs/2026-04-12-weightflow-master-prd.md

## What to build
The full import/export UI in Settings. Export triggers a file-save dialog. Import opens a file picker, detects format, runs the appropriate parser in the background, and shows a summary.

End-to-end: user taps "Import CSV" → file picker opens → user selects WeightFit export → parser detects format automatically → background import runs → "127 entries imported, 3 skipped as duplicates" summary shown → entries visible in History immediately.

## Acceptance criteria
- [ ] Export: tapping "Export CSV" opens Android file-save dialog (Storage Access Framework); file named `weightflow-export-YYYY-MM-DD.csv`; writes `CsvExporter` output in user's current unit
- [ ] Import: tapping "Import CSV" opens file picker (SAF); accepts `.csv` and `.xml` files
- [ ] Format auto-detection: parser chosen based on file headers; falls back to GenericCsvParser
- [ ] Import runs in background coroutine (not blocking UI); progress indicator shown during import
- [ ] Import summary bottom sheet: "X entries imported, Y duplicates skipped, Z errors" with Close button
- [ ] Imported entries immediately visible in History (Room Flow emission)
- [ ] Error handling: unrecognised format shows clear error message; partial imports not committed (transaction)
- [ ] `ImportViewModel`: `StateFlow<ImportUiState>` (idle, loading, success, error)
- [ ] RTL-safe

## Blocked by
- Blocked by #7 (CSV parsers)
- Blocked by #8 (CSV exporter)
- Blocked by #17 (Profile/Settings screen — import/export entry points)

## User stories addressed
64 (export), 65 (import), 69–74 (all migration stories including duplicate detection + summary).
