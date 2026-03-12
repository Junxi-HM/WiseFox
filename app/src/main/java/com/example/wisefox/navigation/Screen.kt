package com.example.wisefox.navigation

// ── Sealed class for all routes ───────────────────────────────────────────────
sealed class Screen(val route: String) {
    object Login        : Screen("login")
    object Home         : Screen("home")
    object Transactions : Screen("transactions")
    object Ledger       : Screen("ledger")
    object Profile      : Screen("profile")
}