package com.weightflow.domain

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.context.ExcessFieldsRowBehaviour
import com.github.doyaaaaaken.kotlincsv.dsl.context.InsufficientFieldsRowBehaviour
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs

// ── Internal limits ────────────────────────────────────────────────────────────

internal const val MAX_ROWS = 10_000
internal const val MAX_COLUMNS = 10

// ── Shared helpers ─────────────────────────────────────────────────────────────

private fun isDuplicate(entry: WeightEntry, existing: List<WeightEntry>): Boolean {
    val zone = ZoneId.systemDefault()
    val entryDate = LocalDate.ofInstant(Instant.ofEpochMilli(entry.timestamp), zone)
    return existing.any { ex ->
        val exDate = LocalDate.ofInstant(Instant.ofEpochMilli(ex.timestamp), zone)
        exDate == entryDate && abs(ex.weightKg - entry.weightKg) < 0.01
    }
}

private fun makeEntry(date: LocalDate, weightKg: Double) = WeightEntry(
    id = 0,
    timestamp = date.toEpochDay() * 86_400_000L,
    weightKg = weightKg,
    note = ""
)

/**
 * Reads all rows from [csv] via the RFC-4180 parser.
 *
 * Configuration choices:
 * - `skipEmptyLine = true` — blank lines are silently dropped rather than producing empty rows.
 * - `excessFieldsRowBehaviour = TRIM` — rows with more columns than the header have the extra
 *   fields silently discarded so the useful leading columns are still available.
 * - `insufficientFieldsRowBehaviour = IGNORE` — rows that are too short are skipped entirely.
 *   Individual parsers still guard against short rows with `if (row.size < N) continue`.
 *
 * Returns null only when the underlying parser throws for truly malformed input (e.g. unclosed
 * quote spanning the entire file).
 */
private fun readAllRows(csv: String): List<List<String>>? = runCatching {
    csvReader {
        skipEmptyLine = true
        excessFieldsRowBehaviour = ExcessFieldsRowBehaviour.TRIM
        insufficientFieldsRowBehaviour = InsufficientFieldsRowBehaviour.IGNORE
    }.readAll(csv)
}.getOrNull()

// ── WeightFit ──────────────────────────────────────────────────────────────────

object WeightFitParser {

    /**
     * Parses a WeightFit-style CSV export.
     *
     * Expected headers: `Date`, `Weight` (optionally `Weight (lbs)` to signal pounds).
     * All output weights are in kg regardless of input unit.
     */
    fun parse(csv: String, existingEntries: List<WeightEntry>): ParseResult {
        if (csv.isBlank()) return ParseResult.Success(emptyList(), 0)

        val rows = readAllRows(csv) ?: return ParseResult.Error("CSV could not be parsed")
        if (rows.isEmpty()) return ParseResult.Success(emptyList(), 0)

        val header = rows[0].joinToString(",")
        if (!header.contains("date", ignoreCase = true) || !header.contains("weight", ignoreCase = true)) {
            return ParseResult.Error("Unrecognized WeightFit format")
        }

        val isLbs = header.contains("lbs", ignoreCase = true)
        val entries = mutableListOf<WeightEntry>()
        var duplicates = 0

        for (i in 1 until rows.size) {
            val row = rows[i]
            if (row.size < 2) continue
            val date = runCatching { LocalDate.parse(row[0].trim()) }.getOrNull() ?: continue
            val raw = row[1].trim().toDoubleOrNull() ?: continue
            val weightKg = if (isLbs) WeightConverter.lbsToKg(raw) else raw
            if (!weightKg.isValidWeightKg()) continue
            val entry = makeEntry(date, weightKg)
            if (isDuplicate(entry, existingEntries)) duplicates++ else entries.add(entry)
        }

        return ParseResult.Success(entries, duplicates)
    }
}

// ── Happy Scale ────────────────────────────────────────────────────────────────

object HappyScaleParser {

    /**
     * Parses a Happy Scale CSV export.
     *
     * Distinctive format: quoted headers containing "Date" and "Pound(s)".
     * Weights are always in pounds; output is converted to kg.
     */
    fun parse(csv: String, existingEntries: List<WeightEntry>): ParseResult {
        if (csv.isBlank()) return ParseResult.Success(emptyList(), 0)

        val rows = readAllRows(csv) ?: return ParseResult.Error("CSV could not be parsed")
        if (rows.isEmpty()) return ParseResult.Success(emptyList(), 0)

        val headerRow = rows[0].joinToString(",").lowercase()
        if (!headerRow.contains("date") || !headerRow.contains("pound")) {
            return ParseResult.Error("Unrecognized Happy Scale format")
        }

        val entries = mutableListOf<WeightEntry>()
        var duplicates = 0

        for (i in 1 until rows.size) {
            val row = rows[i]
            if (row.size < 2) continue
            val date = runCatching { LocalDate.parse(row[0].trim()) }.getOrNull() ?: continue
            val lbs = row[1].trim().toDoubleOrNull() ?: continue
            val weightKg = WeightConverter.lbsToKg(lbs)
            if (!weightKg.isValidWeightKg()) continue
            val entry = makeEntry(date, weightKg)
            if (isDuplicate(entry, existingEntries)) duplicates++ else entries.add(entry)
        }

        return ParseResult.Success(entries, duplicates)
    }
}

