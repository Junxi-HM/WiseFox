package com.example.wisefox.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

/**
 * FIX #2: Applies a locale change at runtime so ALL stringResource() calls
 * pick up the new language immediately (not only when changing screen).
 *
 * The trick is: changing Configuration alone is NOT enough — already-composed
 * Composables hold cached resolved strings. We must force-recreate the activity
 * so the whole composition tree re-runs against the new resources.
 *
 * Usage:
 *   LocaleHelper.applyAndRecreate(context, "ES")
 *
 * Language codes accepted: "EN", "ES", "CN"
 */
object LocaleHelper {

    private fun localeFor(code: String): Locale = when (code.uppercase()) {
        "ES" -> Locale("es")
        "CN" -> Locale("zh")
        else -> Locale("en")
    }

    /**
     * Update the resources configuration of the given context
     * (typically the Activity context).
     */
    fun setLocale(context: Context, languageCode: String) {
        val locale = localeFor(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    /**
     * FIX #2: Persist + apply + recreate Activity in one call.
     * Call this from any screen when the user changes language. The Activity
     * is recreated so every Composable re-resolves its stringResource() against
     * the new locale — no more "some texts only change when I switch screen".
     */
    fun applyAndRecreate(activity: Activity, languageCode: String) {
        SessionManager.saveLanguage(languageCode)
        setLocale(activity, languageCode)
        activity.recreate()
    }

    /**
     * Wrap context in Application/Activity attachBaseContext so the locale
     * persists across activity recreations.
     */
    fun applyStoredLocale(context: Context): Context {
        val locale = localeFor(SessionManager.getLanguage())
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
}
