package com.weightflow.ui.navigation

sealed class Screen(val route: String) {
    data object Home     : Screen("home")
    data object Trends   : Screen("trends")
    data object History  : Screen("history")
    data object Profile  : Screen("profile")
    data object LogEntry : Screen("log_entry")
}
