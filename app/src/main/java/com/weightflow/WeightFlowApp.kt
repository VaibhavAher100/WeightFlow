package com.weightflow

import android.app.Application
import androidx.datastore.preferences.preferencesDataStore
import com.weightflow.data.AppDatabase
import com.weightflow.data.UserPrefsDataStore
import com.weightflow.data.UserProfileRepository
import com.weightflow.data.WeightRepository

private val Application.dataStore by preferencesDataStore(name = "user_prefs")

class WeightFlowApp : Application() {

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
}
