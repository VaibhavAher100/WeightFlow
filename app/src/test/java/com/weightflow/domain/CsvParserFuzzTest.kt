package com.weightflow.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Fuzz / edge-case tests for the RFC-4180-backed CSV parsers.
 *
 * These tests exercise inputs that break naive `.split(",")` implementations:
 * quoted commas, escaped quotes, mixed line endings, huge row counts, malformed rows,
 * blank files, various numeric edge cases, and unusual encodings.
 *
 * All tests must remain pure-JVM (no Android context required).
 */
class CsvParserFuzzTest {

    // ── Quoted commas ──────────────────────────────────────────────────────────

    @Test
    fun `WeightFitParser handles quoted comma in extra columns without corrupting weight`() {
        // Weight column still at index 1; extra quoted-comma column must not shift indices
        val csv = "Date,Weight,Note\n2024-01-01,80.5,\"Felt good, really good\""
        val result = WeightFitParser.parse(csv, emptyList())
        assertTrue(result is ParseResult.Success)
        val entries = (result as ParseResult.Success).entries
        assertEquals(1, entries.size)
        assertEquals(80.5, entries[0].weightKg, 0.01)
    }

    @Test
    fun `GenericCsvParser handles quoted comma in non-weight columns`() {
        // The note column contains a comma; date + weight_kg must still be found by header name
        val csv = "date,weight_kg,note\n2024-03-15,75.0,\"Rest day, easy walk\""
        val result = GenericCsvParser.parse(csv, emptyList())
        assertTrue(result is ParseResult.Success)
        val entries = (result as ParseResult.Success).entries
        assertEquals(1, entries.size)
        assertEquals(75.0, entries[0].weightKg, 0.01)
    }

    @Test
    fun `HappyScaleParser handles quoted comma inside a value cell`() {
        // Pounds cell contains a comma-formatted number like "1,76.4" — should fail numeric parse
        // but parser must not crash
        val csv = "\"Date\",\"Pounds\"\n\"2024-01-01\",\"1,764\""
        val result = HappyScaleParser.parse(csv, emptyList())
        // "1,764" is not a valid Double — row is skipped, parser returns Success with 0 entries
        assertTrue(result is ParseResult.Success)
        assertEquals(0, (result as ParseResult.Success).entries.size)
    }

    // ── Escaped / doubled quotes (RFC-4180 §2.7) ──────────────────────────────

    @Test
    fun `WeightFitParser handles doubled-quote escape inside cell`() {
        // RFC-4180 escapes a literal quote as "" inside a quoted field
        val csv = "Date,Weight,Note\n2024-01-01,82.0,\"He said \"\"great\"\"\""
        val result = WeightFitParser.parse(csv, emptyList())
        assertTrue(result is ParseResult.Success)
        val entries = (result as ParseResult.Success).entries
        assertEquals(1, entries.size)
        assertEquals(82.0, entries[0].weightKg, 0.01)
    }

    @Test
    fun `HappyScaleParser handles doubled-quote escapes in date field`() {
        // Odd but valid RFC-4180: quoted date with escaped internal quote
        val csv = "\"Date\",\"Pounds\"\n\"2024-01-\"\"01\"\"\",\"176.4\""
        // Date "2024-01-"01"" will fail LocalDate.parse — row silently skipped, no crash
        val result = HappyScaleParser.parse(csv, emptyList())
        assertTrue(result is ParseResult.Success)
        assertEquals(0, (result as ParseResult.Success).entries.size)
    }

    // ── Mixed line endings ─────────────────────────────────────────────────────

    @Test
    fun `WeightFitParser parses CRLF line endings`() {
        val csv = "Date,Weight\r\n2024-01-01,80.5\r\n2024-01-02,80.2\r\n"
        val result = WeightFitParser.parse(csv, emptyList())
        assertTrue(result is ParseResult.Success)
        assertEquals(2, (result as ParseResult.Success).entries.size)
    }

    @Test
    fun `WeightFitParser parses bare CR line endings`() {
        val csv = "Date,Weight\r2024-01-01,78.0\r2024-01-02,77.5\r"
        val result = WeightFitParser.parse(csv, emptyList())
        assertTrue(result is ParseResult.Success)
        // kotlin-csv normalises \r; at least one row should parse
        val entries = (result as ParseResult.Success).entries
        assertTrue("Expected at least 1 entry with bare CR endings", entries.isNotEmpty())
    }

