package com.blobatic.shieldfoxvpn.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.blobatic.shieldfoxvpn.ui.screens.HomeScreen
import com.blobatic.shieldfoxvpn.ui.screens.ServerListScreen
import com.blobatic.shieldfoxvpn.ui.screens.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object ServerList : Screen("servers")
    object Settings : Screen("settings")
}

@Composable
fun VPNNavGraph(
    onRequestVpnPermission: (callback: (Boolean) -> Unit) -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToServers = { navController.navigate(Screen.ServerList.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onRequestVpnPermission = onRequestVpnPermission
            )
        }
        composable(Screen.ServerList.route) {
            ServerListScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
