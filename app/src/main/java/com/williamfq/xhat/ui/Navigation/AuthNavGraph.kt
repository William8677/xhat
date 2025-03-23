package com.williamfq.xhat.ui.Navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.williamfq.xhat.ui.screens.auth.PhoneNumberScreen
import com.williamfq.xhat.ui.screens.auth.ProfileSetupScreen
import com.williamfq.xhat.ui.screens.auth.VerificationCodeScreen

@Composable
fun AuthNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.PhoneNumber.route
    ) {
        composable(Screen.PhoneNumber.route) {
            PhoneNumberScreen(navController = navController)
        }
        composable(
            route = Screen.VerificationCode.route,
            arguments = listOf(
                navArgument("phoneNumber") { type = NavType.StringType },
                navArgument("verificationId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            val verificationId = backStackEntry.arguments?.getString("verificationId") ?: ""
            VerificationCodeScreen(
                navController = navController,
                phoneNumber = phoneNumber,
                verificationId = verificationId
            )
        }
        composable(Screen.ProfileSetup.route) {
            ProfileSetupScreen(navController = navController)
        }
    }
}
