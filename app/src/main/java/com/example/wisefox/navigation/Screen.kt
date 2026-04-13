package com.example.wisefox.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object Login        : Screen("login")
    object Home         : Screen("home")
    object Transactions : Screen("transactions")
    object Ledger       : Screen("ledger")
    object Profile      : Screen("profile")

    // Google 注册页，携带 googleToken 和 email 作为路由参数
    object GoogleRegister : Screen("google_register/{googleToken}/{email}") {
        fun createRoute(googleToken: String, email: String): String {
            val encodedToken = URLEncoder.encode(googleToken, StandardCharsets.UTF_8.toString())
            val encodedEmail = URLEncoder.encode(email,       StandardCharsets.UTF_8.toString())
            return "google_register/$encodedToken/$encodedEmail"
        }
    }
}