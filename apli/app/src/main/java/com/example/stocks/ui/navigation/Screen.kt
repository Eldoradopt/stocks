package com.example.stocks.ui.navigation

sealed class Screen(val route: String) {
    object Portfolio : Screen("portfolio")
    object Flipper   : Screen("flipper")
    object Watchlist : Screen("watchlist")
    object Search    : Screen("search")
    object Detail    : Screen("detail/{itemTag}") {
        fun route(tag: String) = "detail/$tag"
    }
}
