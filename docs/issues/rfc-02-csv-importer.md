## Problem

Four parsers exist in isolation with no routing logic. Format auto-detection and orchestration will be scattered across `ImportViewModel`. Adding a 5th parser requires touching the orchestrator. No test covers the seam between format detection and parsing.

## Proposed Interface

```kotlin
enum class CsvFormat { WEIGHT_FIT, HAPPY_SCALE, APPLE_HEALTH, GENERIC, UNKNOWN }

interface CsvParser {
    val format: CsvFormat
    fun canParse(header: String): Boolean   // header-only sniff, never parses full file
    fun parse(csv: String, existingEntries: List<WeightEntry>): ParseResult
}

// ParseResult updated to include detected format
sealed class ParseResult {
    data class Success(
        val entries: List<WeightEntry>,
        val duplicatesSkipped: Int,
        val detectedFormat: CsvFormat    // NEW — surfaced to ImportViewModel
    ) : ParseResult()
    data class Error(val message: String) : ParseResult()
}

// Single entry point
interface CsvImporter {
    fun import(bytes: ByteArray, existingEntries: List<WeightEntry>): ParseResult
}
```

## Implementation

```kotlin
class CsvImporterImpl(private val parsers: List<CsvParser>) : CsvImporter {
    override fun import(bytes: ByteArray, existingEntries: List<WeightEntry>): ParseResult {
        val csv = bytes.toString(Charsets.UTF_8)
        val header = csv.lineSequence().firstOrNull() ?: ""
        val parser = parsers.firstOrNull { it.canParse(header) }
            ?: return ParseResult.Error("Unrecognised CSV format")
        return parser.parse(csv, existingEntries)
    }
}
```

Parser registration in `WeightFlowApp` (DI root — only place that changes when adding a parser):
```kotlin
val csvImporter = CsvImporterImpl(listOf(
    AppleHealthParser(),   // most specific first
    HappyScaleParser(),
    WeightFitParser(),
    GenericCsvParser()     // catch-all last
))
```

## Adding a 5th parser

Implement `CsvParser`, add to the list in `WeightFlowApp`. Nothing else changes.

## ImportViewModel usage

```kotlin
val result = csvImporter.import(fileBytes, existingEntries)
if (result is ParseResult.Success) {
    _uiState.update { it.copy(
        detectedFormat = result.detectedFormat,  // show "Detected: Happy Scale [Change ▾]"
        importedCount = result.entries.size
    )}
}
```

## Affects

Issues #8 (CSV parsers), #22 (CSV import/export UI)
