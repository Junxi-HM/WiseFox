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
import com.example.wisefox.viewmodels.LoginUiState
import com.example.wisefox.viewmodels.LoginViewModel
import com.example.wisefox.viewmodels.ProfileViewModel

@Composable
fun WiseFoxNavGraph(navController: NavHostController) {

    // Shared LoginViewModel across Login → GoogleRegister
    val loginViewModel: LoginViewModel = viewModel()
    val loginUiState by loginViewModel.uiState.collectAsStateWithLifecycle()

    // Shared ProfileViewModel across Profile → EditProfile (so data isn't re-fetched)
    val profileViewModel: ProfileViewModel = viewModel()

    // Navigate to GoogleRegister when needed
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
                onNavigateToRegister = { /* TODO: manual register */ },
                viewModel            = loginViewModel
            )
        }

        // ── Google Register ───────────────────────────────────────────────────
        composable(
            route     = Screen.GoogleRegister.route,
            arguments = listOf(
                navArgument("googleToken") { type = NavType.StringType },
                navArgument("email")       { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val googleToken = backStackEntry.arguments?.getString("googleToken") ?: ""
            val email       = backStackEntry.arguments?.getString("email") ?: ""
            GoogleRegisterScreen(
                googleToken       = googleToken,
                email             = email,
                viewModel         = loginViewModel,
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

        // ── Edit Profile (no bottom nav — full screen) ─────────────────────────
        composable(Screen.EditProfile.route) {
            WiseFoxLayout(navController = navController) {
                EditProfileScreen(
                    navController = navController,
                    viewModel     = profileViewModel
                )
            }
        }
    }
}