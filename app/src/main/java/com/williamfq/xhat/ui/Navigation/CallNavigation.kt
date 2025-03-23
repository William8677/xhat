/*
 * Updated: 2025-01-22 01:29:38
 * Author: William8677
 */

package com.williamfq.xhat.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.williamfq.xhat.ui.calls.CallHistoryScreen

fun NavGraphBuilder.callNavGraph(navController: NavController) {
    composable("calls") {
        CallHistoryScreen(
            onNavigateToChat = { userId ->
                navController.navigate("chat/$userId")
            }
        )
    }
}