// ── Apple Health ───────────────────────────────────────────────────────────────

object AppleHealthParser {

    /**
     * Parses an Apple Health CSV export (body mass records).
     *
     * Distinctive fields: `type` = `HKQuantityTypeIdentifierBodyMass`, `startDate`, `value`.
     * Unit is read from the `unit` column; defaults to kg when absent.
     */
    fun parse(csv: String, existingEntries: List<WeightEntry>): ParseResult {
        if (csv.isBlank()) return ParseResult.Success(emptyList(), 0)

        val rows = readAllRows(csv) ?: return ParseResult.Error("CSV could not be parsed")
        if (rows.isEmpty()) return ParseResult.Success(emptyList(), 0)

        // Build header→index map from first row (trim + lowercase for robustness)
        val headerMap: Map<String, Int> = rows[0]
            .mapIndexed { idx, col -> col.trim() to idx }
            .toMap()

        val typeIdx = headerMap["type"] ?: -1
        val unitIdx = headerMap["unit"] ?: -1
        val startDateIdx = headerMap["startDate"] ?: -1
        val valueIdx = headerMap["value"] ?: -1

        if (typeIdx < 0 || startDateIdx < 0 || valueIdx < 0) {
            return ParseResult.Error("Unrecognized Apple Health format")
        }

        val entries = mutableListOf<WeightEntry>()
        var duplicates = 0

        for (i in 1 until rows.size) {
            val row = rows[i]
            val maxIdx = maxOf(typeIdx, startDateIdx, valueIdx, if (unitIdx >= 0) unitIdx else 0)
            if (row.size <= maxIdx) continue
            if (row[typeIdx].trim() != "HKQuantityTypeIdentifierBodyMass") continue

            val dateStr = row[startDateIdx].trim().take(10)
            val date = runCatching { LocalDate.parse(dateStr) }.getOrNull() ?: continue
            val raw = row[valueIdx].trim().toDoubleOrNull() ?: continue
            val unit = if (unitIdx >= 0 && unitIdx < row.size) row[unitIdx].trim() else "kg"
            val weightKg = if (unit.lowercase() in listOf("lbs", "lb")) WeightConverter.lbsToKg(raw) else raw
            if (!weightKg.isValidWeightKg()) continue
            val entry = makeEntry(date, weightKg)
            if (isDuplicate(entry, existingEntries)) duplicates++ else entries.add(entry)
        }

        return ParseResult.Success(entries, duplicates)
    }
}

// ── Generic CSV ────────────────────────────────────────────────────────────────

object GenericCsvParser {

    /**
     * Parses a generic CSV export using header-name detection.
     *
     * Requires a column whose header contains "date" and a column named exactly
     * `weight_kg` or `weight_lbs`. Index-based parsing is intentionally avoided;
     * all column lookups use the header map produced from row 0.
     */
    fun parse(csv: String, existingEntries: List<WeightEntry>): ParseResult {
        if (csv.isBlank()) return ParseResult.Success(emptyList(), 0)

        val rows = readAllRows(csv) ?: return ParseResult.Error("CSV could not be parsed")
        if (rows.isEmpty()) return ParseResult.Success(emptyList(), 0)

        // Build a lowercase header→index map
        val headerMap: Map<String, Int> = rows[0]
            .mapIndexed { idx, col -> col.trim().lowercase() to idx }
            .toMap()

        val dateIdx = headerMap.entries.firstOrNull { it.key.contains("date") }?.value ?: -1
        val weightKgIdx = headerMap["weight_kg"] ?: -1
        val weightLbsIdx = headerMap["weight_lbs"] ?: -1
        val weightIdx = if (weightKgIdx >= 0) weightKgIdx else weightLbsIdx
        val isLbs = weightLbsIdx >= 0 && weightKgIdx < 0

        if (dateIdx < 0 || weightIdx < 0) {
            return ParseResult.Error("No recognizable date/weight headers found")
        }

        val entries = mutableListOf<WeightEntry>()
        var duplicates = 0

        for (i in 1 until rows.size) {
            val row = rows[i]
            if (row.size <= maxOf(dateIdx, weightIdx)) continue
            val date = runCatching { LocalDate.parse(row[dateIdx].trim()) }.getOrNull() ?: continue
            val raw = row[weightIdx].trim().toDoubleOrNull() ?: continue
            val weightKg = if (isLbs) WeightConverter.lbsToKg(raw) else raw
            if (!weightKg.isValidWeightKg()) continue
            val entry = makeEntry(date, weightKg)
            if (isDuplicate(entry, existingEntries)) duplicates++ else entries.add(entry)
        }

        return ParseResult.Success(entries, duplicates)
    }
}
