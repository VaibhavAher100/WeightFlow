package com.weightflow.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.weightflow.domain.Badge
import com.weightflow.domain.WeightUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPrefsDataStore(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        private val THEME_PALETTE = stringPreferencesKey("theme_palette")
        private val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        private val SEEN_BADGES = stringSetPreferencesKey("seen_badges")
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

    /** RFC #24 — Reactive seen-badge persistence. Emits the set of badges the user has already seen. */
    val seenBadgesFlow: Flow<Set<Badge>> = dataStore.data.map { prefs ->
        (prefs[SEEN_BADGES] ?: emptySet()).mapNotNull {
            runCatching { Badge.valueOf(it) }.getOrNull()
        }.toSet()
    }

    /** Mark one or more badges as seen so they are excluded from [newlyUnlockedBadges]. */
    suspend fun markBadgesSeen(badges: Set<Badge>) {
        dataStore.edit { prefs ->
            val existing = prefs[SEEN_BADGES] ?: emptySet()
            prefs[SEEN_BADGES] = existing + badges.map { it.name }
        }
    }
}
