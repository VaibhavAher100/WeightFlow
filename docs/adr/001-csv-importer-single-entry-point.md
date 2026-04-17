# ADR-001: CsvImporter Single Entry Point + CsvFormat Enum

**Status:** Accepted  
**Date:** 2026-04-16  
**RFC:** #25

## Context
The project had four separate CSV parser objects (WeightFitParser, HappyScaleParser, AppleHealthParser, GenericCsvParser) with no single entry point. Callers had to know which format to try and call parsers directly. This caused scattered format-detection logic and no way to surface which format was actually matched.

## Decision
Introduce `CsvImporter` as the single entry point for all CSV imports. `CsvImporter.import(csv, existingEntries)` tries parsers in order (HappyScale → AppleHealth → Generic → WeightFit) and returns a `ParseResult.Success` with the detected `CsvFormat` set. Add `CsvFormat` enum (WEIGHT_FIT, HAPPY_SCALE, APPLE_HEALTH, GENERIC).

## Detection Order Rationale
More-specific formats first: HappyScale (quoted headers + "Pounds") and AppleHealth ("HKQuantityTypeIdentifierBodyMass") are distinctive enough to prevent false-positive matches. Generic (explicit `weight_kg`/`weight_lbs` headers) before WeightFit (generic `date`/`weight`).

## Consequences
- All UI/ViewModel code calls `CsvImporter.import()` — never individual parsers directly
- `ParseResult.Success` backward-compatible: `format` defaults to `null` when called via individual parsers
- 5 new unit tests in `CsvImporterTest.kt`
