package com.example.rupiyawise.bottomnav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.rupiyawise.BottomBarScreen
import com.example.rupiyawise.CategorizeExpenseScreen
import com.example.rupiyawise.EduReelsScreen
import com.example.rupiyawise.OtherOptionsScreen
import com.example.rupiyawise.RupiyawiseApp
import com.example.rupiyawise.StatsScreen

@Composable
fun BottomNavGraph(
    navController: NavHostController
) {
   NavHost(
       navController = navController,
       startDestination = BottomBarScreen.Home.route
   ) {
       composable(route=BottomBarScreen.Home.route){
           RupiyawiseApp(navController)
       }
        composable(route=BottomBarScreen.Statistics.route){
            StatsScreen()
        }

       composable(route=BottomBarScreen.Reels.route){
           EduReelsScreen()
       }

       composable(route=BottomBarScreen.Categorize.route){
           CategorizeExpenseScreen()
       }

       composable(route=BottomBarScreen.Others.route){
           OtherOptionsScreen()
       }

   }
}

