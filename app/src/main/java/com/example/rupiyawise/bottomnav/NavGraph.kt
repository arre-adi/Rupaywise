package com.example.rupiyawise.bottomnav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.rupiyawise.BottomBarScreen
import com.example.rupiyawise.CategorizeExpenseScreen
import com.example.rupiyawise.ExpenseTrackerAppPreview
import com.example.rupiyawise.ReelScreen
import com.example.rupiyawise.RupiyawiseApp
import com.example.rupiyawise.ScreenWithFloatingMic



@Composable
fun BottomNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = BottomBarScreen.Home.route
    ) {
        composable(route = BottomBarScreen.Home.route) {
            ScreenWithFloatingMic(
                content = { RupiyawiseApp(navController) },
                navController = navController
            )
        }
        composable(route = BottomBarScreen.Statistics.route) {
            ScreenWithFloatingMic(
                content = {  ExpenseTrackerAppPreview() },
                navController = navController
            )
        }
        composable(route = BottomBarScreen.Categorize.route) {
            ScreenWithFloatingMic(
                content = {   CategorizeExpenseScreen() },
                navController = navController
            )
        }
        composable(route = BottomBarScreen.Reels.route) {
            ScreenWithFloatingMic(
                content = { ReelScreen() },
                navController = navController
            )
        }
    }
}
