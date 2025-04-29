package com.avnish.qrscan.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.avnish.qrscan.R
import com.avnish.qrscan.ads.AdManager
import com.avnish.qrscan.screens.GenerateScreen
import com.avnish.qrscan.screens.InfoScreen
import com.avnish.qrscan.screens.ScanScreen
import com.avnish.qrscan.screens.SplashScreen
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

sealed class Screen(val route: String, val title: String, val iconResId: Int) {
    object Scan : Screen("scan", "Scan", R.drawable.ic_scan)
    object Generate : Screen("generate", "Generate", R.drawable.ic_generate)
    object Info : Screen("info", "Info", R.drawable.ic_info)
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Scan,
        Screen.Generate,
        Screen.Info
    )
    
    Scaffold(
        bottomBar = {
            Column {
                AdManager.BannerAd()
                NavigationBar(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    painter = painterResource(id = screen.iconResId),
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Scan.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = {
                fadeIn(tween(10))
            },
            exitTransition = {
                fadeOut(tween(10))
            }
        ) {

            composable(Screen.Scan.route) { 
                ScanScreen(
                    onNavigateToGenerate = {
                        navController.navigate(Screen.Generate.route) {
                            popUpTo(Screen.Scan.route) { inclusive = true }
                        }
                    },
                    onNavigateToInfo = {
                        navController.navigate(Screen.Info.route) {
                            popUpTo(Screen.Scan.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Generate.route) { 
                GenerateScreen(
                    onNavigateToScan = {
                        navController.navigate(Screen.Scan.route) {
                            popUpTo(Screen.Generate.route) { inclusive = true }
                        }
                    },
                    onNavigateToInfo = {
                        navController.navigate(Screen.Info.route) {
                            popUpTo(Screen.Generate.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Info.route) { 
                InfoScreen(
                    onNavigateToScan = {
                        navController.navigate(Screen.Scan.route) {
                            popUpTo(Screen.Info.route) { inclusive = true }
                        }
                    },
                    onNavigateToGenerate = {
                        navController.navigate(Screen.Generate.route) {
                            popUpTo(Screen.Info.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
} 