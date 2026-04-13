package com.weightflow.domain

import java.time.LocalDate
import kotlin.math.abs

private fun isDuplicate(entry: WeightEntry, existing: List<WeightEntry>): Boolean {
    val entryDate = LocalDate.ofEpochDay(entry.timestamp / 86_400_000L)
    return existing.any { ex ->
        val exDate = LocalDate.ofEpochDay(ex.timestamp / 86_400_000L)
        exDate == entryDate && abs(ex.weightKg - entry.weightKg) < 0.01
    }
}

private fun makeEntry(date: LocalDate, weightKg: Double) = WeightEntry(
    id = 0,
    timestamp = date.toEpochDay() * 86_400_000L,
    weightKg = weightKg,
    note = ""
)

// ── WeightFit ─────────────────────────────────────────────────────────────────

object WeightFitParser {

    fun parse(csv: String, existingEntries: List<WeightEntry>): ParseResult {
        if (csv.isBlank()) return ParseResult.Success(emptyList(), 0)

        val lines = csv.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return ParseResult.Success(emptyList(), 0)

        val header = lines[0]
        if (!header.contains("date", ignoreCase = true) || !header.contains("weight", ignoreCase = true)) {
            return ParseResult.Error("Unrecognized WeightFit format")
        }

        val isLbs = header.contains("lbs", ignoreCase = true)
        val entries = mutableListOf<WeightEntry>()
        var duplicates = 0

        for (i in 1 until lines.size) {
            val parts = lines[i].split(",").map { it.trim().removeSurrounding("\"") }
            if (parts.size < 2) continue
            val date = runCatching { LocalDate.parse(parts[0]) }.getOrNull() ?: continue
            val raw = parts[1].toDoubleOrNull() ?: continue
            val weightKg = if (isLbs) WeightConverter.lbsToKg(raw) else raw
            val entry = makeEntry(date, weightKg)
            if (isDuplicate(entry, existingEntries)) duplicates++ else entries.add(entry)
        }

        return ParseResult.Success(entries, duplicates)
    }
}

// ── Happy Scale ───────────────────────────────────────────────────────────────

object HappyScaleParser {

    fun parse(csv: String, existingEntries: List<WeightEntry>): ParseResult {
        if (csv.isBlank()) return ParseResult.Success(emptyList(), 0)

        val lines = csv.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return ParseResult.Success(emptyList(), 0)

        val header = lines[0].replace("\"", "").lowercase()
        if (!header.contains("date") || !header.contains("pound")) {
            return ParseResult.Error("Unrecognized Happy Scale format")
        }

        val entries = mutableListOf<WeightEntry>()
        var duplicates = 0

        for (i in 1 until lines.size) {
            val parts = lines[i].split(",").map { it.trim().removeSurrounding("\"") }
            if (parts.size < 2) continue
            val date = runCatching { LocalDate.parse(parts[0]) }.getOrNull() ?: continue
            val lbs = parts[1].toDoubleOrNull() ?: continue
            val entry = makeEntry(date, WeightConverter.lbsToKg(lbs))
            if (isDuplicate(entry, existingEntries)) duplicates++ else entries.add(entry)
        }

        return ParseResult.Success(entries, duplicates)
    }
}

// ── Apple Health ──────────────────────────────────────────────────────────────

object AppleHealthParser {

    fun parse(csv: String, existingEntries: List<WeightEntry>): ParseResult {
        if (csv.isBlank()) return ParseResult.Success(emptyList(), 0)

        val lines = csv.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return ParseResult.Success(emptyList(), 0)

        val headers = lines[0].split(",").map { it.trim() }
        val typeIdx = headers.indexOf("type")
        val unitIdx = headers.indexOf("unit")
        val startDateIdx = headers.indexOf("startDate")
        val valueIdx = headers.indexOf("value")

        if (typeIdx < 0 || startDateIdx < 0 || valueIdx < 0) {
            return ParseResult.Error("Unrecognized Apple Health format")
        }

        val entries = mutableListOf<WeightEntry>()
        var duplicates = 0

        for (i in 1 until lines.size) {
            val parts = lines[i].split(",").map { it.trim() }
            val maxIdx = maxOf(typeIdx, startDateIdx, valueIdx, if (unitIdx >= 0) unitIdx else 0)
            if (parts.size <= maxIdx) continue
            if (parts[typeIdx] != "HKQuantityTypeIdentifierBodyMass") continue

            val dateStr = parts[startDateIdx].take(10)
            val date = runCatching { LocalDate.parse(dateStr) }.getOrNull() ?: continue
            val raw = parts[valueIdx].toDoubleOrNull() ?: continue
            val unit = if (unitIdx >= 0 && unitIdx < parts.size) parts[unitIdx] else "kg"
            val weightKg = if (unit.lowercase() in listOf("lbs", "lb")) WeightConverter.lbsToKg(raw) else raw

            val entry = makeEntry(date, weightKg)
            if (isDuplicate(entry, existingEntries)) duplicates++ else entries.add(entry)
        }

        return ParseResult.Success(entries, duplicates)
    }
}

// ── Generic CSV ───────────────────────────────────────────────────────────────

object GenericCsvParser {

    fun parse(csv: String, existingEntries: List<WeightEntry>): ParseResult {
        if (csv.isBlank()) return ParseResult.Success(emptyList(), 0)

        val lines = csv.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return ParseResult.Success(emptyList(), 0)

        val headers = lines[0].split(",").map { it.trim().lowercase() }
        val dateIdx = headers.indexOfFirst { it.contains("date") }
        val weightKgIdx = headers.indexOf("weight_kg")
        val weightLbsIdx = headers.indexOf("weight_lbs")
        val weightIdx = if (weightKgIdx >= 0) weightKgIdx else weightLbsIdx
        val isLbs = weightLbsIdx >= 0 && weightKgIdx < 0

        if (dateIdx < 0 || weightIdx < 0) {
            return ParseResult.Error("No recognizable date/weight headers found")
        }

        val entries = mutableListOf<WeightEntry>()
        var duplicates = 0

        for (i in 1 until lines.size) {
            val parts = lines[i].split(",").map { it.trim() }
            if (parts.size <= maxOf(dateIdx, weightIdx)) continue
            val date = runCatching { LocalDate.parse(parts[dateIdx]) }.getOrNull() ?: continue
            val raw = parts[weightIdx].toDoubleOrNull() ?: continue
            val weightKg = if (isLbs) WeightConverter.lbsToKg(raw) else raw
            val entry = makeEntry(date, weightKg)
            if (isDuplicate(entry, existingEntries)) duplicates++ else entries.add(entry)
        }

        return ParseResult.Success(entries, duplicates)
    }
}
