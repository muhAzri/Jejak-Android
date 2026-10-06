package com.muhazri.jejak.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.muhazri.jejak.features.home.presentation.screens.HomeRoute
import com.muhazri.jejak.features.onboarding.presentation.screens.OnboardingRoute
import com.muhazri.jejak.features.session.presentation.screens.ActiveSessionRoute
import com.muhazri.jejak.features.session.presentation.screens.SessionDetailRoute
import com.muhazri.jejak.features.settings.presentation.screens.SettingsRoute

@Composable
fun JejakNavHost(
    /** Onboarding is only shown until it has been completed once. */
    isOnboardingCompleted: Boolean,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = if (isOnboardingCompleted) Route.Home else Route.Onboarding,
        modifier = modifier,
    ) {
        composable<Route.Onboarding> {
            OnboardingRoute(
                onFinish = {
                    navController.navigate(Route.Home) {
                        popUpTo<Route.Onboarding> { inclusive = true }
                    }
                },
            )
        }

        composable<Route.Home> {
            HomeRoute(
                onOpenSettings = { navController.navigate(Route.Settings) },
                onStartActivity = { navController.navigate(Route.ActiveSession(it)) },
                onOpenSessionDetail = { navController.navigate(Route.SessionDetail(it)) },
            )
        }

        composable<Route.Settings> {
            SettingsRoute(onBack = navController::popBackStack)
        }

        composable<Route.ActiveSession> { entry ->
            ActiveSessionRoute(
                activity = entry.toRoute<Route.ActiveSession>().activity,
                onClose = navController::popBackStack,
            )
        }

        composable<Route.SessionDetail> { entry ->
            SessionDetailRoute(
                sessionId = entry.toRoute<Route.SessionDetail>().sessionId,
                onBack = navController::popBackStack,
            )
        }
    }
}
