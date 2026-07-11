package com.dmariani.streamkit.app.ui

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dmariani.streamkit.app.ui.navigation.AppNavGraph
import com.dmariani.streamkit.app.ui.navigation.AppRoutes
import com.dmariani.streamkit.core.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-Activity host — hosts `AppNavGraph` and drives per-destination
 * screen orientation locking as the nav back stack changes (ARCHITECTURE.md,
 * Screen Orientation).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val backStackEntry by navController.currentBackStackEntryAsState()

            LaunchedEffect(backStackEntry?.destination?.route) {
                when (backStackEntry?.destination?.route) {
                    AppRoutes.CATALOG, AppRoutes.SETTINGS ->
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }

            AppTheme {
                AppNavGraph(navController = navController)
            }
        }
    }
}
