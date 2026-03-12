package com.example.wisefox.screens.common

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home.route,         R.string.nav_home,         Icons.Filled.Home),
    BottomNavItem(Screen.Transactions.route, R.string.nav_transactions, Icons.Filled.List),
    BottomNavItem(Screen.Ledger.route,       R.string.nav_ledger,       Icons.Filled.Menu),
    BottomNavItem(Screen.Profile.route,      R.string.nav_profile,      Icons.Filled.AccountCircle),
)

// ── WiseFox App Shell ─────────────────────────────────────────────────────────
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
            Color(0xFFFFF8E8),
            Color(0xFFFFF0CC),
            Color(0xFFFFE8B0)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top spacer so card doesn't hug the status bar ──
            Spacer(modifier = Modifier.height(48.dp))

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
                            spotColor   = WiseFoxOrangeDark.copy(alpha = 0.3f)
                        ),
                    shape  = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = WiseFoxCardBg)
                ) {
                    content()
                }

                // Fox mascot peeking from top-left corner of card
                Image(
                    painter = painterResource(id = R.drawable.ic_fox_mascot),
                    contentDescription = "WiseFox mascot",
                    modifier = Modifier
                        .size(72.dp)
                        .align(Alignment.TopStart)
                        .offset(x = 12.dp, y = (-32).dp)   // peek above card edge
                )
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
        modifier       = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(24.dp)),
        containerColor = Color.White.copy(alpha = 0.85f),
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentDestination
                ?.hierarchy
                ?.any { it.route == item.route } == true

            NavigationBarItem(
                selected = selected,
                onClick  = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
                icon = {
                    Icon(
                        imageVector        = item.icon,
                        contentDescription = stringResource(item.labelRes)
                    )
                },
                label  = { Text(stringResource(item.labelRes)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = WiseFoxOrangeDark,
                    selectedTextColor   = WiseFoxOrangeDark,
                    indicatorColor      = WiseFoxOrangePale,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                )
            )
        }
    }
}