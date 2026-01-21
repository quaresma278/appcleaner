package com.temizlepro.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.temizlepro.R
import com.temizlepro.data.HomeViewModel
import com.temizlepro.data.ScanViewModel
import com.temizlepro.data.SettingsViewModel

sealed class Screen(val route: String, val label: Int, val icon: @Composable () -> Unit) {
    data object Home : Screen("home", R.string.nav_home, { Icon(Icons.Filled.Home, null) })
    data object Scan : Screen("scan", R.string.nav_scan, { Icon(Icons.Filled.Storage, null) })
    data object Settings : Screen("settings", R.string.nav_settings, { Icon(Icons.Filled.Settings, null) })
    data object Info : Screen("info", R.string.nav_info, { Icon(Icons.Filled.Info, null) })
    data object Results : Screen("results", R.string.nav_results, { Icon(Icons.Filled.Storage, null) })
}

@Composable
fun TemizleProNavHost(
    homeViewModel: HomeViewModel,
    scanViewModel: ScanViewModel,
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Scan, Screen.Settings, Screen.Info)

    androidx.compose.material3.Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = screen.icon,
                        label = { Text(stringResource(screen.label)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(homeViewModel, onQuickScan = {
                    navController.navigate(Screen.Scan.route)
                })
            }
            composable(Screen.Scan.route) {
                ScanScreen(
                    scanViewModel = scanViewModel,
                    settingsViewModel = settingsViewModel,
                    onScanFinished = { navController.navigate(Screen.Results.route) }
                )
            }
            composable(Screen.Results.route) {
                ResultsScreen(scanViewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(settingsViewModel)
            }
            composable(Screen.Info.route) {
                InfoScreen()
            }
        }
    }
}
