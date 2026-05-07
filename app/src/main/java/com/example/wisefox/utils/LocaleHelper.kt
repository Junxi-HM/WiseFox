package com.example.wisefox.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
 *   - `languageChangeTick` is a counter that increments on every successful
 *     language switch. UI can use it as a LaunchedEffect key to show a
 *     short loading indicator when the user changes language.
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

    /**
     * Increments every time the user switches language. UI components can
     * key a LaunchedEffect on this value to trigger a brief loading overlay.
     */
    var languageChangeTick by mutableIntStateOf(0)
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
        // Bump the tick so observers (e.g. loading overlay) react.
        languageChangeTick++
    }

    /**
     * Build a Configuration whose locale matches the requested code.
     * Re-uses the base configuration so densities, screen sizes, etc.
     * remain correct.
     */
    fun localizedConfiguration(base: Context, languageCode: String): Configuration {
        val locale = localeFor(languageCode)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return config
    }

    /**
     * Wrap an Activity in a thin ContextWrapper whose `getResources()`
     * returns Resources locked to the chosen locale, while preserving the
     * underlying Activity (so `Context.findActivity()` and similar lookups
     * by Compose APIs like `rememberLauncherForActivityResult` still work).
     *
     * This is what we feed into `LocalContext provides ...` from MainActivity.
     */
    fun localizedActivityContext(activity: Context, languageCode: String): Context {
        val cfg = localizedConfiguration(activity, languageCode)
        // `createConfigurationContext` already returns a context with the
        // requested configuration's resources — we just need to keep the
        // ActivityResultRegistry / ViewModelStore / etc. owners visible to
        // composables, which the wrapper below ensures.
        val configContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            activity.createConfigurationContext(cfg)
        } else {
            @Suppress("DEPRECATION")
            activity.resources.updateConfiguration(cfg, activity.resources.displayMetrics)
            activity
        }
        return LocalisedActivityWrapper(activity, configContext.resources)
    }

    /**
     * Wrap context in Application/Activity attachBaseContext so the locale
     * persists across activity creations. Used at very early startup — the
     * returned Context is a plain ConfigurationContext, which is fine for
     * attachBaseContext (Activity itself is what gets the wrapper applied).
     */
    fun applyStoredLocale(context: Context): Context {
        val code = SessionManager.getLanguage()
        val locale = localeFor(code)
        Locale.setDefault(locale)
        val cfg = Configuration(context.resources.configuration)
        cfg.setLocale(locale)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(cfg)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(cfg, context.resources.displayMetrics)
            context
        }
    }
}

/**
 * Wraps an Activity (or other Context) so that:
 *   - `getResources()` returns Resources tied to a specific locale, so
 *     Compose's `stringResource(...)` resolves against the chosen language.
 *   - Everything else (Activity casts, ContextOwner lookups, lifecycle, etc.)
 *     delegates to the underlying Activity, so APIs like
 *     `rememberLauncherForActivityResult`, `LocalLifecycleOwner` and
 *     `LocalViewModelStoreOwner` keep working.
 *
 * This is the missing piece that prevented `IllegalStateException: No
 * ActivityResultRegistryOwner` when `LocalContext` was overridden with a
 * plain `createConfigurationContext()` result.
 */
private class LocalisedActivityWrapper(
    base: Context,
    private val localisedResources: Resources
) : ContextWrapper(base) {
    override fun getResources(): Resources = localisedResources
    override fun getAssets() = localisedResources.assets
}