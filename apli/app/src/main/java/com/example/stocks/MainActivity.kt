package com.example.stocks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.stocks.ui.navigation.AppNavGraph
import com.example.stocks.ui.navigation.Screen
import com.example.stocks.ui.theme.StocksTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StocksTheme {
                SkyBlockMainContent()
            }
        }
    }
}

@Composable
private fun SkyBlockMainContent() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val bottomNavItems = listOf(
        Triple(Screen.Portfolio, "Portfolio", Icons.Default.PieChart),
        Triple(Screen.Watchlist, "Watchlist", Icons.AutoMirrored.Filled.List),
        Triple(Screen.Search,    "Search",    Icons.Default.Search),
    )
    // Hide bottom bar on detail screen
    val showBottomBar = currentRoute != Screen.Detail.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { (screen, label, icon) ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick  = {
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Portfolio.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon  = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
