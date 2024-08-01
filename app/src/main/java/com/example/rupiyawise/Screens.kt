package com.example.rupiyawise



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
        icon = R.drawable.statsss
    )


    object Reels: BottomBarScreen(
        route = "reels",
        icon =  R.drawable.vssada,
    )

    object Categorize: BottomBarScreen(
        route = "categorize",
        icon = R.drawable.oothers
    )


}