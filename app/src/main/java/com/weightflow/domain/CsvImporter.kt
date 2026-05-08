package com.weightflow.domain

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader

/**
 * Single entry point for CSV import.
 *
 * Enforcement order (before any parser runs):
 *  1. Byte-size limit: 5 MB max (checked on the raw String length as UTF-16 chars ×2 is a
 *     conservative upper bound; real byte size at UTF-8 is always ≤ that).
 *  2. Row-count limit: [MAX_ROWS] data rows max (header excluded).
 *  3. Column-count limit: [MAX_COLUMNS] columns max.
 *  4. Blank-file guard: at least one data row required after the header.
 *
 * Detection order: HappyScale → AppleHealth → Generic → WeightFit.
 * More specific/distinctive formats are tried first to avoid false positives.
 */
object CsvImporter {

    /** Maximum file size in bytes (5 MB). */
    private const val MAX_BYTES = 5_000_000

    fun import(csv: String, existingEntries: List<WeightEntry>): ParseResult {
        // 1. Byte-size check BEFORE reading content into structured form.
        //    String.length returns UTF-16 code units; each is at most 3 bytes in UTF-8,
        //    so length * 2 is a safe conservative upper bound. We use the simpler
        //    toByteArray().size only once as an exact check.
        if (csv.toByteArray(Charsets.UTF_8).size > MAX_BYTES) {
            return ParseResult.Error("File too large (max 5 MB)")
        }

        // 2. Parse into rows once so we can enforce structural limits before dispatching.
        val rows: List<List<String>> = runCatching {
            csvReader().readAll(csv)
        }.getOrElse {
            return ParseResult.Error("CSV could not be parsed: ${it.message}")
        }

        // 3. Blank-file guard (no rows at all, or only a header with no data rows).
        if (rows.isEmpty()) {
            return ParseResult.Error("CSV file is empty")
        }
        if (rows.size == 1) {
            // Only a header row, zero data rows.
            return ParseResult.Error("CSV file contains no data rows")
        }

        // 4. Row-count limit (exclude header row from the count).
        val dataRowCount = rows.size - 1
        if (dataRowCount > MAX_ROWS) {
            return ParseResult.Error("Too many rows ($dataRowCount rows; max $MAX_ROWS)")
        }

        // 5. Column-count limit (check the header row).
        val columnCount = rows[0].size
        if (columnCount > MAX_COLUMNS) {
            return ParseResult.Error("Too many columns ($columnCount columns; max $MAX_COLUMNS)")
        }

        // ── Parser dispatch ───────────────────────────────────────────────────

        // Try HappyScale first (distinctive: quoted headers + "Pounds")
        val happyScaleResult = HappyScaleParser.parse(csv, existingEntries)
        if (happyScaleResult is ParseResult.Success) {
            return happyScaleResult.copy(format = CsvFormat.HAPPY_SCALE)
        }

        // Try AppleHealth (distinctive: "type", "HKQuantityTypeIdentifierBodyMass")
        val appleHealthResult = AppleHealthParser.parse(csv, existingEntries)
        if (appleHealthResult is ParseResult.Success) {
            return appleHealthResult.copy(format = CsvFormat.APPLE_HEALTH)
        }

        // Try Generic (distinctive: "weight_kg" or "weight_lbs" specific header)
        val genericResult = GenericCsvParser.parse(csv, existingEntries)
        if (genericResult is ParseResult.Success) {
            return genericResult.copy(format = CsvFormat.GENERIC)
        }

        // Try WeightFit last (generic: just "date" + "weight")
        val weightFitResult = WeightFitParser.parse(csv, existingEntries)
        if (weightFitResult is ParseResult.Success) {
            return weightFitResult.copy(format = CsvFormat.WEIGHT_FIT)
        }

        // All parsers failed — return the last error
        return weightFitResult
    }
}