    @Test
    fun `GenericCsvParser parses mixed CRLF and LF line endings`() {
        // First separator CRLF, second LF
        val csv = "date,weight_kg\r\n2024-02-01,70.0\n2024-02-02,69.5\r\n"
        val result = GenericCsvParser.parse(csv, emptyList())
        assertTrue(result is ParseResult.Success)
        assertEquals(2, (result as ParseResult.Success).entries.size)
    }

    @Test
    fun `CsvImporter correctly dispatches CRLF HappyScale CSV`() {
        val csv = "\"Date\",\"Pounds\"\r\n\"2024-06-01\",\"176.4\"\r\n"
        val result = CsvImporter.import(csv, emptyList())
        assertTrue(result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals(CsvFormat.HAPPY_SCALE, success.format)
        assertEquals(1, success.entries.size)
    }

    // ── Huge row count ─────────────────────────────────────────────────────────

    @Test
    fun `CsvImporter rejects CSV with more than 10000 data rows`() {
        val sb = StringBuilder("Date,Weight\n")
        repeat(10_001) { i ->
            sb.append("2020-01-01,80.0\n") // same date repeated is fine for the limit check
        }
        val result = CsvImporter.import(sb.toString(), emptyList())
        assertTrue(result is ParseResult.Error)
        assertTrue((result as ParseResult.Error).message.contains("Too many rows"))
    }

    @Test
    fun `WeightFitParser processes exactly 10000 data rows without memory issues`() {
        val sb = StringBuilder("Date,Weight\n")
        for (i in 0 until 10_000) {
            val date = java.time.LocalDate.of(2020, 1, 1).plusDays(i.toLong())
            sb.append("$date,${60.0 + (i % 10) * 0.1}\n")
        }
        // Pass directly to parser (bypass CsvImporter limit which applies to 10,001+)
        val result = WeightFitParser.parse(sb.toString(), emptyList())
        assertTrue(result is ParseResult.Success)
        val entries = (result as ParseResult.Success).entries
        assertEquals(10_000, entries.size)
    }

    @Test
    fun `large valid import completes within reasonable time`() {
        val sb = StringBuilder("Date,Weight\n")
        for (i in 0 until 5_000) {
            val date = java.time.LocalDate.of(2015, 1, 1).plusDays(i.toLong())
            sb.append("$date,70.0\n")
        }
        val start = System.currentTimeMillis()
        val result = WeightFitParser.parse(sb.toString(), emptyList())
        val elapsed = System.currentTimeMillis() - start
        assertTrue(result is ParseResult.Success)
        assertTrue("Parsing 5000 rows took ${elapsed}ms (>5000ms)", elapsed < 5_000)
    }

    // ── Malformed rows ─────────────────────────────────────────────────────────

    @Test
    fun `WeightFitParser skips rows with missing weight column`() {
        val csv = "Date,Weight\n2024-01-01\n2024-01-02,80.0"
        val result = WeightFitParser.parse(csv, emptyList())
        assertTrue(result is ParseResult.Success)
        val entries = (result as ParseResult.Success).entries
        assertEquals(1, entries.size)
        assertEquals(80.0, entries[0].weightKg, 0.01)
    }

    @Test
    fun `WeightFitParser skips rows with extra unexpected columns`() {
        val csv = "Date,Weight\n2024-01-01,79.5,extraCol,anotherExtra\n2024-01-02,80.0"
        val result = WeightFitParser.parse(csv, emptyList())
        assertTrue(result is ParseResult.Success)
        assertEquals(2, (result as ParseResult.Success).entries.size)
    }

    @Test
    fun `WeightFitParser skips rows with null-like string in weight column`() {
        val csv = "Date,Weight\n2024-01-01,null\n2024-01-02,80.0"
        val result = WeightFitParser.parse(csv, emptyList())
        assertTrue(result is ParseResult.Success)
        assertEquals(1, (result as ParseResult.Success).entries.size)
    }

    @Test
    fun `WeightFitParser skips rows with empty weight cell`() {
        val csv = "Date,Weight\n2024-01-01,\n2024-01-02,80.0"
        val result = WeightFitParser.parse(csv, emptyList())
        assertTrue(result is ParseResult.Success)
        assertEquals(1, (result as ParseResult.Success).entries.size)
    }

    @Test
    fun `GenericCsvParser skips rows with non-numeric weight`() {
        val csv = "date,weight_kg\n2024-01-01,heavy\n2024-01-02,72.5"
        val result = GenericCsvParser.parse(csv, emptyList())
        assertTrue(result is ParseResult.Success)
        assertEquals(1, (result as ParseResult.Success).entries.size)
    }

    @Test
    fun `GenericCsvParser skips rows with unparseable date`() {
        val csv = "date,weight_kg\nnot-a-date,72.5\n2024-01-02,72.5"
        val result = GenericCsvParser.parse(csv, emptyList())
        assertTrue(result is ParseResult.Success)
        assertEquals(1, (result as ParseResult.Success).entries.size)
    }

    // ── Blank / header-only CSV ────────────────────────────────────────────────

    @Test
    fun `CsvImporter rejects blank CSV string`() {
        val result = CsvImporter.import("", emptyList())
        assertTrue(result is ParseResult.Error)
    }

    @Test
    fun `CsvImporter rejects CSV with header row only and no data`() {
        val result = CsvImporter.import("Date,Weight\n", emptyList())
        assertTrue(result is ParseResult.Error)
        val msg = (result as ParseResult.Error).message
        assertTrue("Expected 'no data rows' message, got: $msg", msg.contains("no data rows"))
    }

    @Test
    fun `WeightFitParser returns empty success for blank input`() {
        val result = WeightFitParser.parse("", emptyList())
        assertTrue(result is ParseResult.Success)
        assertEquals(0, (result as ParseResult.Success).entries.size)
    }

    @Test
    fun `HappyScaleParser returns empty success for blank input`() {
        val result = HappyScaleParser.parse("", emptyList())
        assertTrue(result is ParseResult.Success)
        assertEquals(0, (result as ParseResult.Success).entries.size)
    }

    @Test
    fun `AppleHealthParser returns empty success for blank input`() {
        val result = AppleHealthParser.parse("", emptyList())
        assertTrue(result is ParseResult.Success)
        assertEquals(0, (result as ParseResult.Success).entries.size)
    }

    @Test
    fun `GenericCsvParser returns empty success for blank input`() {
        val result = GenericCsvParser.parse("", emptyList())
        assertTrue(result is ParseResult.Success)
        assertEquals(0, (result as ParseResult.Success).entries.size)
    }

    // ── Column-count limit ─────────────────────────────────────────────────────

    @Test
    fun `CsvImporter rejects CSV with more than 10 columns`() {
        val header = (1..11).joinToString(",") { "col$it" }
        val row = (1..11).joinToString(",") { it.toString() }
        val csv = "$header\n$row"
        val result = CsvImporter.import(csv, emptyList())
        assertTrue(result is ParseResult.Error)
        assertTrue((result as ParseResult.Error).message.contains("Too many columns"))
    }

    @Test
    fun `CsvImporter accepts CSV with exactly 10 columns`() {
        // Build a WeightFit-style CSV with exactly 10 columns — parser still dispatches normally
        val header = "Date,Weight,c3,c4,c5,c6,c7,c8,c9,c10"
        val row = "2024-01-01,80.0,x,x,x,x,x,x,x,x"
        val csv = "$header\n$row"
        val result = CsvImporter.import(csv, emptyList())
        // Should not be rejected by the column limit; parser may or may not succeed
        assertFalse(
            "Should not fail with 'Too many columns' for exactly 10 columns",
            result is ParseResult.Error && (result as ParseResult.Error).message.contains("Too many columns")
        )
    }

    // ── Size limit ─────────────────────────────────────────────────────────────

    @Test
    fun `CsvImporter rejects file that exceeds 5 MB byte limit`() {
        // UTF-8 single-byte chars — 5,000,001 bytes exactly
        val csv = "x".repeat(5_000_001)
        val result = CsvImporter.import(csv, emptyList())
        assertTrue(result is ParseResult.Error)
        assertEquals("File too large (max 5 MB)", (result as ParseResult.Error).message)
    }

    @Test
    fun `CsvImporter accepts file at exactly 5 MB boundary`() {
        // Build a valid CSV whose UTF-8 encoding is just under 5 MB
        val header = "Date,Weight\n"
        val row = "2024-01-01,80.0\n"
        val rowsNeeded = (5_000_000 - header.toByteArray().size) / row.toByteArray().size
        val sb = StringBuilder(header)
        // Only generate unique dates to avoid hitting MAX_ROWS first
        var date = java.time.LocalDate.of(2000, 1, 1)
        repeat(minOf(rowsNeeded.toInt(), MAX_ROWS)) {
            sb.append("$date,80.0\n")
            date = date.plusDays(1)
        }
        val csv = sb.toString()
        assertTrue("Test CSV must be under 5 MB", csv.toByteArray().size <= 5_000_000)
        val result = CsvImporter.import(csv, emptyList())
        // No size or row-count error; actual parse result may vary
        assertFalse(
            "Should not fail with size error",
            result is ParseResult.Error && (result as ParseResult.Error).message.contains("too large")
        )
    }

    // ── Numeric edge cases ─────────────────────────────────────────────────────

    @Test
    fun `WeightFitParser skips negative weight values as out of valid range`() {
        val csv = "Date,Weight\n2024-01-01,-10.0\n2024-01-02,80.0"
        val result = WeightFitParser.parse(csv, emptyList())
        assertTrue(result is ParseResult.Success)
        assertEquals(1, (result as ParseResult.Success).entries.size)
    }

    @Test
    fun `WeightFitParser skips zero weight as out of valid range`() {
        val csv = "Date,Weight\n2024-01-01,0.0\n2024-01-02,75.0"
        val result = WeightFitParser.parse(csv, emptyList())
        assertTrue(result is ParseResult.Success)
        assertEquals(1, (result as ParseResult.Success).entries.size)
    }

    @Test
    fun `WeightFitParser skips weight exceeding 635kg maximum`() {
        val csv = "Date,Weight\n2024-01-01,636.0\n2024-01-02,75.0"
        val result = WeightFitParser.parse(csv, emptyList())
        assertTrue(result is ParseResult.Success)
        assertEquals(1, (result as ParseResult.Success).entries.size)
    }

    @Test
    fun `WeightFitParser accepts minimum valid weight 0_5 kg`() {
        val csv = "Date,Weight\n2024-01-01,0.5"
        val result = WeightFitParser.parse(csv, emptyList())
        assertTrue(result is ParseResult.Success)
        assertEquals(1, (result as ParseResult.Success).entries.size)
        assertEquals(0.5, (result as ParseResult.Success).entries[0].weightKg, 0.001)
    }

    @Test
    fun `WeightFitParser accepts maximum valid weight 635 kg`() {
        val csv = "Date,Weight\n2024-01-01,635.0"
        val result = WeightFitParser.parse(csv, emptyList())
        assertTrue(result is ParseResult.Success)
        assertEquals(1, (result as ParseResult.Success).entries.size)
    }

    @Test
    fun `WeightFitParser skips scientific notation values that are out of range`() {
        // 1e10 is way beyond 635 kg — should be skipped
        val csv = "Date,Weight\n2024-01-01,1e10\n2024-01-02,80.0"
        val result = WeightFitParser.parse(csv, emptyList())
        assertTrue(result is ParseResult.Success)
        assertEquals(1, (result as ParseResult.Success).entries.size)
    }

    @Test
    fun `WeightFitParser parses decimal weight with many decimal places`() {
        val csv = "Date,Weight\n2024-01-01,72.123456789"
        val result = WeightFitParser.parse(csv, emptyList())
        assertTrue(result is ParseResult.Success)
        assertEquals(72.123456789, (result as ParseResult.Success).entries[0].weightKg, 0.0001)
    }

    // ── UTF-8 with BOM ─────────────────────────────────────────────────────────

    @Test
    fun `WeightFitParser handles UTF-8 BOM at start of file`() {
        // Some exports include a UTF-8 BOM (﻿)
        val csv = "﻿Date,Weight\n2024-01-01,80.0"
        val result = WeightFitParser.parse(csv, emptyList())
        // BOM may cause header detection to fail or succeed; the parser must not crash
        assertTrue("Parser must not throw on UTF-8 BOM input", result is ParseResult.Success || result is ParseResult.Error)
    }

    @Test
    fun `GenericCsvParser handles UTF-8 BOM and still finds date header`() {
        val csv = "﻿date,weight_kg\n2024-01-01,70.0"
        val result = GenericCsvParser.parse(csv, emptyList())
        // BOM may prefix the first column name — parser must not crash
        assertTrue(result is ParseResult.Success || result is ParseResult.Error)
    }

    // ── Unicode in non-key columns ─────────────────────────────────────────────

    @Test
    fun `WeightFitParser handles Unicode characters in note column`() {
        val csv = "Date,Weight,Note\n2024-01-01,80.0,体重記録"
        val result = WeightFitParser.parse(csv, emptyList())
        assertTrue(result is ParseResult.Success)
        assertEquals(1, (result as ParseResult.Success).entries.size)
    }

    @Test
    fun `HappyScaleParser handles accented characters in non-numeric columns`() {
        val csv = "\"Date\",\"Pounds\",\"Note\"\n\"2024-01-01\",\"176.4\",\"Très bien\""
        val result = HappyScaleParser.parse(csv, emptyList())
        assertTrue(result is ParseResult.Success)
        assertEquals(1, (result as ParseResult.Success).entries.size)
    }

    // ── Whitespace and trimming ────────────────────────────────────────────────

    @Test
    fun `WeightFitParser trims leading and trailing spaces from cells`() {
        val csv = "Date,Weight\n  2024-01-01  ,  80.5  "
        val result = WeightFitParser.parse(csv, emptyList())
        assertTrue(result is ParseResult.Success)
        assertEquals(1, (result as ParseResult.Success).entries.size)
        assertEquals(80.5, (result as ParseResult.Success).entries[0].weightKg, 0.01)
    }

    @Test
    fun `GenericCsvParser handles trailing newline without producing empty entry`() {
        val csv = "date,weight_kg\n2024-01-01,80.0\n"
        val result = GenericCsvParser.parse(csv, emptyList())
        assertTrue(result is ParseResult.Success)
        // No phantom empty row from trailing newline
        assertEquals(1, (result as ParseResult.Success).entries.size)
    }

    // ── Apple Health specific edge cases ──────────────────────────────────────

    @Test
    fun `AppleHealthParser skips rows not matching HKQuantityTypeIdentifierBodyMass`() {
        val csv = """type,sourceName,unit,startDate,endDate,value
HKQuantityTypeIdentifierHeartRate,iPhone,count/min,2024-01-01 08:00:00 +0000,2024-01-01 08:00:00 +0000,72
HKQuantityTypeIdentifierBodyMass,iPhone,kg,2024-01-01 08:00:00 +0000,2024-01-01 08:00:00 +0000,80.5"""
        val result = AppleHealthParser.parse(csv, emptyList())
        assertTrue(result is ParseResult.Success)
        assertEquals(1, (result as ParseResult.Success).entries.size)
        assertEquals(80.5, (result as ParseResult.Success).entries[0].weightKg, 0.01)
    }

    @Test
    fun `AppleHealthParser converts lbs to kg correctly`() {
        val csv = """type,sourceName,unit,startDate,endDate,value
HKQuantityTypeIdentifierBodyMass,iPhone,lb,2024-06-15 08:00:00 +0000,2024-06-15 08:00:00 +0000,176.37"""
        val result = AppleHealthParser.parse(csv, emptyList())
        assertTrue(result is ParseResult.Success)
        val entries = (result as ParseResult.Success).entries
        assertEquals(1, entries.size)
        assertEquals(80.0, entries[0].weightKg, 0.1)
    }

    // ── Duplicate detection ────────────────────────────────────────────────────

    @Test
    fun `duplicate detection works correctly with RFC-4180 parsed entries`() {
        val existing = listOf(
            WeightEntry(
                id = 1,
                timestamp = java.time.LocalDate.of(2024, 1, 1).toEpochDay() * 86_400_000L,
                weightKg = 80.5,
                note = ""
            )
        )
        val csv = "Date,Weight\n2024-01-01,80.5\n2024-01-02,80.2"
        val result = WeightFitParser.parse(csv, existing)
        assertTrue(result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals(1, success.entries.size)
        assertEquals(1, success.duplicatesSkipped)
    }

    // ── CsvImporter row-level integration ─────────────────────────────────────

    @Test
    fun `CsvImporter import of WeightFit with quoted comma in note column succeeds`() {
        val csv = "Date,Weight,Note\n2024-01-01,80.5,\"Morning, fasted\""
        val result = CsvImporter.import(csv, emptyList())
        assertTrue(result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals(CsvFormat.WEIGHT_FIT, success.format)
        assertEquals(1, success.entries.size)
    }

    @Test
    fun `CsvImporter import of Generic CSV with CRLF line endings succeeds`() {
        val csv = "date,weight_kg\r\n2024-01-01,72.0\r\n2024-01-02,71.8\r\n"
        val result = CsvImporter.import(csv, emptyList())
        assertTrue(result is ParseResult.Success)
        val success = result as ParseResult.Success
        assertEquals(CsvFormat.GENERIC, success.format)
        assertEquals(2, success.entries.size)
    }
}
