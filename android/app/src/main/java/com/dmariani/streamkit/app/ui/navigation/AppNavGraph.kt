package com.dmariani.streamkit.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

/**
 * Hosts the app's single `NavHost` — Catalog, Player, Live Player, and
 * Settings. Player, Live Player, and Settings are stubs until their
 * respective features are implemented.
 */
@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = AppRoutes.CATALOG) {
        composable(AppRoutes.CATALOG) {
            PlaceholderScreen(label = "Catalog")
        }
        composable(
            route = AppRoutes.PLAYER,
            arguments = listOf(navArgument(AppRoutes.VIDEO_ID_ARG) { type = NavType.StringType }),
        ) {
            PlaceholderScreen(label = "Player")
        }
        composable(
            route = AppRoutes.LIVE_PLAYER,
            arguments = listOf(navArgument(AppRoutes.VIDEO_ID_ARG) { type = NavType.StringType }),
        ) {
            PlaceholderScreen(label = "Live Player")
        }
        composable(AppRoutes.SETTINGS) {
            PlaceholderScreen(label = "Settings")
        }
    }
}

@Composable
private fun PlaceholderScreen(label: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = label)
    }
}
