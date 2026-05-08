package com.weightflow.ui.shell

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.weightflow.WeightFlowApp
import com.weightflow.ui.logentry.LogEntrySheet
import com.weightflow.ui.logentry.LogEntryViewModel
import com.weightflow.ui.navigation.Screen
import com.weightflow.ui.navigation.WeightFlowNavGraph
import com.weightflow.ui.theme.WFTokens
import kotlinx.coroutines.launch

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShellScreen(app: WeightFlowApp) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val onHomeTab = currentDestination?.hierarchy?.any { it.route == Screen.Home.route } == true

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showLogEntry by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val logEntryVm: LogEntryViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                LogEntryViewModel(app.weightRepository, app.userPrefsDataStore) as T
        },
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = WFTokens.Border, thickness = 1.dp)
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp,
                ) {
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
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = WFTokens.Text3,
                                unselectedTextColor = WFTokens.Text3,
                            ),
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (onHomeTab) {
                FloatingActionButton(
                    onClick = {
                        logEntryVm.reset()
                        showLogEntry = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Log weight")
                }
            }
        },
    ) { innerPadding ->
        WeightFlowNavGraph(
            app = app,
            navController = navController,
            snackbarHostState = snackbarHostState,
            modifier = Modifier.padding(innerPadding),
        )

        if (showLogEntry) {
            ModalBottomSheet(
                onDismissRequest = { showLogEntry = false },
                sheetState = sheetState,
            ) {
                LogEntrySheet(
                    viewModel = logEntryVm,
                    onDismiss = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            showLogEntry = false
                        }
                    },
                )
            }
        }
    }
}
