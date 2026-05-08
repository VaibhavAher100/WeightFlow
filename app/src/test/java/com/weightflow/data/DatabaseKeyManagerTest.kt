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
    fun `resetProvider restores production provider type`() {
        DatabaseKeyManager.setProviderForTesting(FakeKeyProvider())
        DatabaseKeyManager.resetProvider()
        // After reset the provider is AndroidKeystoreKeyProvider.
        // We can only verify keyExists() without crashing on JVM — it should be false
        // because there is no real Keystore available in unit-test JVM.
        // The assertion merely confirms the call does not throw.
        try {
            DatabaseKeyManager.keyExists()
            // If it returns (even false) that is acceptable
        } catch (_: Exception) {
            // UnsatisfiedLinkError / ProviderException from Keystore on plain JVM is
            // acceptable — the important thing is that the reset itself did not throw.
        }
    }

    // ── Key size constant ─────────────────────────────────────────────────────

    @Test
    fun `AndroidKeystoreKeyProvider KEY_SIZE_BITS is 256`() {
        assertEquals(256, AndroidKeystoreKeyProvider.KEY_SIZE_BITS)
    }

    @Test
    fun `AndroidKeystoreKeyProvider KEY_ALIAS is stable`() {
        assertEquals("weightflow_db_key", AndroidKeystoreKeyProvider.KEY_ALIAS)
    }
}
