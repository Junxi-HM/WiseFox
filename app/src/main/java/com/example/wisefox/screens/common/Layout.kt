package com.example.wisefox.screens.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.wisefox.R
import com.example.wisefox.navigation.Screen
import com.example.wisefox.ui.theme.*

// ── Bottom nav item definition ─────────────────────────────────────────────────
data class BottomNavItem(
    val route: String,
    val labelRes: Int,
    val icon: Int
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home.route, R.string.nav_home, R.drawable.ic_home),
    BottomNavItem(Screen.Transactions.route, R.string.nav_transactions, R.drawable.ic_statistics),
    BottomNavItem(Screen.AI.route, R.string.nav_ai, R.drawable.ic_ai),
    BottomNavItem(Screen.Profile.route, R.string.nav_profile, R.drawable.ic_profile),
)

// ── WiseFox App Shell ─────────────────────────────────────────────────────────
private val brush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFF8E8),
        Color(0xFFFFF0CC),
        Color(0xFFFFE8B0)
    )
)

/**
 * Wraps every main screen with:
 *  • Warm yellow gradient background
 *  • Large rounded orange content card
 *  • Fox mascot peeking from the top-left corner of the card
 *  • Bottom navigation bar
 *
 * Usage:
 *   WiseFoxLayout(navController = navController) {
 *       YourScreenContent()
 *   }
 */

@Composable
fun WiseFoxLayout(
    navController: NavController,
    content: @Composable () -> Unit
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
//            Color(0xFFFFF8E8),
//            Color(0xFFFFF0CC),
//            Color(0xFFFFE8B0)
            Color(0xFFFEDD7B),
            Color(0xFFFFE288),
            Color(0xFFFFE490),
            Color(0xFFFFE9A7),
            Color(0xFFFFEEB9)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top spacer so card doesn't hug the status bar ──
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

            // ── Main content card ──────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                // The orange card
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(32.dp),
                            ambientColor = WiseFoxOrangeDark.copy(alpha = 0.3f),
                            spotColor = WiseFoxOrangeDark.copy(alpha = 0.3f)
                        ),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = WiseFoxCardBg)
                ) {
                    content()
                }
            }

            // ── Bottom Navigation ──────────────────────────────
            WiseFoxBottomBar(navController = navController)

            // ── Bottom system bar spacing ──
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

// ── Bottom Bar ────────────────────────────────────────────────────────────────
@Composable
fun WiseFoxBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .border(
                width = 2.dp,
                color = Color.Gray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp)),
        containerColor = Color.White.copy(alpha = 0.85f),
        tonalElevation = 0.dp,
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentDestination
                ?.hierarchy
                ?.any { it.route == item.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Image(
                        painter = painterResource(id = item.icon),
                        contentDescription = stringResource(item.labelRes),
                        modifier = Modifier
                            .size(40.dp)
                            .padding(bottom = 2.dp)
                    )
                },
                label = { Text(stringResource(item.labelRes),
                    fontSize = 15.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = WiseFoxOrangeDark,
                    selectedTextColor = WiseFoxOrangeDark,
                    indicatorColor = WiseFoxOrangePale,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                )
            )
        }
    }
}