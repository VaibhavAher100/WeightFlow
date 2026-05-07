package com.weightflow.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.weightflow.WeightFlowApp
import com.weightflow.ui.history.HistoryScreen
import com.weightflow.ui.history.HistoryViewModel
import com.weightflow.ui.home.HomeScreen
import com.weightflow.ui.home.HomeViewModel
import com.weightflow.ui.profile.ProfileScreen
import com.weightflow.ui.profile.ProfileViewModel
import com.weightflow.ui.settings.SettingsScreen
import com.weightflow.ui.settings.SettingsViewModel
import com.weightflow.ui.trends.TrendsScreen
import com.weightflow.ui.trends.TrendsViewModel

@Composable
fun WeightFlowNavGraph(
    app: WeightFlowApp,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(180)) },
        exitTransition = { fadeOut(animationSpec = tween(180)) },
        popEnterTransition = { fadeIn(animationSpec = tween(180)) },
        popExitTransition = { fadeOut(animationSpec = tween(180)) },
    ) {
        composable(Screen.Home.route) {
            val vm: HomeViewModel = viewModel(
                factory = vmFactory { HomeViewModel(app.homeDataAggregator, app.badgeObserver) },
            )
            HomeScreen(vm, snackbarHostState)
        }
        composable(Screen.Trends.route) {
            val vm: TrendsViewModel = viewModel(
                factory = vmFactory { TrendsViewModel(app.weightRepository, app.userPrefsDataStore, app.userProfileRepository) },
            )
            TrendsScreen(vm)
        }
        composable(Screen.History.route) {
            val vm: HistoryViewModel = viewModel(
                factory = vmFactory { HistoryViewModel(app.weightRepository, app.userPrefsDataStore) },
            )
            HistoryScreen(vm)
        }
        composable(Screen.Profile.route) {
            val vm: ProfileViewModel = viewModel(
                factory = vmFactory {
                    ProfileViewModel(
                        app.userProfileRepository,
                        app.userPrefsDataStore,
                        app.weightRepository,
                        app.badgeObserver,
                    )
                },
            )
            ProfileScreen(vm, onSettingsClick = { navController.navigate(Screen.Settings.route) })
        }
        composable(Screen.Settings.route) {
            val vm: SettingsViewModel = viewModel(
                factory = vmFactory { SettingsViewModel(app.userPrefsDataStore, app.weightRepository) },
            )
            SettingsScreen(vm, onBack = { navController.popBackStack() })
        }
    }
}

private inline fun <reified VM : androidx.lifecycle.ViewModel> vmFactory(
    crossinline create: () -> VM,
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = create() as T
}

@Composable
private fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
