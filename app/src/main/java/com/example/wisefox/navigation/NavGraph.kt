package com.example.wisefox.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.wisefox.screens.LoginScreen
import com.example.wisefox.screens.common.WiseFoxLayout

// Placeholder composables – replace with real screen content
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.wisefox.screens.HomeScreen
import com.example.wisefox.ui.theme.TextWhite

@Composable
fun WiseFoxNavGraph(navController: NavHostController) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Login.route
    ) {

        // ── Login (no bottom nav shell) ───────────────────────────────────────
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess       = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    // TODO: navigate to Register screen
                }
            )
        }

        // ── Home ──────────────────────────────────────────────────────────────
        composable(Screen.Home.route) {
            WiseFoxLayout(navController = navController) {
                HomeScreen(navController)
            }
        }

        // ── Transactions ──────────────────────────────────────────────────────
        composable(Screen.Transactions.route) {
            WiseFoxLayout(navController = navController) {
                PlaceholderScreen("Transactions")
            }
        }

        // ── Ledger ────────────────────────────────────────────────────────────
        composable(Screen.Ledger.route) {
            WiseFoxLayout(navController = navController) {
                PlaceholderScreen("Ledger")
            }
        }

        // ── Profile ───────────────────────────────────────────────────────────
        composable(Screen.Profile.route) {
            WiseFoxLayout(navController = navController) {
                PlaceholderScreen("Profile")
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(name: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = name, color = TextWhite)
    }
}