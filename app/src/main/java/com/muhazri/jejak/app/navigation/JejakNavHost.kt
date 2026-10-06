package com.muhazri.jejak.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.muhazri.jejak.features.home.presentation.screens.HomeScreen
import com.muhazri.jejak.features.onboarding.presentation.screens.OnboardingScreen
import com.muhazri.jejak.features.session.presentation.screens.ActiveSessionScreen
import com.muhazri.jejak.features.session.presentation.screens.SessionDetailScreen
import com.muhazri.jejak.features.settings.presentation.screens.SettingsScreen

@Composable
fun JejakNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Route.Onboarding,
        modifier = modifier,
    ) {
        composable<Route.Onboarding> {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Route.Home) {
                        popUpTo<Route.Onboarding> { inclusive = true }
                    }
                },
            )
        }

        composable<Route.Home> {
            HomeScreen(
                onOpenSettings = { navController.navigate(Route.Settings) },
                onStartActivity = { navController.navigate(Route.ActiveSession(it)) },
                onOpenLastSession = { navController.navigate(Route.SessionDetail) },
            )
        }

        composable<Route.Settings> {
            SettingsScreen(onBack = navController::popBackStack)
        }

        composable<Route.ActiveSession> { entry ->
            ActiveSessionScreen(
                activity = entry.toRoute<Route.ActiveSession>().activity,
                onClose = navController::popBackStack,
            )
        }

        composable<Route.SessionDetail> {
            SessionDetailScreen(onBack = navController::popBackStack)
        }
    }
}
