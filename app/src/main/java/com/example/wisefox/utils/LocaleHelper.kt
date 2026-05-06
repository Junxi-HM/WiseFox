package com.example.wisefox.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/**
 * Locale management for WiseFox.
 *
 * Strategy:
 *   - We keep a single source of truth for the current language code
 *     (`currentLanguage`) as a Compose-observable mutableStateOf.
 *   - MainActivity wraps the whole NavGraph with a CompositionLocalProvider
 *     that swaps LocalConfiguration / LocalContext whenever currentLanguage
 *     changes. This makes every stringResource(...) inside the tree
 *     re-resolve against the new locale on the SAME frame — no Activity
 *     recreate(), no "have to tap twice" bug.
 *   - We still call applyStoredLocale() in attachBaseContext so resources
 *     loaded BEFORE Compose runs (e.g. system back button labels) match.
 *
 * Language codes accepted: "EN", "ES", "CN"
 */
object LocaleHelper {

    /**
     * The currently active language code. Backed by a Compose state so that
     * any composable that reads it will recompose on change.
     */
    var currentLanguage by mutableStateOf("EN")
        private set

    /** Call this once SessionManager is initialised (e.g. from Application.onCreate). */
    fun initFromSession() {
        currentLanguage = SessionManager.getLanguage()
    }

    private fun localeFor(code: String): Locale = when (code.uppercase()) {
        "ES" -> Locale("es")
        "CN" -> Locale("zh")
        else -> Locale("en")
    }

    /**
     * Change the app language. Persists the choice and updates the Compose
     * state — the UI will recompose with the new locale on the next frame.
     * NO Activity.recreate() is performed.
     */
    fun changeLanguage(languageCode: String) {
        val normalised = languageCode.uppercase()
        if (normalised == currentLanguage) return
        SessionManager.saveLanguage(normalised)
        currentLanguage = normalised
        // Update the JVM default so non-Compose code paths
        // (e.g. backend Accept-Language headers) also see the new locale.
        Locale.setDefault(localeFor(normalised))
    }

    /**
     * Build a Context whose Configuration has the requested locale applied.
     * Used by MainActivity's CompositionLocalProvider to feed Compose a
     * Context that resolves resources in the chosen language.
     */
    fun localizedContext(base: Context, languageCode: String): Context {
        val locale = localeFor(languageCode)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            base.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            base.resources.updateConfiguration(config, base.resources.displayMetrics)
            base
        }
    }

    /**
     * Wrap context in Application/Activity attachBaseContext so the locale
     * persists across activity creations. Used at very early startup.
     */
    fun applyStoredLocale(context: Context): Context {
        val code = SessionManager.getLanguage()
        val locale = localeFor(code)
        Locale.setDefault(locale)
        return localizedContext(context, code)
    }
}