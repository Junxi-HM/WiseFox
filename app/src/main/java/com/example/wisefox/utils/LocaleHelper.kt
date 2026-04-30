package com.example.wisefox.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Applies a locale change at runtime so all stringResource() calls
 * pick up the new language immediately after recomposition.
 *
 * Usage: LocaleHelper.setLocale(context, "ES")
 *
 * Language codes accepted: "EN", "ES", "CN"
 */
object LocaleHelper {

    fun setLocale(context: Context, languageCode: String) {
        val locale = when (languageCode.uppercase()) {
            "ES"  -> Locale("es")
            "CN"  -> Locale("zh")
            else  -> Locale("en")
        }
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    /**
     * Wrap context in Application to persist the locale even when the Activity
     * is recreated. Call this in Application.attachBaseContext().
     */
    fun applyStoredLocale(context: Context): Context {
        val lang   = SessionManager.getLanguage()
        val locale = when (lang.uppercase()) {
            "ES" -> Locale("es")
            "CN" -> Locale("zh")
            else -> Locale("en")
        }
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}