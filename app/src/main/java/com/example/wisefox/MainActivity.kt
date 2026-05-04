package com.example.wisefox

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.wisefox.navigation.WiseFoxNavGraph
import com.example.wisefox.ui.theme.WiseFoxTheme
import com.example.wisefox.utils.LocaleHelper

class MainActivity : ComponentActivity() {

    // FIX #2: wrap base context with the stored locale BEFORE the activity inflates
    // any resources — guarantees the right language even after recreate().
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyStoredLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()          // draw behind status & navigation bars
        setContent {
            WiseFoxTheme {
                val navController = rememberNavController()
                WiseFoxNavGraph(navController = navController)
            }
        }
    }
}
