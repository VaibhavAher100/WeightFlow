package com.weightflow

import android.app.Application
import android.util.Log
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.weightflow.data.AppDatabase
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
        initCrashlytics()
    }

    /**
     * Initialises Firebase Crashlytics.
     *
     * Safe to call before google-services.json is wired up — the try-catch prevents
     * an IllegalStateException from crashing the app. Crashlytics will silently be
     * a no-op until the Firebase project is configured (see app/build.gradle.kts TODO).
     *
     * Collection is disabled in DEBUG builds so test runs don't pollute production reports.
     */
    private fun initCrashlytics() {
        try {
            FirebaseCrashlytics.getInstance()
                .setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        } catch (e: Exception) {
            Log.d("WeightFlowApp", "Crashlytics not configured yet: ${e.message}")
        }
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
