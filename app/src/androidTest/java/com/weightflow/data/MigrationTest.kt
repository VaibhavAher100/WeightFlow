package com.weightflow.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for Room schema migrations.
 *
 * These tests verify that:
 *  - v1 → v2: `achievedAtEpochDay` column is added to `user_profile`
 *  - v2 → v3: no-op schema migration (encryption is handled at file level)
 *  - Full path v1 → v3: chained migrations leave schema valid at v3
 *
 * NOTE: Compile-verified only. Requires a connected device/emulator to actually run.
 * Run with: ./gradlew connectedDebugAndroidTest
 *
 * The MigrationTestHelper loads schema JSON files from androidTest assets.
 * The schemas directory is wired as an asset source set in build.gradle.kts.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val dbName = "migration-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    /**
     * Create a v1 database with one row in each table, migrate to v2, and validate the schema.
     *
     * v1 weight_entries columns: id (INTEGER PK AUTOINCREMENT), timestamp (INTEGER), weightKg (REAL), note (TEXT)
     * v1 user_profile columns: id (INTEGER PK), displayName (TEXT), goalWeightKg (REAL nullable),
     *   targetDateEpochDay (INTEGER nullable), heightCm (REAL nullable),
     *   maintenanceMode (INTEGER), maintenanceRangeKg (REAL), maintenanceModeActivatedAt (INTEGER nullable)
     *
     * v2 adds: achievedAtEpochDay (INTEGER nullable) to user_profile
     */
    @Test
    fun migrate1To2_preservesData() {
        helper.createDatabase(dbName, 1).apply {
            // Insert a weight entry using v1 columns (no id — AUTOINCREMENT)
            execSQL(
                "INSERT INTO weight_entries (timestamp, weightKg, note) " +
                    "VALUES (1700000000000, 75.5, 'morning weigh-in')"
            )
            // Insert a user profile using all v1 columns
            execSQL(
                "INSERT INTO user_profile " +
                    "(id, displayName, goalWeightKg, targetDateEpochDay, heightCm, " +
                    "maintenanceMode, maintenanceRangeKg, maintenanceModeActivatedAt) " +
                    "VALUES (1, 'Test User', 70.0, 19800, 175.0, 0, 1.0, NULL)"
            )
            close()
        }

        // Migrate to v2 and validate the resulting schema matches schemas/2.json
        val db = helper.runMigrationsAndValidate(dbName, 2, true, AppDatabase.MIGRATION_1_2)

        // Verify the newly-added column exists and the existing row is intact
        val cursor = db.query("SELECT achievedAtEpochDay, displayName FROM user_profile WHERE id = 1")
        assert(cursor.moveToFirst()) { "Expected the migrated user_profile row to exist" }
        val colIndex = cursor.getColumnIndex("achievedAtEpochDay")
        assert(colIndex >= 0) { "achievedAtEpochDay column must exist after v1→v2 migration" }
        assert(cursor.isNull(colIndex)) { "achievedAtEpochDay should be NULL for pre-existing row" }
        cursor.close()
    }

    /**
     * Migrate from v2 to v3.
     * v2 → v3 is a no-op schema migration (encryption applied at file level by buildDatabase).
     * The schema identity hash is the same for both versions.
     */
    @Test
    fun migrate2To3_noSchemaChange() {
        helper.createDatabase(dbName, 2).apply {
            execSQL(
                "INSERT INTO weight_entries (timestamp, weightKg, note) " +
                    "VALUES (1700000000001, 80.0, 'v2 entry')"
            )
            close()
        }

        // Validate: schema must match schemas/3.json after migration (identical to v2 schema)
        helper.runMigrationsAndValidate(dbName, 3, true, AppDatabase.MIGRATION_2_3)
    }

    /**
     * Full chain: v1 → v2 → v3 in a single runMigrationsAndValidate call.
     * Ensures Room can apply both migrations sequentially and the final schema is valid at v3.
     */
    @Test
    fun migrateAll_1To3() {
        helper.createDatabase(dbName, 1).apply {
            execSQL(
                "INSERT INTO weight_entries (timestamp, weightKg, note) " +
                    "VALUES (1700000000002, 65.0, 'chain migration test')"
            )
            close()
        }

        helper.runMigrationsAndValidate(
            dbName,
            3,
            true,
            AppDatabase.MIGRATION_1_2,
            AppDatabase.MIGRATION_2_3,
        )
    }
}
