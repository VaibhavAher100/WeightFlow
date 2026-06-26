package com.weightflow.ui.i18n

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/** Thin wrapper over AppCompat per-app locales. androidx persists the choice. */
object LocaleManager {

    /** Supported app languages. `SYSTEM` = follow device. */
    enum class AppLanguage(val tag: String?) {
        SYSTEM(null),
        ENGLISH("en"),
        GERMAN("de"),
    }

    /** Apply a language. Empty list => follow system. Recreates the activity. */
    fun setLanguage(language: AppLanguage) {
        val locales = when (language.tag) {
            null -> LocaleListCompat.getEmptyLocaleList()
            else -> LocaleListCompat.forLanguageTags(language.tag)
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }

    /** The currently selected app language (SYSTEM if none forced). */
    fun currentLanguage(): AppLanguage {
        val tag = AppCompatDelegate.getApplicationLocales()[0]?.language
        return when (tag) {
            "en" -> AppLanguage.ENGLISH
            "de" -> AppLanguage.GERMAN
            else -> AppLanguage.SYSTEM
        }
    }
}
