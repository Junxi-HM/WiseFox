package com.example.wisefox.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.wisefox.screens.*
import com.example.wisefox.screens.common.WiseFoxLayout
import com.example.wisefox.viewmodels.HomeViewModel
import com.example.wisefox.viewmodels.LoginUiState
import com.example.wisefox.viewmodels.LoginViewModel
import com.example.wisefox.viewmodels.ProfileViewModel

@Composable
fun WiseFoxNavGraph(navController: NavHostController) {

    val loginViewModel: LoginViewModel = viewModel()
    val loginUiState by loginViewModel.uiState.collectAsStateWithLifecycle()

    val profileViewModel: ProfileViewModel = viewModel()

    // 共享同一个 HomeViewModel，这样 ledger_detail 路由能从里面查账本
    val homeViewModel: HomeViewModel = viewModel()

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
                onLoginSuccess = {
                    homeViewModel.loadLedgers()
                    profileViewModel.loadProfile()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { /* TODO */ },
                viewModel = loginViewModel
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
                googleToken  = googleToken,
                email        = email,
                viewModel    = loginViewModel,
                onRegisterSuccess = {
                    homeViewModel.loadLedgers()       // ← 新增
                    profileViewModel.loadProfile()    // ← 新增
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Home ──────────────────────────────────────────────────────────────
        composable(Screen.Home.route) {
            WiseFoxLayout(navController = navController) {
                HomeScreen(navController, vm = homeViewModel)
            }
        }

        // ── Transactions ──────────────────────────────────────────────────────
        composable(Screen.Transactions.route) {
            WiseFoxLayout(navController = navController) {
                TransactionsScreen(navController)
            }
        }

        // ── AI ────────────────────────────────────────────────────────────────
        composable(Screen.AI.route) {
            WiseFoxLayout(navController = navController) {
                AIScreen(navController)
            }
        }

        // ── Profile ───────────────────────────────────────────────────────────
        composable(Screen.Profile.route) {
            WiseFoxLayout(navController = navController) {
                ProfileScreen(
                    navController = navController,
                    viewModel     = profileViewModel
                )
            }
        }

        // ── Edit Profile ──────────────────────────────────────────────────────
        composable(Screen.EditProfile.route) {
            WiseFoxLayout(navController = navController) {
                EditProfileScreen(
                    navController = navController,
                    viewModel     = profileViewModel
                )
            }
        }

        // ── Ledger Detail ─────────────────────────────────────────────────────
        composable(
            route     = Screen.LedgerDetail.route,          // "ledger_detail/{ledgerId}"
            arguments = listOf(
                navArgument("ledgerId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val ledgerId = backStackEntry.arguments?.getLong("ledgerId") ?: return@composable
            val ledger   = homeViewModel.findLedgerById(ledgerId) ?: return@composable

            WiseFoxLayout(navController = navController) {
                LedgerDetailScreen(
                    navController = navController,
                    ledger        = ledger
                )
            }
        }
    }
}