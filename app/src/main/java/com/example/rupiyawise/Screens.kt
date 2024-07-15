package com.example.rupiyawise

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home

sealed class Screens(route:String) {
//    object submitEcoBrick : Screens("submit_eco_brick")
}

sealed class BottomBarScreen(
    val route: String,
    val icon: Int,
) {
    object Home: BottomBarScreen(
        route = "home",
        icon =  R.drawable.baseline_home_filled_24,
    )


    object Statistics: BottomBarScreen(
        route = "statistics",
        icon = R.drawable.baseline_auto_graph_24
    )


    object Reels: BottomBarScreen(
        route = "reels",
        icon =  R.drawable.baseline_movie_filter_24,
    )

    object Categorize: BottomBarScreen(
        route = "categorize",
        icon = R.drawable.baseline_filter_alt_24
    )
    object Others: BottomBarScreen(
        route = "others",
        icon = R.drawable.baseline_more_horiz_24
    )

}