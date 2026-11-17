package com.example.stocks.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.stocks.ui.detail.ItemDetailScreen
import com.example.stocks.ui.flipper.FlipperScreen
import com.example.stocks.ui.portfolio.PortfolioScreen
import com.example.stocks.ui.search.SearchScreen
import com.example.stocks.ui.watchlist.WatchlistScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Portfolio.route) {
        composable(Screen.Portfolio.route) {
            PortfolioScreen(onItemClick = { tag -> navController.navigate(Screen.Detail.route(tag)) })
        }
        composable(Screen.Flipper.route) {
            FlipperScreen(onItemClick = { tag -> navController.navigate(Screen.Detail.route(tag)) })
        }
        composable(Screen.Watchlist.route) {
            WatchlistScreen(onItemClick = { tag -> navController.navigate(Screen.Detail.route(tag)) })
        }
        composable(Screen.Search.route) {
            SearchScreen(onItemClick = { tag -> navController.navigate(Screen.Detail.route(tag)) })
        }
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("itemTag") { type = NavType.StringType })
        ) { backStack ->
            val tag = backStack.arguments?.getString("itemTag") ?: return@composable
            ItemDetailScreen(itemTag = tag, onBack = { navController.popBackStack() })
        }
    }
}
