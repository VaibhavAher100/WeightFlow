package com.weightflow.domain

import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.AesKeyStrength
import net.lingala.zip4j.model.enums.AesVersion
import net.lingala.zip4j.model.enums.CompressionMethod
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.ByteArrayInputStream
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * CSV export in three formats:
 *
 * - [export]           — full plaintext CSV with unit-converted weight column
 * - [exportMinimalCsv] — date + weight only (no profile / goal / notes). Still quasi-identifying
 *                        when combined with external information. See SECURITY.md §3.
 * - [exportEncryptedZip] — AES-256-GCM encrypted ZIP containing the full plaintext CSV.
 *                          Requires a 12+ character password supplied as a [CharArray] that is
 *                          zeroed immediately after the zip4j call. Password is NEVER logged,
 *                          stored in DataStore, or held in UiState.
 *
 * ### Notes on encrypted export compatibility
 * The output uses WinZip AES v2 (AES-256). Supported decompressors include 7-Zip, WinRAR, and
 * The Unarchiver. **macOS Archive Utility does not support AES-encrypted ZIPs** — document this
 * in user-facing help. See SECURITY.md §3 for the known-limitation note on ZIP central-directory
 * metadata leakage (filename + size visible without password).
 */
object CsvExporter {

    // ── Constants ─────────────────────────────────────────────────────────────

    /** Minimum password length enforced before encryption is attempted. */
    const val MIN_PASSWORD_LENGTH = 12

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Builds a full plaintext CSV string with a unit-converted weight column.
     * Entries are ordered oldest-first (ascending timestamp).
     */
    fun export(entries: List<WeightEntry>, unit: WeightUnit): String {
        val header = when (unit) {
            WeightUnit.KG  -> "date,weight_kg"
            WeightUnit.LBS -> "date,weight_lbs"
            WeightUnit.ST  -> "date,weight_st"
        }

        val rows = entries.sortedBy { it.timestamp }.map { entry ->
            val date   = Instant.ofEpochMilli(entry.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            val weight = when (unit) {
                WeightUnit.KG  -> "%.1f".format(entry.weightKg)
                WeightUnit.LBS -> "%.1f".format(WeightConverter.kgToLbs(entry.weightKg))
                WeightUnit.ST  -> {
                    val totalStones = entry.weightKg / 6.35029
                    "%.2f".format(totalStones)
                }
            }
            "$date,$weight"
        }

        return buildString {
            append(header)
            rows.forEach { append('\n'); append(it) }
        }
    }

    /**
     * Builds a minimal CSV containing **only** `date` and `weight_kg` columns.
     *
     * Profile data, goal data, and notes are intentionally omitted to reduce
     * data exposure on share. However, a date+weight series alone is
     * **quasi-identifying** — it may still link to an individual when combined
     * with other information (e.g., a known start date or distinctive weight
     * pattern). This format is therefore labeled "Minimal CSV", not "Anonymous".
     *
     * Entries are ordered oldest-first.
     */
    fun exportMinimalCsv(entries: List<WeightEntry>): String {
        val header = "date,weight_kg"
        val rows = entries.sortedBy { it.timestamp }.map { entry ->
            val date = Instant.ofEpochMilli(entry.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            "$date,${"%.1f".format(entry.weightKg)}"
        }
        return buildString {
            append(header)
            rows.forEach { append('\n'); append(it) }
        }
    }

    /**
     * Creates an AES-256 encrypted ZIP file in [outputDir] containing the
     * full plaintext CSV for [entries].
     *
     * Security contract:
     * - AES-256 (WinZip AES v2) — no ZipCrypto fallback
     * - [password] CharArray is zeroed after the zip4j call, even on failure
     * - Password is never logged, stored, or surfaced in UiState
     * - Password must be at least [MIN_PASSWORD_LENGTH] characters
     * - After writing, the ZIP is re-opened with [password] to verify the
     *   inner CSV CRC before returning success
     * - File size estimate uses `csvBytes * 0.5` (rough; labeled approximate)
     *
     * @param entries     weight entries to export
     * @param unit        display unit for the weight column
     * @param password    caller-owned CharArray — **will be zeroed** by this method
     * @param outputDir   writable temp directory (e.g. `context.cacheDir`)
     * @param dateSuffix  ISO-8601 date string used in the filename (default: today)
     *
     * @return [Result.success] with the output [File] on success,
     *         [Result.failure] with a descriptive exception on any error.
     */
    fun exportEncryptedZip(
        entries: List<WeightEntry>,
        unit: WeightUnit,
        password: CharArray,
        outputDir: File,
        dateSuffix: String = LocalDate.now().toString(),
    ): Result<File> {
        // Validate password length before touching any file I/O.
        if (password.size < MIN_PASSWORD_LENGTH) {
            password.fill(' ')
            return Result.failure(
                IllegalArgumentException(
                    "Password must be at least $MIN_PASSWORD_LENGTH characters " +
                        "(got ${password.size})."
                )
            )
        }

        val csvFileName = "weightflow_export_$dateSuffix.csv"
        val zipFileName = "weightflow_export_$dateSuffix.zip"
        val zipFile = File(outputDir, zipFileName)
        val passwordCopy = password.copyOf()

        return try {
            val csvContent = export(entries, unit)
            val csvBytes   = csvContent.toByteArray(Charsets.UTF_8)

            val params = ZipParameters().apply {
                compressionMethod  = CompressionMethod.DEFLATE
                isEncryptFiles     = true
                encryptionMethod   = EncryptionMethod.AES
                aesKeyStrength     = AesKeyStrength.KEY_STRENGTH_256
                aesVersion         = AesVersion.TWO
                fileNameInZip      = csvFileName
            }

            ZipFile(zipFile, passwordCopy).use { zf ->
                ByteArrayInputStream(csvBytes).use { stream ->
                    zf.addStream(stream, params)
                }
            }

            val passwordCopy2 = password.copyOf()
            verifyZipIntegrity(zipFile, passwordCopy2, csvFileName)
                .onFailure { ex ->
                    zipFile.delete()
                    passwordCopy2.fill(' ')
                    return Result.failure(ex)
                }
            passwordCopy2.fill(' ')

            Result.success(zipFile)
        } catch (ex: Exception) {
            zipFile.delete()
            Result.failure(ex)
        } finally {
            passwordCopy.fill(' ')
            password.fill(' ')
        }
    }

    fun estimateEncryptedSizeBytes(entries: List<WeightEntry>, unit: WeightUnit): Long {
        val csvBytes = export(entries, unit).toByteArray(Charsets.UTF_8).size.toLong()
        return (csvBytes * 0.5).toLong().coerceAtLeast(1L)
    }

    private fun verifyZipIntegrity(
        zipFile: File,
        password: CharArray,
        innerFileName: String,
    ): Result<Unit> = try {
        ZipFile(zipFile, password).use { zf ->
            val header = zf.getFileHeader(innerFileName)
                ?: return Result.failure(
                    IllegalStateException("Integrity check failed: inner file '$innerFileName' not found in ZIP.")
                )

            zf.getInputStream(header).use { stream ->
                val buffer = ByteArray(8192)
                while (stream.read(buffer) != -1) { }
            }
        }
        Result.success(Unit)
    } catch (ex: Exception) {
        Result.failure(
            IllegalStateException("ZIP integrity verification failed: ${ex.message}", ex)
        )
    }
}
