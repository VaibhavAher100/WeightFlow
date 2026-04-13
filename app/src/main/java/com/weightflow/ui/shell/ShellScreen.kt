package com.weightflow.ui.shell

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.padding
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.weightflow.ui.navigation.Screen
import com.weightflow.ui.navigation.WeightFlowNavGraph

private data class Tab(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
)

private val tabs = listOf(
    Tab(Screen.Home,    "Home",    Icons.Filled.Home),
    Tab(Screen.Trends,  "Trends",  Icons.AutoMirrored.Filled.TrendingUp),
    Tab(Screen.History, "History", Icons.AutoMirrored.Filled.List),
    Tab(Screen.Profile, "Profile", Icons.Filled.Person),
)

@Composable
fun ShellScreen() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val onHomeTab = currentDestination?.hierarchy?.any { it.route == Screen.Home.route } == true

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    val selected = currentDestination?.hierarchy
                        ?.any { it.route == tab.screen.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
        floatingActionButton = {
            if (onHomeTab) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.LogEntry.route) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Log weight")
                }
            }
        },
    ) { innerPadding ->
        WeightFlowNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
