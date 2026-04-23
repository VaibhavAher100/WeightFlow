package com.weightflow

import android.app.Application
import androidx.datastore.preferences.preferencesDataStore
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
        // Phase 4 — Crashlytics activation checklist:
        //   1. Copy google-services.json to app/
        //   2. Uncomment plugins in app/build.gradle.kts (google-services + firebase-crashlytics-gradle)
        //   3. Uncomment Firebase deps in app/build.gradle.kts
        //   4. Uncomment library aliases in gradle/libs.versions.toml
        //   5. Replace this comment with: FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        scheduleWeightReminder()
    }

    private fun scheduleWeightReminder() {
        com.weightflow.worker.WeightReminderWorker.schedule(this)
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
