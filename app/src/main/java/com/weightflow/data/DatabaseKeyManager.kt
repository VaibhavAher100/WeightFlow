package com.weightflow.data

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

/**
 * Contract for supplying the Room database encryption key.
 *
 * Tests inject an alternative implementation to avoid requiring a device or
 * the Android Keystore hardware.
 */
internal interface DatabaseKeyProvider {
    /**
     * Returns the raw 32-byte passphrase used as the SQLCipher database key.
     * Must be stable — identical bytes returned on every invocation for the
     * lifetime of the installation.
     */
    fun getOrCreateKey(): ByteArray

    /** Returns true when the underlying key entry already exists. */
    fun keyExists(): Boolean
}

/**
 * Production [DatabaseKeyProvider].
 *
 * Generates a cryptographically random 32-byte passphrase once and persists it
 * in [EncryptedSharedPreferences] (AES-256-GCM master key stored in the Android
 * Keystore, value read back as plaintext).
 *
 * This avoids the critical flaw of the previous [AndroidKeystoreKeyProvider]
 * approach: calling `SecretKey.encoded` on a hardware-backed Android Keystore key
 * returns `null` because the raw key material never leaves the secure enclave.
 * SQLCipher would receive a null passphrase and fail to open the database on real
 * devices.
 *
 * By storing a randomly generated passphrase inside [EncryptedSharedPreferences]
 * instead, the Keystore protects the master encryption key while the passphrase
 * itself can be read back as plaintext bytes on every subsequent call.
 *
 * Passphrase lifetime: tied to the app installation. Clearing app data also
 * deletes the preferences file; the database file is also cleared at that point,
 * so no data loss occurs.
 *
 * Thread-safety: [prefs] is accessed via a `lazy` delegate (synchronized by
 * default); [EncryptedSharedPreferences] calls are individually thread-safe.
 */
internal class EncryptedPrefsKeyProvider(context: Context) : DatabaseKeyProvider {

    private val appContext = context.applicationContext

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            appContext,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override fun getOrCreateKey(): ByteArray {
        val existing = prefs.getString(KEY_PREF, null)
        if (existing != null) return hexToBytes(existing)
        val newKey = ByteArray(KEY_SIZE_BYTES).also { SecureRandom().nextBytes(it) }
        prefs.edit { putString(KEY_PREF, bytesToHex(newKey)) }
        return newKey
    }

    override fun keyExists(): Boolean = prefs.contains(KEY_PREF)

    private fun bytesToHex(bytes: ByteArray): String =
        bytes.joinToString("") { "%02x".format(it) }

    private fun hexToBytes(hex: String): ByteArray =
        ByteArray(hex.length / 2) { hex.substring(it * 2, it * 2 + 2).toInt(16).toByte() }

    internal companion object {
        const val PREFS_FILE = "weightflow_db_key_store"
        const val KEY_PREF = "db_passphrase"
        const val KEY_SIZE_BYTES = 32
    }
}

/**
 * Singleton accessor used by [AppDatabase].
 *
 * Call [init] with the application [Context] once (e.g. in
 * `Application.onCreate` or at the top of `AppDatabase.buildDatabase`)
 * before the first call to [getOrCreateKey]. Double-checked locking ensures
 * the provider is initialised exactly once.
 *
 * Tests replace the provider via [setProviderForTesting] and restore the
 * uninitialised state via [resetProvider].
 */
internal object DatabaseKeyManager {

    @Volatile private var provider: DatabaseKeyProvider? = null

    private val initLock = Any()

    /**
     * Initialises the production [EncryptedPrefsKeyProvider] if no provider has
     * been set yet. Idempotent — safe to call multiple times.
     */
    fun init(context: Context) {
        if (provider == null) {
            synchronized(initLock) {
                if (provider == null) {
                    provider = EncryptedPrefsKeyProvider(context)
                }
            }
        }
    }

    /**
     * Returns the database encryption passphrase from the configured
     * [DatabaseKeyProvider].
     *
     * @throws IllegalStateException if [init] has not been called first.
     */
    fun getOrCreateKey(): ByteArray =
        checkNotNull(provider) {
            "DatabaseKeyManager.init(context) must be called before getOrCreateKey()"
        }.getOrCreateKey()

    /**
     * Returns true when the underlying key entry already exists.
     *
     * @throws IllegalStateException if [init] has not been called first.
     */
    fun keyExists(): Boolean =
        checkNotNull(provider) {
            "DatabaseKeyManager.init(context) must be called before keyExists()"
        }.keyExists()

    /**
     * Replaces the active [DatabaseKeyProvider] with [testProvider].
     * Must only be called from test code.
     */
    internal fun setProviderForTesting(testProvider: DatabaseKeyProvider) {
        provider = testProvider
    }

    /**
     * Resets the provider to `null` (uninitialised state).
     * Call in test `tearDown` to isolate test cases.
     */
    internal fun resetProvider() {
        provider = null
    }
}
