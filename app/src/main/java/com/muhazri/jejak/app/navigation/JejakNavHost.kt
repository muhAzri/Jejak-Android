package com.muhazri.jejak.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.muhazri.jejak.features.counter.presentation.screens.CounterScreen

@Composable
fun JejakNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Route.Counter,
        modifier = modifier,
    ) {
        composable<Route.Counter> {
            CounterScreen()
        }
    }
}
