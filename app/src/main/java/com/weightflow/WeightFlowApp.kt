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
