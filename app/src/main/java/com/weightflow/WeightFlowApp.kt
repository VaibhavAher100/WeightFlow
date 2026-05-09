package com.weightflow

import android.app.Application
import androidx.datastore.preferences.preferencesDataStore
import com.weightflow.data.AppDatabase
import com.weightflow.data.DatabaseKeyManager
import com.weightflow.data.UserPrefsDataStore
import com.weightflow.data.UserProfileRepository
import com.weightflow.data.WeightRepository
import com.weightflow.domain.BadgeObserver
import com.weightflow.domain.BadgeObserverImpl
import com.weightflow.domain.HomeDataAggregatorImpl

private val Application.dataStore by preferencesDataStore(name = "user_prefs")

class WeightFlowApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialise the database key provider early so it is ready when any
        // component accesses the database. EncryptedSharedPreferences is backed
        // by the Android Keystore but returns the passphrase as readable bytes —
        // this avoids the null-encoded() issue with hardware-backed Keystore keys.
        if (!BuildConfig.DEBUG) System.loadLibrary("sqlcipher")
        DatabaseKeyManager.init(this)
        // Crashlytics: add google-services.json + wire deps when ready (Phase 5)
        // Reminders: opt-in via Settings toggle — not auto-scheduled at startup
    }

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    val weightRepository: WeightRepository by lazy {
        WeightRepository(database.weightEntryDao())
    }

    val userProfileRepository: UserProfileRepository by lazy {
        UserProfileRepository(database.userProfileDao())
    }

    val userPrefsDataStore: UserPrefsDataStore by lazy {
        UserPrefsDataStore(dataStore)
    }

    val homeDataAggregator: HomeDataAggregatorImpl by lazy {
        HomeDataAggregatorImpl(weightRepository, userProfileRepository, userPrefsDataStore)
    }

    val badgeObserver: BadgeObserver by lazy {
        BadgeObserverImpl(weightRepository, userProfileRepository, userPrefsDataStore)
    }
}
