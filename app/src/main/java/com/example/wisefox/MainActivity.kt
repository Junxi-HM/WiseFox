package com.example.wisefox

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.wisefox.navigation.WiseFoxNavGraph
import com.example.wisefox.ui.theme.WiseFoxOrangeDark
import com.example.wisefox.ui.theme.WiseFoxSubCardBg
import com.example.wisefox.ui.theme.WiseFoxTheme
import com.example.wisefox.utils.LocaleHelper
import kotlinx.coroutines.delay

/** How long the language-switch loading overlay stays visible. */
private const val LANGUAGE_LOADING_DURATION_MS = 350L

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

            // Build a Context that:
            //   - reports a locale-overridden Resources for stringResource(...)
            //   - still IS an Activity for purposes of Compose APIs that walk
            //     up the Context chain (ActivityResultRegistryOwner,
            //     LifecycleOwner, ViewModelStoreOwner, ...).
            val localisedContext = remember(language, baseContext) {
                LocaleHelper.localizedActivityContext(baseContext, language)
            }
            val localisedConfiguration = remember(language, localisedContext) {
                localisedContext.resources.configuration
            }

            // Show a full-screen spinner overlay briefly whenever the user
            // switches language. The overlay sits above the entire NavHost
            // including the bottom navigation bar.
            val languageTick = LocaleHelper.languageChangeTick
            var showLanguageLoading by remember { mutableStateOf(false) }
            LaunchedEffect(languageTick) {
                if (languageTick > 0) {
                    showLanguageLoading = true
                    delay(LANGUAGE_LOADING_DURATION_MS)
                    showLanguageLoading = false
                }
            }

            CompositionLocalProvider(
                LocalContext provides localisedContext,
                LocalConfiguration provides localisedConfiguration
            ) {
                WiseFoxTheme {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val navController = rememberNavController()
                        WiseFoxNavGraph(navController = navController)

                        if (showLanguageLoading) {
                            LanguageSwitchOverlay()
                        }
                    }
                }
            }
        }
    }
}

/**
 * Full-screen translucent overlay with a centred spinner. Shown briefly
 * while the user changes language — purely for visual feedback, no text.
 * Placed at the activity-root level so it covers EVERYTHING (NavHost,
 * bottom nav bar, system bars background) during the transition.
 */
@Composable
private fun LanguageSwitchOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.25f))
            // Consume all pointer events so taps don't reach widgets underneath.
            .pointerInput(Unit) { },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(WiseFoxSubCardBg),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color       = WiseFoxOrangeDark,
                strokeWidth = 3.dp,
                modifier    = Modifier.size(36.dp)
            )
        }
    }
}