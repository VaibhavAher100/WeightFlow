package com.weightflow.data

import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [DatabaseKeyManager] and the [DatabaseKeyProvider] contract.
 *
 * These tests run on the JVM (no Android Keystore required) by injecting a
 * [FakeKeyProvider] via [DatabaseKeyManager.setProviderForTesting].
 *
 * Keystore-backed verification on a real device is covered separately in
 * `androidTest/data/DatabaseKeyManagerInstrumentedTest.kt`.
 */
class DatabaseKeyManagerTest {

    /**
     * In-memory [DatabaseKeyProvider] that mimics the production contract without
     * requiring the Android Keystore hardware.
     */
    private class FakeKeyProvider(
        private val fixedKey: ByteArray = ByteArray(32) { (it + 1).toByte() },
    ) : DatabaseKeyProvider {
        private var keyGenerated = false
        var getOrCreateCallCount = 0
            private set

        override fun getOrCreateKey(): ByteArray {
            getOrCreateCallCount++
            keyGenerated = true
            return fixedKey
        }

        override fun keyExists(): Boolean = keyGenerated
    }

    @Before
    fun setUp() {
        // Ensure every test starts with a fresh fake provider.
        DatabaseKeyManager.setProviderForTesting(FakeKeyProvider())
    }

    @After
    fun tearDown() {
        DatabaseKeyManager.resetProvider()
    }

    // ── Contract: key material ────────────────────────────────────────────────

    @Test
    fun `getOrCreateKey returns non-null byte array`() {
        assertNotNull(DatabaseKeyManager.getOrCreateKey())
    }

    @Test
    fun `getOrCreateKey returns 32-byte AES-256 key`() {
        val key = DatabaseKeyManager.getOrCreateKey()
        assertEquals("AES-256 key must be 32 bytes", 32, key.size)
    }

    @Test
    fun `getOrCreateKey is idempotent — same bytes on repeated calls`() {
        val first = DatabaseKeyManager.getOrCreateKey()
        val second = DatabaseKeyManager.getOrCreateKey()
        assertArrayEquals("Key bytes must be identical across calls", first, second)
    }

    @Test
    fun `key bytes are not all zeroes`() {
        val key = DatabaseKeyManager.getOrCreateKey()
        assertFalse("Key must not be an all-zero byte array", key.all { it == 0.toByte() })
    }

    // ── Contract: existence check ─────────────────────────────────────────────

    @Test
    fun `keyExists returns false before first getOrCreateKey call`() {
        // FakeKeyProvider starts with keyGenerated = false
        DatabaseKeyManager.setProviderForTesting(FakeKeyProvider())
        assertFalse(DatabaseKeyManager.keyExists())
    }

    @Test
    fun `keyExists returns true after getOrCreateKey`() {
        DatabaseKeyManager.getOrCreateKey()
        assertTrue(DatabaseKeyManager.keyExists())
    }

    // ── Contract: delegation ──────────────────────────────────────────────────

    @Test
    fun `getOrCreateKey delegates to the injected provider`() {
        val fake = FakeKeyProvider()
        DatabaseKeyManager.setProviderForTesting(fake)
        DatabaseKeyManager.getOrCreateKey()
        assertEquals("Provider must be called exactly once", 1, fake.getOrCreateCallCount)
    }

    @Test
    fun `setProviderForTesting replaces the active provider`() {
        val expected = ByteArray(32) { 0xAB.toByte() }
        DatabaseKeyManager.setProviderForTesting(FakeKeyProvider(expected))
        assertArrayEquals(expected, DatabaseKeyManager.getOrCreateKey())
    }

    @Test
    fun `resetProvider clears provider — subsequent call without init throws`() {
        DatabaseKeyManager.setProviderForTesting(FakeKeyProvider())
        DatabaseKeyManager.resetProvider()
        // After reset, provider is null. getOrCreateKey() must throw IllegalStateException.
        try {
            DatabaseKeyManager.getOrCreateKey()
            // If we reach here the reset had no effect — fail explicitly.
            throw AssertionError("Expected IllegalStateException but no exception was thrown")
        } catch (_: IllegalStateException) {
            // Expected — provider is null after resetProvider()
        }
    }

    // ── EncryptedPrefsKeyProvider constants ───────────────────────────────────

    @Test
    fun `EncryptedPrefsKeyProvider KEY_SIZE_BYTES is 32`() {
        assertEquals(32, EncryptedPrefsKeyProvider.KEY_SIZE_BYTES)
    }

    @Test
    fun `EncryptedPrefsKeyProvider PREFS_FILE is stable`() {
        assertEquals("weightflow_db_key_store", EncryptedPrefsKeyProvider.PREFS_FILE)
    }

    @Test
    fun `EncryptedPrefsKeyProvider KEY_PREF is stable`() {
        assertEquals("db_passphrase", EncryptedPrefsKeyProvider.KEY_PREF)
    }
}
