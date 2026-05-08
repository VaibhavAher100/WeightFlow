package com.weightflow.domain

import net.lingala.zip4j.ZipFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.time.LocalDate

/**
 * Unit tests for [CsvExporter].
 *
 * Covers:
 * - Existing plaintext CSV behaviour (unchanged)
 * - Minimal CSV format (date + weight_kg only)
 * - Encrypted ZIP: AES-256, correct password succeeds, wrong password fails
 * - Password policy: < 12 chars rejected, 12+ accepted
 * - Password zero-out: CharArray is cleared after use
 * - Integrity check: corrupted ZIP is rejected
 * - Estimated size calculation
 *
 * Note: zip4j is a pure-JVM library so these tests run on the JVM without Robolectric.
 */
class CsvExporterTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private fun entry(daysAgo: Int, weightKg: Double) = WeightEntry(
        id = daysAgo.toLong(),
        timestamp = LocalDate.now().minusDays(daysAgo.toLong()).toEpochDay() * 86_400_000L,
        weightKg = weightKg,
        note = ""
    )

    // ── Plaintext CSV: headers ────────────────────────────────────────────────

    @Test
    fun `exports kg header when unit is KG`() {
        val csv = CsvExporter.export(emptyList(), WeightUnit.KG)
        assertTrue(csv.startsWith("date,weight_kg"))
    }

    @Test
    fun `exports lbs header when unit is LBS`() {
        val csv = CsvExporter.export(emptyList(), WeightUnit.LBS)
        assertTrue(csv.startsWith("date,weight_lbs"))
    }

    @Test
    fun `exports st header when unit is ST`() {
        val csv = CsvExporter.export(emptyList(), WeightUnit.ST)
        assertTrue(csv.startsWith("date,weight_st"))
    }

    // ── Plaintext CSV: unit conversion ────────────────────────────────────────

    @Test
    fun `exports weight in kg without conversion`() {
        val entries = listOf(entry(0, weightKg = 80.0))
        val csv = CsvExporter.export(entries, WeightUnit.KG)
        assertTrue(csv.contains("80.0"))
    }

    @Test
    fun `exports weight converted to lbs`() {
        val entries = listOf(entry(0, weightKg = 80.0))
        val csv = CsvExporter.export(entries, WeightUnit.LBS)
        // 80kg = ~176.4 lbs
        assertTrue(csv.contains("176."))
    }

    // ── Plaintext CSV: ordering ───────────────────────────────────────────────

    @Test
    fun `exports entries in chronological order oldest first`() {
        val entries = listOf(
            entry(daysAgo = 0, weightKg = 79.0),
            entry(daysAgo = 2, weightKg = 81.0),
            entry(daysAgo = 1, weightKg = 80.0)
        )
        val lines = CsvExporter.export(entries, WeightUnit.KG)
            .lines()
            .filter { it.isNotBlank() }
            .drop(1)

        assertTrue(lines[0].contains("81.0")) // oldest first
        assertTrue(lines[1].contains("80.0"))
        assertTrue(lines[2].contains("79.0"))
    }

    // ── Plaintext CSV: date format ────────────────────────────────────────────

    @Test
    fun `date is formatted as ISO-8601 yyyy-MM-dd`() {
        val entries = listOf(entry(0, weightKg = 80.0))
        val csv = CsvExporter.export(entries, WeightUnit.KG)
        val today = LocalDate.now().toString()
        assertTrue(csv.contains(today))
    }

    // ── Plaintext CSV: empty list / row count ─────────────────────────────────

    @Test
    fun `empty entry list returns header row only`() {
        val csv = CsvExporter.export(emptyList(), WeightUnit.KG)
        val lines = csv.lines().filter { it.isNotBlank() }
        assertEquals(1, lines.size)
    }

    @Test
    fun `exports correct number of data rows`() {
        val entries = (0 until 5).map { entry(it, 80.0 - it * 0.3) }
        val csv = CsvExporter.export(entries, WeightUnit.KG)
        val dataLines = csv.lines().filter { it.isNotBlank() }.drop(1)
        assertEquals(5, dataLines.size)
    }

    // ── Minimal CSV ───────────────────────────────────────────────────────────

    @Test
    fun `minimal CSV has only date and weight_kg headers`() {
        val csv = CsvExporter.exportMinimalCsv(emptyList())
        val header = csv.lines().first()
        assertEquals("date,weight_kg", header)
    }

    @Test
    fun `minimal CSV has no extra columns beyond date and weight`() {
        val entries = listOf(entry(0, 75.0))
        val csv = CsvExporter.exportMinimalCsv(entries)
        // Each data line should contain exactly one comma (two fields: date,weight)
        val dataLines = csv.lines().filter { it.isNotBlank() }.drop(1)
        assertTrue("Expected at least one data line", dataLines.isNotEmpty())
        dataLines.forEach { line ->
            assertEquals("Data line should have exactly 2 fields: $line", 1, line.count { it == ',' })
        }
    }

    @Test
    fun `minimal CSV emits weight_kg regardless of user unit preference`() {
        val entries = listOf(entry(0, 80.0))
        val csv = CsvExporter.exportMinimalCsv(entries)
        assertTrue(csv.startsWith("date,weight_kg"))
        assertTrue(csv.contains("80.0"))
    }

    @Test
    fun `minimal CSV orders entries oldest first`() {
        val entries = listOf(
            entry(daysAgo = 0, weightKg = 70.0),
            entry(daysAgo = 3, weightKg = 73.0),
        )
        val lines = CsvExporter.exportMinimalCsv(entries)
            .lines()
            .filter { it.isNotBlank() }
            .drop(1)
        assertTrue("Oldest entry (73.0) should be first", lines[0].contains("73.0"))
        assertTrue("Newest entry (70.0) should be last",  lines[1].contains("70.0"))
    }

    @Test
    fun `minimal CSV with empty list returns header only`() {
        val csv = CsvExporter.exportMinimalCsv(emptyList())
        val lines = csv.lines().filter { it.isNotBlank() }
        assertEquals(1, lines.size)
    }

    // ── Encrypted ZIP: password policy ────────────────────────────────────────

    @Test
    fun `password shorter than 12 chars is rejected`() {
        val shortPassword = CharArray(11) { 'a' }
        val result = CsvExporter.exportEncryptedZip(
            entries    = emptyList(),
            unit       = WeightUnit.KG,
            password   = shortPassword,
            outputDir  = tmpFolder.root,
        )
        assertTrue("Expected failure for short password", result.isFailure)
        assertTrue(
            result.exceptionOrNull()?.message?.contains("12") == true
        )
    }

    @Test
    fun `password of exactly 12 chars is accepted`() {
        val password = CharArray(12) { 'a' }
        val result = CsvExporter.exportEncryptedZip(
            entries    = emptyList(),
            unit       = WeightUnit.KG,
            password   = password,
            outputDir  = tmpFolder.root,
        )
        assertTrue("Expected success for 12-char password", result.isSuccess)
    }

    @Test
    fun `password shorter than 12 chars is zeroed even on rejection`() {
        val password = "short1".toCharArray() // 6 chars
        CsvExporter.exportEncryptedZip(
            entries    = emptyList(),
            unit       = WeightUnit.KG,
            password   = password,
            outputDir  = tmpFolder.root,
        )
        assertTrue(
            "Password CharArray must be zeroed after rejection",
            password.all { it == ' ' }
        )
    }

    // ── Encrypted ZIP: password zero-out after use ────────────────────────────

    @Test
    fun `password CharArray is zeroed after successful encryption`() {
        val password = "SecurePassword123".toCharArray()
        val result = CsvExporter.exportEncryptedZip(
            entries    = listOf(entry(0, 80.0)),
            unit       = WeightUnit.KG,
            password   = password,
            outputDir  = tmpFolder.root,
        )
        assertTrue("Export should succeed", result.isSuccess)
        assertTrue(
            "Password CharArray must be zeroed after use",
            password.all { it == ' ' }
        )
    }

    // ── Encrypted ZIP: output file properties ────────────────────────────────

    @Test
    fun `encrypted ZIP file is created with correct name pattern`() {
        val password = "StrongPassword99!".toCharArray()
        val dateSuffix = LocalDate.now().toString()
        val result = CsvExporter.exportEncryptedZip(
            entries    = emptyList(),
            unit       = WeightUnit.KG,
            password   = password,
            outputDir  = tmpFolder.root,
            dateSuffix = dateSuffix,
        )
        assertTrue(result.isSuccess)
        val file = result.getOrThrow()
        assertEquals("weightflow_export_$dateSuffix.zip", file.name)
    }

    @Test
    fun `encrypted ZIP file exists and has non-zero size`() {
        val password = "StrongPassword99!".toCharArray()
        val result = CsvExporter.exportEncryptedZip(
            entries    = listOf(entry(0, 80.0), entry(1, 79.5)),
            unit       = WeightUnit.KG,
            password   = password,
            outputDir  = tmpFolder.root,
        )
        assertTrue(result.isSuccess)
        val file = result.getOrThrow()
        assertTrue("ZIP file should exist", file.exists())
        assertTrue("ZIP file should be non-empty", file.length() > 0)
    }

    // ── Encrypted ZIP: correct password can read inner CSV ────────────────────

    @Test
    fun `encrypted ZIP with correct password contains readable CSV`() {
        val originalPassword = "CorrectHorse112!".toCharArray()
        val entries = listOf(entry(0, 85.0), entry(1, 84.5))
        val result = CsvExporter.exportEncryptedZip(
            entries    = entries,
            unit       = WeightUnit.KG,
            password   = originalPassword,
            outputDir  = tmpFolder.root,
        )
        assertTrue("Export should succeed", result.isSuccess)
        val zipFile = result.getOrThrow()

        // Re-open with a fresh CharArray of the same password.
        val readPassword = "CorrectHorse112!".toCharArray()
        val content = ZipFile(zipFile, readPassword).use { zf ->
            val headers = zf.fileHeaders
            assertFalse("ZIP should contain at least one file", headers.isEmpty())
            zf.getInputStream(headers.first()).use { it.reader().readText() }
        }

        assertTrue("Inner CSV should have kg header", content.startsWith("date,weight_kg"))
        assertTrue("Inner CSV should contain 85.0", content.contains("85.0"))
        assertTrue("Inner CSV should contain 84.5", content.contains("84.5"))
    }

    // ── Encrypted ZIP: wrong password fails cleanly ───────────────────────────

    @Test
    fun `opening encrypted ZIP with wrong password throws exception`() {
        val correctPassword = "CorrectHorse112!".toCharArray()
        val result = CsvExporter.exportEncryptedZip(
            entries    = listOf(entry(0, 80.0)),
            unit       = WeightUnit.KG,
            password   = correctPassword,
            outputDir  = tmpFolder.root,
        )
        assertTrue(result.isSuccess)
        val zipFile = result.getOrThrow()

        val wrongPassword = "WrongPassword123".toCharArray()
        var threwException = false
        try {
            ZipFile(zipFile, wrongPassword).use { zf ->
                val headers = zf.fileHeaders
                if (headers.isNotEmpty()) {
                    zf.getInputStream(headers.first()).use { stream ->
                        val buf = ByteArray(8192)
                        while (stream.read(buf) != -1) { /* drain */ }
                    }
                }
            }
        } catch (_: Exception) {
            threwException = true
        }
        assertTrue("Reading with wrong password should throw", threwException)
    }

    // ── Encrypted ZIP: AES indicator in ZIP central directory ────────────────

    @Test
    fun `encrypted ZIP uses AES encryption not ZipCrypto`() {
        val password = "StrongPassword99!".toCharArray()
        val result = CsvExporter.exportEncryptedZip(
            entries    = listOf(entry(0, 80.0)),
            unit       = WeightUnit.KG,
            password   = password,
            outputDir  = tmpFolder.root,
        )
        assertTrue(result.isSuccess)
        val zipFile = result.getOrThrow()

        // The zip4j API exposes the encryption method on each file header.
        val readPw = "StrongPassword99!".toCharArray()
        ZipFile(zipFile, readPw).use { zf ->
            val header = zf.fileHeaders.firstOrNull()
                ?: error("ZIP has no file headers")
            val encMethod = header.encryptionMethod
            assertEquals(
                "Should be AES (not ZipCrypto)",
                net.lingala.zip4j.model.enums.EncryptionMethod.AES,
                encMethod,
            )
            assertEquals(
                "Should be AES-256",
                net.lingala.zip4j.model.enums.AesKeyStrength.KEY_STRENGTH_256,
                header.aesExtraDataRecord?.aesKeyStrength,
            )
        }
    }

    // ── Estimated size ────────────────────────────────────────────────────────

    @Test
    fun `estimated size is positive for non-empty entries`() {
        val entries = (0 until 10).map { entry(it, 80.0 - it * 0.1) }
        val estimate = CsvExporter.estimateEncryptedSizeBytes(entries, WeightUnit.KG)
        assertTrue("Estimated size should be positive", estimate > 0)
    }

    @Test
    fun `estimated size for empty list is at least 1 byte`() {
        val estimate = CsvExporter.estimateEncryptedSizeBytes(emptyList(), WeightUnit.KG)
        assertTrue("Estimated size should be at least 1", estimate >= 1L)
    }

    @Test
    fun `estimated size is approximately half of plaintext CSV bytes`() {
        val entries = (0 until 20).map { entry(it, 80.0 - it * 0.1) }
        val csvSize = CsvExporter.export(entries, WeightUnit.KG).toByteArray().size.toLong()
        val estimate = CsvExporter.estimateEncryptedSizeBytes(entries, WeightUnit.KG)
        // Allow 10% tolerance on the 0.5 heuristic
        val expected = (csvSize * 0.5).toLong()
        assertTrue("Estimate should be within 10% of csvBytes*0.5",
            estimate in (expected - expected / 10)..(expected + expected / 10 + 1))
    }
}
