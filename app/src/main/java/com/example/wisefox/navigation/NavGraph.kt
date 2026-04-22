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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.wisefox.screens.AIScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.wisefox.screens.GoogleRegisterScreen
import com.example.wisefox.screens.HomeScreen
import com.example.wisefox.screens.ProfileScreen
import com.example.wisefox.screens.TransactionsScreen
import com.example.wisefox.ui.theme.TextWhite
import com.example.wisefox.viewmodels.LoginUiState
import com.example.wisefox.viewmodels.LoginViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.wisefox.screens.LedgerDetailScreen

import androidx.navigation.navArgument
import com.example.wisefox.model.LedgerResponse
import com.example.wisefox.network.LedgerApiService
import com.example.wisefox.network.RetrofitClient
import com.example.wisefox.screens.LedgerDetailScreen
import kotlinx.coroutines.runBlocking
@Composable
fun WiseFoxNavGraph(navController: NavHostController) {

    // 共享同一个 LoginViewModel，使 Login → GoogleRegister 两个屏幕能共享状态
    val loginViewModel: LoginViewModel = viewModel()
    val loginUiState by loginViewModel.uiState.collectAsStateWithLifecycle()

    // 监听 NeedRegister 状态 → 跳转到注册页
    LaunchedEffect(loginUiState) {
        if (loginUiState is LoginUiState.NeedRegister) {
            val state = loginUiState as LoginUiState.NeedRegister
            navController.navigate(
                Screen.GoogleRegister.createRoute(state.googleToken, state.email)
            )
        }
    }

    NavHost(
        navController    = navController,
        startDestination = Screen.Login.route
    ) {

        // ── Login ─────────────────────────────────────────────────────────────
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess       = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { /* TODO: 手动注册页 */ },
                viewModel            = loginViewModel
            )
        }

        // ── Google Register ───────────────────────────────────────────────────
        composable(
            route = Screen.GoogleRegister.route,
            arguments = listOf(
                navArgument("googleToken") { type = NavType.StringType },
                navArgument("email")       { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val googleToken = backStackEntry.arguments?.getString("googleToken") ?: ""
            val email       = backStackEntry.arguments?.getString("email") ?: ""
            GoogleRegisterScreen(
                googleToken     = googleToken,
                email           = email,
                viewModel       = loginViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
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
                TransactionsScreen(navController)
            }
        }

        // ── Ledger ────────────────────────────────────────────────────────────
        composable(Screen.AI.route) {
            WiseFoxLayout(navController = navController) {
                AIScreen(navController)
            }
        }

        // ── Ledger Detail ─────────────────────────────────────────────────────
        composable(
            route = Screen.LedgerDetail.route,
            arguments = listOf(navArgument("ledgerId") { type = NavType.LongType })
        ) { backStackEntry ->
            val ledgerId = backStackEntry.arguments?.getLong("ledgerId") ?: return@composable
            // We fetch ledger info here to pass down as LedgerResponse
            // Use a ViewModel or a simple state to load it
            val ledgerState = remember { mutableStateOf<LedgerResponse?>(null) }
            LaunchedEffect(ledgerId) {
                try {
                    val api = RetrofitClient.instance.create(LedgerApiService::class.java)
                    val resp = api.getLedgerById(ledgerId)
                    if (resp.isSuccessful) ledgerState.value = resp.body()
                } catch (e: Exception) { /* ignore */ }
            }
            ledgerState.value?.let { ledger ->
                WiseFoxLayout(navController = navController) {
                    LedgerDetailScreen(navController = navController, ledger = ledger)
                }
            } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = com.example.wisefox.ui.theme.WiseFoxOrange)
            }
        }

        // ── Profile ───────────────────────────────────────────────────────────
        composable(Screen.Profile.route) {
            WiseFoxLayout(navController = navController) {
                ProfileScreen(navController)
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