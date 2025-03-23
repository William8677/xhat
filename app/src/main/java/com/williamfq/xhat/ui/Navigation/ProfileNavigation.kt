package com.williamfq.xhat.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.williamfq.xhat.ui.screens.auth.ProfileSetupScreen

fun NavGraphBuilder.profileNavGraph(navController: NavController) {
    composable("profile_setup") {
        ProfileSetupScreen(
            navController = navController
        )
    }
}