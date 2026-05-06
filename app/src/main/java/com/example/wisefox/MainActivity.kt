package com.example.wisefox

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.wisefox.navigation.WiseFoxNavGraph
import com.example.wisefox.ui.theme.WiseFoxTheme
import com.example.wisefox.utils.LocaleHelper

class MainActivity : ComponentActivity() {

    // Wrap base context with the stored locale BEFORE the activity inflates
    // any resources — guarantees the right language for resources loaded
    // outside Compose (e.g. system widgets, action bar).
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyStoredLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()          // draw behind status & navigation bars
        setContent {
            // Read the current language from LocaleHelper. Because it's a
            // Compose mutableState, any change triggers a recomposition of
            // this block, which rebuilds the localised Context below — and
            // every stringResource(...) inside the tree picks up the change
            // on the SAME frame. No Activity.recreate() needed.
            val language = LocaleHelper.currentLanguage
            val baseContext = LocalContext.current

            // Build a context whose Configuration has the chosen locale.
            // Keyed on `language` so it's rebuilt whenever the user changes it.
            val localisedContext = remember(language) {
                LocaleHelper.localizedContext(baseContext, language)
            }
            val localisedConfiguration = remember(language) {
                localisedContext.resources.configuration
            }

            CompositionLocalProvider(
                LocalContext provides localisedContext,
                LocalConfiguration provides localisedConfiguration
            ) {
                WiseFoxTheme {
                    val navController = rememberNavController()
                    WiseFoxNavGraph(navController = navController)
                }
            }
        }
    }
}