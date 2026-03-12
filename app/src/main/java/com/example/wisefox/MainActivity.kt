package com.example.wisefox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.wisefox.navigation.WiseFoxNavGraph
import com.example.wisefox.ui.theme.WiseFoxTheme

class MainActivity : ComponentActivity() {
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