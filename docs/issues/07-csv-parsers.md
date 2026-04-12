# Issue 7: CSV parsers domain module + tests (all 4 formats)

## Parent PRD
docs/specs/2026-04-12-weightflow-master-prd.md

## What to build
A sealed class hierarchy of CSV parsers, one per supported import format. Each parser is isolated and independently testable. Includes duplicate detection logic.

End-to-end: given a WeightFit CSV string → `WeightFitParser.parse(csv)` returns `ParseResult.Success(listOf(...))` with correct entries in kg. Duplicate entries are detected and counted separately.

## Acceptance criteria
- [ ] `ParseResult` sealed class: `Success(entries: List<WeightEntry>, duplicatesSkipped: Int)` / `Error(message: String)`
- [ ] `WeightFitParser` — parses WeightFit CSV export format
- [ ] `HappyScaleParser` — parses Happy Scale CSV export format
- [ ] `AppleHealthParser` — parses Apple Health body mass XML/CSV export format
- [ ] `GenericCsvParser` — detects date + weight columns by header name heuristics; handles kg/lbs/st column headers
- [ ] All parsers convert to kg internally on parse
- [ ] Duplicate detection: entry is duplicate if same calendar day + weightKg within 0.01 of existing entry
- [ ] No Android imports
- [ ] `CsvParserTest` for each parser: valid file, malformed file, empty file, duplicate detection, unit conversion from lbs input

## Blocked by
- Blocked by #2 (Room schema — needs WeightEntry shape)

## User stories addressed
69–74 (all migration/import stories).
