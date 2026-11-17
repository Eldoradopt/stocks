package com.example.stocks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.stocks.ui.navigation.AppNavGraph
import com.example.stocks.ui.navigation.Screen
import com.example.stocks.ui.theme.DarkCard
import com.example.stocks.ui.theme.DarkCardBorder
import com.example.stocks.ui.theme.EmeraldProfit
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
        Triple(Screen.Portfolio, "Bank", Icons.Default.AccountBalance),
        Triple(Screen.Flipper,   "Flipper", Icons.Default.AutoGraph),
        Triple(Screen.Watchlist, "Watchlist", Icons.AutoMirrored.Filled.TrendingUp),
        Triple(Screen.BazaarWatcher, "Bazaar", Icons.Default.ShoppingCart),
        Triple(Screen.BossTracker, "Bosses", Icons.Default.BugReport),
        Triple(Screen.Search,    "Search", Icons.Default.Search),
    )
    // Hide bottom bar on detail screen
    val showBottomBar = currentRoute?.startsWith("detail") != true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = DarkCard,
                    modifier = Modifier.border(width = 1.dp, color = DarkCardBorder)
                ) {
                    bottomNavItems.forEach { (screen, label, icon) ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Portfolio.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = EmeraldProfit,
                                selectedTextColor = EmeraldProfit,
                                indicatorColor = DarkCardBorder,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        )
                    }
                }
            }
        }
    ) { _ ->
        AppNavGraph(navController = navController)
    }
}
