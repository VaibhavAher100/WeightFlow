package com.weightflow.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.weightflow.domain.WeightUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPrefsDataStore(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        private val THEME_PALETTE = stringPreferencesKey("theme_palette")
        private val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    }

    val weightUnit: Flow<WeightUnit> = dataStore.data.map { prefs ->
        WeightUnit.valueOf(prefs[WEIGHT_UNIT] ?: WeightUnit.KG.name)
    }

    val themePalette: Flow<String> = dataStore.data.map { prefs ->
        prefs[THEME_PALETTE] ?: "lime"
    }

    val onboardingComplete: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[ONBOARDING_COMPLETE] ?: false
    }

    suspend fun setWeightUnit(unit: WeightUnit) {
        dataStore.edit { prefs -> prefs[WEIGHT_UNIT] = unit.name }
    }

    suspend fun setThemePalette(palette: String) {
        dataStore.edit { prefs -> prefs[THEME_PALETTE] = palette }
    }

    suspend fun setOnboardingComplete() {
        dataStore.edit { prefs -> prefs[ONBOARDING_COMPLETE] = true }
    }
}
