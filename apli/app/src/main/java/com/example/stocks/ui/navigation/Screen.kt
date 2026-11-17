package com.example.stocks.ui.navigation

sealed class Screen(val route: String) {
    object Portfolio : Screen("portfolio")
    object Flipper   : Screen("flipper")
    object Watchlist : Screen("watchlist")
    object Search    : Screen("search")
    object BazaarWatcher : Screen("bazaar_watcher")
    object BossTracker : Screen("boss_tracker")
    object Detail    : Screen("detail/{itemTag}") {
        fun route(tag: String) = "detail/$tag"
    }
}
