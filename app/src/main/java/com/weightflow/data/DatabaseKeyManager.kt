package com.weightflow.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Contract for supplying the Room database encryption key.
 *
 * The production implementation ([AndroidKeystoreKeyProvider]) stores a 256-bit AES
 * key permanently in the hardware-backed Android Keystore. Tests may inject an
 * alternative implementation to avoid requiring a device.
 */
internal interface DatabaseKeyProvider {
    /**
     * Returns the raw 32-byte AES-256 key to be used as the SQLCipher passphrase.
     * Generates and stores the key on the first call; returns the cached entry on
     * subsequent calls. Must be stable — the same bytes returned on every invocation
     * for the lifetime of the installation.
     */
    fun getOrCreateKey(): ByteArray

    /** Returns true when the underlying key entry already exists. */
    fun keyExists(): Boolean
}

/**
 * Production [DatabaseKeyProvider] backed by the Android Keystore system.
 *
 * Key properties:
 * - Algorithm: AES-256 in GCM mode
 * - User authentication: NOT required (offline-first, no biometric gate)
 * - No expiry: key is valid for the full lifetime of the installation
 *
 * The Keystore never exposes raw key material outside the secure enclave. The
 * [ByteArray] returned by [getOrCreateKey] is the encoded form of a
 * [javax.crypto.SecretKey] obtained via the Keystore API — it is used directly
 * as the SQLCipher passphrase and then discarded from memory.
 *
 * Thread-safe: Android Keystore operations are internally synchronized.
 */
internal class AndroidKeystoreKeyProvider : DatabaseKeyProvider {

    private val keyStore: KeyStore
        get() = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }

    override fun getOrCreateKey(): ByteArray {
        if (!keyExists()) generateKey()
        val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
        return secretKey.encoded
    }

    override fun keyExists(): Boolean = keyStore.containsAlias(KEY_ALIAS)

    private fun generateKey() {
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setKeySize(KEY_SIZE_BITS)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(false)
            .build()

        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
            .apply { init(spec) }
            .generateKey()
    }

    internal companion object {
        const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        const val KEY_ALIAS = "weightflow_db_key"
        const val KEY_SIZE_BITS = 256
    }
}

/**
 * Singleton accessor used by [AppDatabase]. Replaced in tests via [setProviderForTesting].
 */
internal object DatabaseKeyManager {

    @Volatile
    private var provider: DatabaseKeyProvider = AndroidKeystoreKeyProvider()

    /** Returns the database encryption key from the configured [DatabaseKeyProvider]. */
    fun getOrCreateKey(): ByteArray = provider.getOrCreateKey()

    /** Returns true when the underlying key entry already exists. */
    fun keyExists(): Boolean = provider.keyExists()

    /**
     * Replaces the [DatabaseKeyProvider] for testing purposes.
     * Must only be called from test code.
     */
    internal fun setProviderForTesting(testProvider: DatabaseKeyProvider) {
        provider = testProvider
    }

    /** Resets to the production [AndroidKeystoreKeyProvider]. Call in test tearDown. */
    internal fun resetProvider() {
        provider = AndroidKeystoreKeyProvider()
    }
}
